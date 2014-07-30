package fr.cvlaminck.notidroid.android.api.accounts;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * An account created by the user on a notidroid cloud backend
 * instance. This account will allow the user to use cloud capacities
 * of the notidroid project.
 */
public class NotidroidCloudAccount
    implements Parcelable {

    /**
     * First name of the user
     */
    private String firstName;

    /**
     * Last name of the user
     */
    private String lastName;

    /**
     * Email of the user. Also used as login to access the API.
     */
    private String email;

    /**
     * Server on which this account has been registered.
     */
    private ServerInformation server;

    public NotidroidCloudAccount() {
    }

    private NotidroidCloudAccount(Parcel in) {
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.email = in.readString();
        this.server = in.readParcelable(ServerInformation.class.getClassLoader());
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ServerInformation getServer() {
        return server;
    }

    public void setServer(ServerInformation server) {
        this.server = server;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(firstName);
        out.writeString(lastName);
        out.writeString(email);
        out.writeParcelable(server, flags);
    }
}
