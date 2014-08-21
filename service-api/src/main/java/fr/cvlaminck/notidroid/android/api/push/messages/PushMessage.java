package fr.cvlaminck.notidroid.android.api.push.messages;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Base representation of a message that can be transferred through the push service.
 * Messages can contain a text, an event, ... Each type of message has its own
 * representation class, you need to use these classes to set/get the content of the
 * message.
 *
 * @since 0.2
 */
public abstract class PushMessage implements Parcelable {

    /**
     * Id of the application that has sent this message to the message broker.
     * 0 is reserved for messages used by notidroid.
     * When sending a message, you do not have to fulfill this field, the framework
     * will take care of this for you.
     *
     * @since 0.2
     */
    private long appId;

    /**
     * Id of the device that has sent this message to the message broker.
     * 0 if the message is sent by the cloudBackend.
     * When sending a message, you do not have to fulfill this field, the framework
     * will take care of this for you.
     *
     * @since 0.2
     */
    private long from;

    /**
     * Id of devices that should handle this message.
     * If empty, all devices connected with the end-user account must handle this message.
     * When sending a message, you do not have to fulfill this field, the framework
     * will take care of this for you.
     *
     * @since 0.2
     */
    private long to[] = new long[]{};

    public PushMessage() {
    }

    protected PushMessage(Parcel in) {
        appId = in.readLong();
        from = in.readLong();
        to = new long[in.readInt()];
        in.readLongArray(to);
    }

    public abstract ContentType getContentType();

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long[] getTo() {
        return to;
    }

    public void setTo(long[] to) {
        this.to = to;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getContentType().name());
        out.writeLong(appId);
        out.writeLong(from);
        out.writeInt(to.length);
        out.writeLongArray(to);
    }

    public static Creator<PushMessage> CREATOR = new Creator<PushMessage>() {
        @Override
        public PushMessage createFromParcel(Parcel in) {
            Class<?> messageClass = null;
            PushMessage message = null;
            switch (ContentType.valueOf(in.readString())) {
                case TEXT:
                    messageClass = TextPushMessage.class;
                    break;
                case EVENT:
                    //TODO
                    break;
            }
            try {
                Constructor constructor = messageClass.getDeclaredConstructor(Parcel.class);
                constructor.setAccessible(true);
                message = (PushMessage) constructor.newInstance(in);
                constructor.setAccessible(false);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return message;
        }

        @Override
        public PushMessage[] newArray(int size) {
            return new PushMessage[size];
        }
    };

    /**
     * Type of content that can be enclosed in a PushMessage.
     *
     * @since 0.2
     */
    public enum ContentType {
        /**
         * @since 0.2
         */
        TEXT,
        /**
         * @since 0.2
         */
        EVENT,
    }
}
