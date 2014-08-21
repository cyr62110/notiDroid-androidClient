package fr.cvlaminck.notidroid.android.api.push.helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fr.cvlaminck.notidroid.android.api.push.ConnectionStatus;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnConnectionStatusChangedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallbackOptions;
import fr.cvlaminck.notidroid.android.api.push.intents.BindPushNotificationServiceIntentBuilder;
import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;
import fr.cvlaminck.notidroid.android.api.push.service.PushNotificationService;

/**
 * @since 0.2
 */
public abstract class PushAwareActivity
        extends Activity {
    private final static String TAG = PushAwareActivity.class.getSimpleName();
    private final static boolean DEBUG = true;

    private PushNotificationServiceConnection pushNotificationServiceConnection = null;

    //Helper callbacks

    @Override
    protected void onStart() {
        super.onStart();
        if (pushNotificationServiceConnection == null) {
            pushNotificationServiceConnection = new PushNotificationServiceConnection();
            final Intent intent = (new BindPushNotificationServiceIntentBuilder()).build();
            bindService(intent, pushNotificationServiceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pushNotificationServiceConnection != null) {
            unbindService(pushNotificationServiceConnection);
            pushNotificationServiceConnection = null;
        }
    }

    /**
     * Callback called when the status of the connection between the push notification service
     * and the message broker change.
     * <p/>
     * This function does nothing and are available for third-party developers who want to handle
     * the status of the connection more precisely. If you do not require this level of details, just
     * use the onPushNotificationServiceConnected and onPushNotificationServiceDisconnected callbacks.
     * Note that those callbacks are called first.
     *
     * @param connectionStatus Status of the connection between the service and the message broker.
     * @since 0.2
     */
    public void onConnectionStatusChanged(ConnectionStatus connectionStatus) {
        //Do nothing.
    }

    /**
     * Callback called when the push notification service is ready to be used.
     * This function is automatically if the service is connected when it is bound.
     *
     * @param pushNotificationService Interface of the service
     * @since 0.2
     */
    public abstract void onPushNotificationServiceConnected(PushNotificationService pushNotificationService);

    /**
     * Callback called when the application receive a push message through
     * the push service.
     *
     * @param message Message received.
     */
    public abstract void onMessageReceived(PushMessage message);

    /**
     * Callback called when the push notification service is no more available.
     * This function is automatically called when the service is unbound.
     *
     * @since 0.2
     */
    public abstract void onPushNotificationServiceDisconnected();

    /**
     * Configure the options of the callback called when a message is received by the push service for
     * this application.
     * <p/>
     * Override this function to change the behavior of the callback. Base implementation does not
     * make any change to the default options configured in the API.
     *
     * @param options Options of the callback
     * @since 0.2
     */
    public void configurePushMessageReceivedOptions(OnPushMessageReceivedCallbackOptions options) {
    }

    /**
     * Return the interface to communicate with the push notification service.
     * May be null if the service is not bond.
     *
     * @since 0.2
     */
    public PushNotificationService getPushNotificationService() {
        return (pushNotificationServiceConnection != null) ?
                pushNotificationServiceConnection.pushNotificationService : null;
    }

    private OnConnectionStatusChangedCallback onConnectionStatusChangedCallback = new OnConnectionStatusChangedCallback.Stub() {
        private ConnectionStatus.Status lastStatus = ConnectionStatus.Status.DISCONNECTED;

        @Override
        public void onConnectionStatusChanged(final ConnectionStatus status) throws RemoteException {
            if (lastStatus != status.getStatus()) {
                PushAwareActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (status.getStatus()) {
                            case DISCONNECTED:
                                onPushNotificationServiceDisconnected();
                                break;
                            case CONNECTING:
                                //Nothing to do here.
                                break;
                            case CONNECTED:
                                onPushNotificationServiceConnected(getPushNotificationService());
                                break;
                        }
                        PushAwareActivity.this.onConnectionStatusChanged(status);
                    }
                });
            }
            lastStatus = status.getStatus();
        }

    };

    private OnPushMessageReceivedCallback onPushMessageReceivedCallback = new OnPushMessageReceivedCallback.Stub() {

        @Override
        public void onPushMessageReceived(final PushMessage message) throws RemoteException {
            Log.d(TAG, "onPushMessageReceivedCallback");
            PushAwareActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(DEBUG)
                        Log.d(TAG, String.format("Message of type '%s' received from device %d.", message.getContentType().name(),
                                message.getFrom()));
                    PushAwareActivity.this.onMessageReceived(message);
                }
            });
        }

    };

    private class PushNotificationServiceConnection
            implements ServiceConnection {
        private PushNotificationService pushNotificationService;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //We retrieve the interface of the service to communicate with it
            pushNotificationService = PushNotificationService.Stub.asInterface(iBinder);

            try {
                //And we register some callbacks
                //One for the connection status
                pushNotificationService.registerOnConnectionStatusChangedCallback(onConnectionStatusChangedCallback);
                //One for the push messages
                final OnPushMessageReceivedCallbackOptions options = OnPushMessageReceivedCallbackOptions.defaultOptions();
                configurePushMessageReceivedOptions(options);
                pushNotificationService.registerOnPushMessageReceivedCallback(onPushMessageReceivedCallback, options);

                //Finally we call the callback depending on the status of the connection
                if (pushNotificationService.getConnectionStatus().getStatus() == ConnectionStatus.Status.CONNECTED)
                    onPushNotificationServiceConnected(pushNotificationService);
                //We also call the onConnectionStatusChanged callback
                onConnectionStatusChanged(pushNotificationService.getConnectionStatus());
            } catch (RemoteException e) {
                if (DEBUG)
                    Log.e(TAG, "An error occurred while registering callbacks. " + e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //We unregister our callbacks
            try {
                pushNotificationService.unregisterOnConnectionStatusChangedCallback(onConnectionStatusChangedCallback);
                pushNotificationService.unregisterOnPushMessageReceivedCallback(onPushMessageReceivedCallback);
            } catch (RemoteException e) {}
            pushNotificationService = null;
        }
    }

}
