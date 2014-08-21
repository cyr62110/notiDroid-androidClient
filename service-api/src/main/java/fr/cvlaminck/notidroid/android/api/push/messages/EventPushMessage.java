package fr.cvlaminck.notidroid.android.api.push.messages;

import android.os.Parcel;

/**
 * Representation of a PushMessage containing a server-sided-event.
 */
public class EventPushMessage
    extends PushMessage{

    public EventPushMessage() {
    }

    protected EventPushMessage(Parcel in) {
        super(in);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.EVENT;
    }
}
