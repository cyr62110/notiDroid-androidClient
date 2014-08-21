package fr.cvlaminck.notidroid.android.api.push.messages;

import android.os.Parcel;

/**
 * Representation of PushMessage containing text.
 *
 * @since 0.2
 */
public class TextPushMessage
    extends PushMessage {

    /**
     * Content of the message.
     *
     * @since 0.2
     */
    private String text = null;

    public TextPushMessage() {
    }

    protected TextPushMessage(Parcel in) {
        super(in);
        this.text = in.readString();
    }

    @Override
    public ContentType getContentType() {
        return ContentType.TEXT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(text);

    }
}
