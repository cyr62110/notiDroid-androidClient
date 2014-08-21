package fr.cvlaminck.notidroid.android.base.services.push;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.undercouch.bson4jackson.BsonFactory;
import fr.cvlaminck.notidroid.android.api.cloud.CloudService;
import fr.cvlaminck.notidroid.android.api.cloud.intents.BindCloudServiceIntentBuilder;
import fr.cvlaminck.notidroid.android.api.push.ConnectionStatus;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnConnectionStatusChangedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallbackOptions;
import fr.cvlaminck.notidroid.android.base.accounts.NotidroidAccount;
import fr.cvlaminck.notidroid.android.base.converters.push.PushMessageConverters;
import fr.cvlaminck.notidroid.android.base.services.push.binders.impl.PushNotificationServiceStubImpl;
import fr.cvlaminck.notidroid.android.base.services.push.callbacks.OnConnectionStatusChangedCallbackList;
import fr.cvlaminck.notidroid.android.base.services.push.callbacks.OnMessageReceivedCallbackList;
import fr.cvlaminck.notidroid.android.base.services.push.listeners.OnAccountsUpdateListenerImpl;
import fr.cvlaminck.notidroid.android.base.services.push.runnables.CallOnMessageReceivedCallbacksRunnable;
import fr.cvlaminck.notidroid.android.base.services.push.runnables.RegisterMessageReceivedCallbackRunnable;

/**
 * Service handling the communication with the message broker. This service keeps an always-on communication
 * channel with the message broker.
 * <p/>
 * Since MQTT includes heartbeating, this service will try to heartbeat when the radio module
 * is in use avoiding useless waking up-sleeping cycle that could drain the battery. If it is not possible
 * to avoid waking up the radio module, this service will still do the heartbeat so the connection will not
 * be lost.
 * <p/>
 * This service expose a public API allowing third-party developers to use notidroid
 * as a push notification service between user devices. Application that want to use those
 * functions require to have the '' permission.
 */
