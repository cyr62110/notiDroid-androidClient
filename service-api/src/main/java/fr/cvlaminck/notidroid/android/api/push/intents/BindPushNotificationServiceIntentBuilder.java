package fr.cvlaminck.notidroid.android.api.push.intents;

import android.content.Intent;

/**
 * Utility class that build the intent to bind the push notification service.
 */
public class BindPushNotificationServiceIntentBuilder {

    private static final String ACTION = "fr.cvlaminck.notidroid.push";

    public BindPushNotificationServiceIntentBuilder() {
    }

    public Intent build() {
        final Intent intent = new Intent(ACTION);
        return intent;
    }

}
