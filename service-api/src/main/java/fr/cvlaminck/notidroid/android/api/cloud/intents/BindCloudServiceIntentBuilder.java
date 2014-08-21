package fr.cvlaminck.notidroid.android.api.cloud.intents;

import android.content.Intent;

/**
 * TODO
 *
 * @since 0.2
 */
public class BindCloudServiceIntentBuilder {

    private static final String ACTION = "fr.cvlaminck.notidroid.cloud.api";

    public BindCloudServiceIntentBuilder() {
    }

    public Intent build() {
        final Intent intent = new Intent(ACTION);
        return intent;
    }

}
