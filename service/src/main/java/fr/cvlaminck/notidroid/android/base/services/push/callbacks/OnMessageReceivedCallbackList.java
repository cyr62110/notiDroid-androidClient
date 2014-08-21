package fr.cvlaminck.notidroid.android.base.services.push.callbacks;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallback;
import fr.cvlaminck.notidroid.android.api.push.callbacks.OnPushMessageReceivedCallbackOptions;
import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;
import fr.cvlaminck.notidroid.android.base.services.push.NotidroidPushNotificationService;

/**
 * When a message is received from the message broker, we have two kind of
 * callback to call : Intent-based or Binder-based.
 */
public class OnMessageReceivedCallbackList {
    private static final String TAG = OnMessageReceivedCallbackList.class.getSimpleName();
    private static final boolean DEBUG = true;

    /**
     * Map containing all registered callbacks.
     * Callbacks are associated with the id of the application having
     * registered those one.
     */
    private Map<Long, Set<Callback>> callbacks = null;

    /**
     * Map allowing us to retrieve the app id using the
     * callback as key. This map is used when a callback is
     * unregistered to find the list in which this callback is
     * stored.
     */
    private Map<Callback, Long> appIds = null;

    public OnMessageReceivedCallbackList() {
        this.callbacks = new HashMap<>();
        this.appIds = new HashMap<>();
    }

    public void registerOnMessageReceivedCallback(long appId, String packageName, OnPushMessageReceivedCallback callback,
                                                  OnPushMessageReceivedCallbackOptions options) {
        if (callback == null)
            throw new IllegalArgumentException("callback must not be null");
        if (options == null)
            throw new IllegalArgumentException("options must not be null");
        //We register the callback
        final BinderBasedCallback binderBasedCallback = new BinderBasedCallback(packageName, callback, options);
        registerCallback(appId, binderBasedCallback);
        //And we also linkToDeath so we can remove the

    }

    public void registerBroadcastReceiver(long appId, String packageName, ComponentName componentName) {
        //TODO
    }

    private void registerCallback(long appId, Callback callback) {
        synchronized (callbacks) {
            //We associate the callback we are going to register with its app id.
            appIds.put(callback, appId);
            //Then we register this callback in the list of callbacks associated with app id.
            Set<Callback> appCallbacks = callbacks.get(appId);
            //If the application has no other registered callbacks, we create the list
            if (appCallbacks == null) {
                appCallbacks = new HashSet<>();
                callbacks.put(appId, appCallbacks);
            }
            //We must check if the callback is not already registered before trying to register it
            if (!appCallbacks.contains(callback)) {
                //We call the init function on the new callback to initialize its internal state
                callback.init();
                //Then we add our new callback
                appCallbacks.add(callback);
            }
        }
    }

    public void unregisterBroadcastReceiver() {
        //TODO
    }

    public void unregisterOnMessageReceivedCallback(OnPushMessageReceivedCallback callback) {

    }

    private void unregisterCallback(Callback callback) {
        synchronized (callbacks) {
            //We retrieve the app id of the application having registered this callback
            final Long appId = appIds.get(callback);
            if (appId == null)
                return;
            appIds.remove(callback);
            //Then we retrieve the set of callbacks for this app
            final Set<Callback> appCallbacks = callbacks.get(appId);
            if (appCallbacks == null) {
                return;
            }
            //We execute the destroy function on the registered callback.
            for (Callback c : appCallbacks) {
                if (c.equals(callback)) {
                    c.destroy();
                    break;
                }
            }
            appCallbacks.remove(callback);
            //If the set is empty, we remove the app from the map of callbacks
            if (appCallbacks.isEmpty())
                callbacks.remove(appId);
        }
    }

    /**
     * Call all callbacks registered to receive message for the application
     * with the provided id.
     *
     * @param appId Application that must receive and handle the message
     * @param message Message received through the message broker
     */
    public void onMessageReceived(long appId, PushMessage message) {
        final Callback[] callbacks = getCallbacksRegisteredForApp(appId);
        if(DEBUG)
            Log.d(TAG, String.format("%d callback(s) will be called because a message has been received.", callbacks.length));
        for(Callback callback : callbacks)
            callback.invoke(message);
    }

    /**
     * Return a copy of the list of callbacks registered for the given application.
     *
     * @param appId Application that must receive and handle the message
     * @return Copy of callbacks registered for the application.
     */
    private Callback[] getCallbacksRegisteredForApp(long appId) {
        Callback[] appCallbacks = null;
        synchronized (callbacks) {
            final Set<Callback> appCallbackSet = callbacks.get(appId);
            if(appCallbackSet != null) {
                appCallbacks = appCallbackSet.toArray(new Callback[appCallbackSet.size()]);
            } else {
                appCallbacks = new Callback[]{};
            }
        }
        return appCallbacks;
    }

    private abstract class Callback {

        /**
         * Package name of the application having registered this
         * callback.
         */
        protected String packageName = null;

        protected Callback(String packageName) {
            this.packageName = packageName;
        }

        /**
         * Function called when this callback is
         * added to the map of registered callbacks by the registerCallback function.
         * Base implementation is no-op.
         */
        public void init() {
        }

        ;

        /**
         * Invoke the callback.
         */
        public abstract void invoke(PushMessage message);

        /**
         * Function called when this callback is
         * removed from the map of registered callbacks by the unregisterCallback function.
         * Base implementation is no-op.
         */
        public void destroy() {
        }

        ;

    }

    /**
     * Callback sending the intent provided by the user
     * when registering its callback.
     */
    //TODO
    private class IntentBasedCallback
            extends Callback {

        private IntentBasedCallback(String packageName) {
            super(packageName);
        }

        @Override
        public void invoke(PushMessage message) {

        }
    }

    /**
     * Callback calling the method on the binder implementing
     * the callback interface.
     */
    private class BinderBasedCallback
            extends Callback {

        private OnPushMessageReceivedCallback callback = null;

        private OnPushMessageReceivedCallbackOptions options = null;

        private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                Log.e(NotidroidPushNotificationService.TAG, String.format("Application '%s' died without removing its OnMessageReceivedCallback registered on the push service.",
                        packageName));
                unregisterOnMessageReceivedCallback(callback);
            }
        };

        private BinderBasedCallback(String packageName, OnPushMessageReceivedCallback callback,
                                    OnPushMessageReceivedCallbackOptions options) {
            super(packageName);
            this.callback = callback;
            this.options = options;
        }

        @Override
        public void init() {
            //We register a DeathRecipient to be sure to remove the callback if the process
            //die.
            try {
                callback.asBinder().linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {}
        }

        @Override
        public void invoke(PushMessage message) {
            if(DEBUG)
                Log.d(TAG, String.format("Calling binder-based callback registered by '%s'. {callback=%s}", packageName, callback.toString()));
            //TODO handle options like filtering HERE
            try {
                callback.onPushMessageReceived(message);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException thrown while calling OnPushMessageReceivedCallback registered by '" + packageName + "' " + e.toString());
            }
        }

        @Override
        public void destroy() {
            callback.asBinder().unlinkToDeath(deathRecipient, 0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BinderBasedCallback)) return false;

            BinderBasedCallback that = (BinderBasedCallback) o;

            if (!callback.equals(that.callback)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return callback.hashCode();
        }
    }

}
