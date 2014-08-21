package fr.cvlaminck.notidroid.android.api.applications;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Representation of an application synchronizing its notification using Notidroid or
 * using notidroid to push messages between user devices.
 *
 * @since 0.2
 */
public class Application
        implements Parcelable {

    /**
     * Identifier of the application. This identifier is given by the
     * server when the application is registered.
     *
     * @since 0.2
     */
    private long id;

    /**
     * Name of the application that is displayed to the
     * end-user.
     *
     * @since 0.2
     */
    private String displayName;

    public Application() {

    }

    private Application(Parcel in) {
        this.id = in.readLong();
        this.displayName = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.id);
        out.writeString(this.displayName);
    }

    public static final Creator<Application> CREATOR = new Creator<Application>() {
        @Override
        public Application createFromParcel(Parcel parcel) {
            return new Application(parcel);
        }

        @Override
        public Application[] newArray(int size) {
            return new Application[size];
        }
    };
}