@EService
public class NotidroidPushNotificationService
        extends Service {
    public static final String TAG = NotidroidPushNotificationService.class.getSimpleName();
    //Should this service output logs in the logcat.
    public static final boolean DEBUG = true;

    /**
     * Topic reserved to the user.
     */
    private final static String MQTT_USER_TOPIC = "users/me";

    private CloudServiceConnection cloudServiceConnection = null;

    private CloudService cloudService = null;

    private AccountManager accountManager = null;

    @Bean
    protected PushMessageConverters pushMessageConverters;

    private OnAccountsUpdateListener onAccountsUpdateListener = null;

    /**
     * Status of the connection between this service and the message broker.
     */
    private ConnectionStatus connectionStatus = new ConnectionStatus();

    /**
     * List of callbacks that will be called when the connection status changes.
     */
    private OnConnectionStatusChangedCallbackList onConnectionStatusChangedCallbacks = null;

    /**
     * List of callbacks that will be called when this service receive a message from
     * the message broker.
     */
    private OnMessageReceivedCallbackList onMessageReceivedCallbacks = null;

    /**
     * Client that is used to communicate with the message broker.
     */
    private MqttClient mqttClient = null;

    /**
     * Executor that will execute all runnables.
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Object mapper used to serialize push message and enclosed them into
     * MQTT message. It is also used to deserialize the content of received
     * message.
     */
    private ObjectMapper bsonObjectMapper = null;

    @Override
    public void onCreate() {
        super.onCreate();
        accountManager = AccountManager.get(this);
        //We init our callback lists
        onConnectionStatusChangedCallbacks = new OnConnectionStatusChangedCallbackList();
        onMessageReceivedCallbacks = new OnMessageReceivedCallbackList();

        //And our object mapper
        bsonObjectMapper = new ObjectMapper(new BsonFactory());
        configureBsonObjectMapper(bsonObjectMapper);

        //We bind to other notidroid services
        //The cloud service that abstract the REST api of the cloud backend.
        if (cloudServiceConnection == null) {
            final Intent bindIntent = new BindCloudServiceIntentBuilder().build();
            cloudServiceConnection = new CloudServiceConnection();
            bindService(bindIntent, cloudServiceConnection, BIND_AUTO_CREATE);
        }

        //This service tries to connect after its internal state has been initialized
        connect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PushNotificationServiceStubImpl(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //We unbind other services
        if (cloudServiceConnection != null) {
            unbindService(cloudServiceConnection);
            cloudServiceConnection = null;
        }
        //We do not forget to close our connection with the message broker before destroying this service
        if (mqttClient != null && mqttClient.isConnected())
            disconnect();
    }

    /**
     * Clear the internal state and remove all listeners registered by this service.
     * This function must be called to do a clean connect.
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void clear() {
        setConnectionStatus(ConnectionStatus.DetailedStatus.NOT_STARTED);
        if (onAccountsUpdateListener != null)
            accountManager.removeOnAccountsUpdatedListener(onAccountsUpdateListener);
    }

    /**
     * Establish a connection with the message broker.
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void connect() {
        if (DEBUG) Log.i(TAG, "Trying to connect to the message broker.");
        //We need to retrieve the user online account to authenticate this device on the MQ
        final NotidroidAccount account = NotidroidAccount.findOne(this);
        if (account == null || account.getDeviceId() == null) {
            if (DEBUG && account == null) Log.e(TAG, "No notidroid account on this device.");
            if (DEBUG && account.getDeviceId() == null)
                Log.e(TAG, "This device is not identified.");
            //We update the status of the connection
            if (account == null) setConnectionStatus(ConnectionStatus.DetailedStatus.NO_ACCOUNT);
            if (account.getDeviceId() == null)
                setConnectionStatus(ConnectionStatus.DetailedStatus.NO_DEVICE_ID);
            //If there is no account or no device id, we register an OnAccountsUpdateListener to
            //re-launch this connect function when needed.
            onAccountsUpdateListener = new OnAccountsUpdateListenerImpl(this);
            accountManager.addOnAccountsUpdatedListener(onAccountsUpdateListener, new Handler(), false);
            return;
        }
        //We check if we have a connection available to connect to the message broker
        //TODO Check if a connection is connected otherwise register a listener calling connect when CONNECTED
        //If we have an account then we try to connect to the message broker using MQTT.
        doConnect(account, 0);
    }

    @Background
    protected void doConnect(NotidroidAccount account, int internalNumberOfTry) {
        String accessToken = null;
        try {
            setConnectionStatus(ConnectionStatus.DetailedStatus.RETRIEVING_AUTH_TOKEN);
            //First, we need a valid OAuth2 access token and an username to
            //connect to the message broker
            final String deviceId = "device-" + account.getDeviceId();
            accessToken = account.blockingGetAuthToken();
            final String serverUrl = "tcp://192.168.1.183:8989"; //TODO : replace the IP by the value in the account. Debug the account.

            setConnectionStatus(ConnectionStatus.DetailedStatus.CONNECTING_WITH_MQTT);
            //Then we configure our Mqtt Client with those credentials
            final MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(deviceId);
            options.setPassword(accessToken.toCharArray());
            configureMqttClient(options);

            //And we start our client with memory persistence
            mqttClient = new MqttClient(serverUrl, deviceId, null);
            mqttClient.setCallback(mqttCallback);
            mqttClient.connect(options);

            //Finally we subscribe to the user reserved channel
            //We use a Qos of 0 since we do not care if we miss or loss messages.
            mqttClient.subscribe(MQTT_USER_TOPIC, 0);

            setConnectionStatus(ConnectionStatus.DetailedStatus.CONNECTED);
            if (DEBUG)
                Log.i(TAG, String.format("Connected to the message broker with credentials {username=%s, password=%s}", deviceId, accessToken));
        } catch (AuthenticatorException | OperationCanceledException e) {
            if (DEBUG)
                Log.e(TAG, "An error occurred while retrieving the token from the authenticator. " + e.toString());
            setConnectionStatus(ConnectionStatus.DetailedStatus.CANNOT_RETRIEVE_AUTH_TOKEN);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        } catch (MqttException e) {
            if (DEBUG)
                Log.e(TAG, "Error " + e.getReasonCode() + " occurred while connecting to the message broker. " + e.toString());
            if (e.getReasonCode() == MqttException.REASON_CODE_SERVER_CONNECT_ERROR) {
                //This exception can occur if we do not have an Internet or if the server is
                //down, we choose the first one because it is the more likely to happen.
                setConnectionStatus(ConnectionStatus.DetailedStatus.NO_NETWORK_AVAILABLE);
                //We register an alarm that retry to connect with exponential backoff
                //TODO
            } else if (e.getReasonCode() == MqttException.REASON_CODE_BROKER_UNAVAILABLE) {
                //The server send this error if the authentication fails, so we try one more time
                //after having invalidated our token.
                if (internalNumberOfTry == 0) {
                    setConnectionStatus(ConnectionStatus.DetailedStatus.AUTHENTICATION_FAILED);
                    //Invalidate the token
                    account.invalidateAuthToken(accessToken);
                    //and retry
                    doConnect(account, 1);
                } else {
                    //Well in this case the broker is really not available
                    //TODO
                    setConnectionStatus(ConnectionStatus.DetailedStatus.UNKNOWN);
                }
            }
        }
    }

    /**
     * Same as connect but clear the status and the listener before trying to connect.
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void reconnect() {
        clear();
        connect();
    }

    /**
     *
     */
    @Background
    protected void disconnect() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
            } finally {
                mqttClient = null;
            }
        }
    }

    /**
     * Configure the MQTT client that will be used to communicate with our message broker.
     *
     * @param options Configuration of our client
     */
    private void configureMqttClient(MqttConnectOptions options) {
        options.setCleanSession(true); //We do not use persistent connection. If the device is disconnected, it will not receive the message when it comes back online.
        //TODO : more configuration here
    }

    /**
     * Configure the object mapper used to serialize/deserialize the content of MQTT message.
     *
     * @param bsonObjectMapper ObjectMapper to configure.
     */
    private void configureBsonObjectMapper(ObjectMapper bsonObjectMapper) {
    }

    /**
     * Change the connection status and notify all callbacks registered.
     *
     * @param detailedStatus New connection status
     */
    protected void setConnectionStatus(ConnectionStatus.DetailedStatus detailedStatus) {
        //If the status has changed
        if (connectionStatus.getDetailedStatus() != detailedStatus) {
            connectionStatus.setDetailedStatus(detailedStatus);
            //We call all the registered callbacks
            onConnectionStatusChangedCallbacks.onConnectionStatusChanged(connectionStatus);
        }
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void registerOnPushMessageReceivedCallback(String packageName,
                                                      OnPushMessageReceivedCallback onMsgReceivedCallback,
                                                      OnPushMessageReceivedCallbackOptions options) {
        if(onMsgReceivedCallback == null)
            throw new IllegalArgumentException("onMsgReceivedCallback must not be null");
        if(options == null)
            options = OnPushMessageReceivedCallbackOptions.defaultOptions();
        //We execute the runnable that will register our callback
        final RegisterMessageReceivedCallbackRunnable runnable = new RegisterMessageReceivedCallbackRunnable(cloudService,
                onMessageReceivedCallbacks, packageName, onMsgReceivedCallback, options);
        executorService.submit(runnable);
    }

    public void unregisterOnPushMessageReceivedCallback(OnPushMessageReceivedCallback onMsgReceivedCallback) {
        //TODO
    }

    public void registerOnConnectionStatusChangedCallback(OnConnectionStatusChangedCallback onConnectionStatusChangedCallback) {
        if (onConnectionStatusChangedCallback == null)
            throw new IllegalArgumentException("onConnectionStatusChangedCallback must not be null.");
        onConnectionStatusChangedCallbacks.register(onConnectionStatusChangedCallback);
    }

    public void unregisterOnConnectionStatusChangedCallback(OnConnectionStatusChangedCallback onConnectionStatusChangedCallback) {
        if (onConnectionStatusChangedCallback == null)
            throw new IllegalArgumentException("onConnectionStatusChangedCallback must not be null.");
        onConnectionStatusChangedCallbacks.unregister(onConnectionStatusChangedCallback);
    }

    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            if (DEBUG) Log.d(TAG, "Connection with the broker lost. Cause : " + cause.getMessage());
            setConnectionStatus(ConnectionStatus.DetailedStatus.UNKNOWN); //TODO
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //When we receive a message, we create a runnable to convert the message and call the callbacks.
            final CallOnMessageReceivedCallbacksRunnable runnable = new CallOnMessageReceivedCallbacksRunnable(bsonObjectMapper,
                    message, pushMessageConverters, onMessageReceivedCallbacks);
            executorService.submit(runnable);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    };

    private class CloudServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            cloudService = CloudService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            cloudService = null;
        }
    }
}
