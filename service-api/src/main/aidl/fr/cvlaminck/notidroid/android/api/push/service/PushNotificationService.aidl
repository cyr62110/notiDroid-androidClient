package fr.cvlaminck.notidroid.android.api.push.service;

import android.content.ComponentName;

import fr.cvlaminck.notidroid.android.api.push.ConnectionStatus;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnConnectionStatusChangedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallbackOptions;
import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;

/**
 * @since 0.2
 */
interface PushNotificationService {

    ConnectionStatus getConnectionStatus();

    /**
     * Send a message to all other devices connected to the message broker.
     *
     *
     * @since 0.2
     */
    void send(in PushMessage message);

    /**
     * Register a callback that will be called when your application receive a message
     * through the push notification service. Do not forget to unregister the callback when
     * you are done using it to avoid memory leadks.
     *
     * @param onMsgReceivedCallback Callback that must be called when your application receive a
     * message through the push notification service.
     * @param options Options to apply.
     * @since 0.2
     */
    void registerOnPushMessageReceivedCallback(OnPushMessageReceivedCallback onMsgReceivedCallback,
                          in OnPushMessageReceivedCallbackOptions options);

    /**
     * Register a callback that will be called when the status of the connection between the push
     * notification service and the message broker change. Do not forget to unregister the callback when
     * you are done using it to avoid memory leadks.
     *
     * @param onConnectionStatusChangedCallback Callback that must be called when the status of the connection change.
     * @since 0.2
     */
    void registerOnConnectionStatusChangedCallback(OnConnectionStatusChangedCallback onConnectionStatusChangedCallback);

    /**
     * Register a broadcast receiver that will receive an intent when your application receive a
     * message through the push notification service.
     * </p>
     * /!\ Do not forget to export your broadcast receiver in your manifest and set the permission
     * '' on it. The permission will allow only the push notification service to send intent to
     * your broadcast receiver.
     * TODO : Add an options class as for binder based callback
     *
     * @param componentName ComponentName of your broadcast receiver.
     * @since 0.2
     */
    void registerBroadcastReceiver(in ComponentName componentName);

    /**
     * Unregister a callback previously registered with registerOnPushMessageReceivedCallback.
     *
     * @since 0.2
     */
    void unregisterOnPushMessageReceivedCallback(OnPushMessageReceivedCallback onMsgReceivedCallback);

    /**
     * Unregister a callback previously registered with registerOnConnectionStatusChanged.
     *
     * @since 0.2
     */
    void unregisterOnConnectionStatusChangedCallback(OnConnectionStatusChangedCallback onConnectionStatusChangedCallback);

    /**
     * Unregister a broadcast receiver registered with registerBroadcastReceiver.
     *
     * @since 0.2
     */
    void unregisterBroadcastReceiver(in ComponentName componentName);

}
