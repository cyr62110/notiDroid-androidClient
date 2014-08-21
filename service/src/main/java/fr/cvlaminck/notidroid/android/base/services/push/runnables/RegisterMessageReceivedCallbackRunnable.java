package fr.cvlaminck.notidroid.android.base.services.push.runnables;

import fr.cvlaminck.notidroid.android.api.cloud.CloudService;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallbackOptions;
import fr.cvlaminck.notidroid.android.base.services.push.callbacks.OnMessageReceivedCallbackList;

/**
 * Runnable that register an OnPushMessageReceivedCallback on the PushService.
 */
public class RegisterMessageReceivedCallbackRunnable
        implements Runnable {

    private CloudService cloudService = null;

    private OnMessageReceivedCallbackList onMessageReceivedCallbacks = null;

    private String packageName = null;

    private OnPushMessageReceivedCallback callback = null;

    private OnPushMessageReceivedCallbackOptions options = null;

    public RegisterMessageReceivedCallbackRunnable(CloudService cloudService, OnMessageReceivedCallbackList onMessageReceivedCallbacks, String packageName,
                                                   OnPushMessageReceivedCallback callback, OnPushMessageReceivedCallbackOptions options) {
        this.cloudService = cloudService;
        this.onMessageReceivedCallbacks = onMessageReceivedCallbacks;
        this.packageName = packageName;
        this.callback = callback;
        this.options = options;
    }

    @Override
    public void run() {
        try {
            //First, we need to retrieve the application id. We use the CloudService to do so.
            final long appId = cloudService.getApplicationByPackageName(packageName, false).getId();
            //Then we register our callback
            onMessageReceivedCallbacks.registerOnMessageReceivedCallback(appId, packageName,
                    callback, options);
        } catch (Exception e) {
            //TODO Find a way to return the exception to the calling app. Print stack trace for now.
            e.printStackTrace();
        }
    }

}
