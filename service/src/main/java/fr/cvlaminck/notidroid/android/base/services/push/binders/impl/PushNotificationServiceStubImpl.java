package fr.cvlaminck.notidroid.android.base.services.push.binders.impl;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;

import fr.cvlaminck.notidroid.android.api.push.ConnectionStatus;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnConnectionStatusChangedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallbackOptions;
import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;
import fr.cvlaminck.notidroid.android.api.push.service.PushNotificationService;
import fr.cvlaminck.notidroid.android.base.services.push.NotidroidPushNotificationService;
import fr.cvlaminck.notidroid.android.base.utils.StubImplUtils;

/**
 * Implementation of the interface allowing other applications to communicate
 * with our push notification service through AIDL
 */
public class PushNotificationServiceStubImpl
        extends PushNotificationService.Stub {
    private static final String TAG = PushNotificationServiceStubImpl.class.getSimpleName();
    private static final boolean DEBUG = false;

    private NotidroidPushNotificationService pushNotificationService = null;

    private ActivityManager activityManager = null;

    private StubImplUtils stubImplUtils = null;

    public PushNotificationServiceStubImpl(NotidroidPushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
        this.activityManager = (ActivityManager) pushNotificationService.getSystemService(Context.ACTIVITY_SERVICE);
        this.stubImplUtils = new StubImplUtils(pushNotificationService);
    }

    @Override
    public ConnectionStatus getConnectionStatus() throws RemoteException {
        return pushNotificationService.getConnectionStatus();
    }

    @Override
    public void send(PushMessage message) throws RemoteException {
        //TODO
    }

    @Override
    public void registerOnPushMessageReceivedCallback(OnPushMessageReceivedCallback onMsgReceivedCallback,
                                                      OnPushMessageReceivedCallbackOptions options) throws RemoteException {
        final String callingPackageName = getCallingPackageName();
        pushNotificationService.registerOnPushMessageReceivedCallback(callingPackageName,
                onMsgReceivedCallback, options);
    }

    @Override
    public void registerOnConnectionStatusChangedCallback(OnConnectionStatusChangedCallback onConnectionStatusChangedCallback) throws RemoteException {
        pushNotificationService.registerOnConnectionStatusChangedCallback(onConnectionStatusChangedCallback);
    }

    @Override
    public void registerBroadcastReceiver(ComponentName componentName) throws RemoteException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void unregisterOnPushMessageReceivedCallback(OnPushMessageReceivedCallback onMsgReceivedCallback) throws RemoteException {

    }

    @Override
    public void unregisterOnConnectionStatusChangedCallback(OnConnectionStatusChangedCallback onConnectionStatusChangedCallback) throws RemoteException {
        pushNotificationService.unregisterOnConnectionStatusChangedCallback(onConnectionStatusChangedCallback);
    }

    @Override
    public void unregisterBroadcastReceiver(ComponentName componentName) throws RemoteException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private String getCallingPackageName() {
        return stubImplUtils.getCallingPackageName(getCallingUid());
    }
}
