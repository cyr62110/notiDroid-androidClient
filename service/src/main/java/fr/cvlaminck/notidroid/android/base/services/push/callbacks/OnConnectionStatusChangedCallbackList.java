package fr.cvlaminck.notidroid.android.base.services.push.callbacks;

import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import fr.cvlaminck.notidroid.android.api.push.ConnectionStatus;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnConnectionStatusChangedCallback;
import fr.cvlaminck.notidroid.android.base.services.push.NotidroidPushNotificationService;

/**
 *
 */
public class OnConnectionStatusChangedCallbackList
    extends RemoteCallbackList<OnConnectionStatusChangedCallback> {

    @Override
    public void onCallbackDied(OnConnectionStatusChangedCallback callback, Object cookie) {
        Log.e(NotidroidPushNotificationService.TAG,
                "Removing callback due to the death of the calling process. Do not forget to call the 'unregister' function.");
        super.onCallbackDied(callback, cookie);
    }

    public void onConnectionStatusChanged(ConnectionStatus status) {
        final int count = this.beginBroadcast();
        for(int i = 0; i < count; i++) {
            final OnConnectionStatusChangedCallback broadcastItem = this.getBroadcastItem(i);
            try {
                broadcastItem.onConnectionStatusChanged(status);
            } catch (RemoteException e) {}
        }
        this.finishBroadcast();
    }

}
