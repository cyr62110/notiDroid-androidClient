package fr.cvlaminck.notidroid.android.api.push.callbacks;

import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;

/**
 *
 *
 * @since 0.2
 */
interface OnPushMessageReceivedCallback {

    void onPushMessageReceived(in PushMessage message);

}
