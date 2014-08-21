package fr.cvlaminck.notidroid.android.api.push.callbacks;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Options that can be applied when you register an OnPushMessageReceivedCallback
 * on the PushNotificationService.
 *
 * @since 0.2
 */
public class OnPushMessageReceivedCallbackOptions implements Parcelable {

    public OnPushMessageReceivedCallbackOptions() {
    }

    public OnPushMessageReceivedCallbackOptions(Parcel in) {
    }

    /**
     * Return the default options used when registering a OnPushMessageReceivedCallback on the
     * push notification service.
     */
    public static OnPushMessageReceivedCallbackOptions defaultOptions() {
        final OnPushMessageReceivedCallbackOptions options = new OnPushMessageReceivedCallbackOptions();
        return options;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

    }

    public static final Creator<OnPushMessageReceivedCallbackOptions> CREATOR = new Creator<OnPushMessageReceivedCallbackOptions>() {
        @Override
        public OnPushMessageReceivedCallbackOptions createFromParcel(Parcel parcel) {
            return new OnPushMessageReceivedCallbackOptions(parcel);
        }

        @Override
        public OnPushMessageReceivedCallbackOptions[] newArray(int size) {
            return new OnPushMessageReceivedCallbackOptions[size];
        }
    };
}
