package fr.cvlaminck.notidroid.android.api.accounts;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Information on the notidroid cloud backend where the user
 * has created its account.
 */
public class ServerInformation
    implements Parcelable {

    /**
     * Public name of the server.
     */
    private String publicName;

    /**
     * Url pointing to the root of the REST client API of the notidroid cloud backend.
     */
    private String apiUrl;

    public ServerInformation() {
    }

    private ServerInformation(Parcel in) {
        this.publicName = in.readString();
        this.apiUrl = in.readString();
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Return an url pointing to the root of the oauth2 authorization server.
     * Oauth2 endpoints are not under the API root url.
     */
    public String getOAuth2AuthorizationServerUrl() {
        if(apiUrl == null || apiUrl.length() == 0)
            return null;
        return apiUrl.substring(0, apiUrl.length() - (apiUrl.endsWith("/")?4:3)) + "oauth";
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(publicName);
        parcel.writeString(apiUrl);
    }

    public static final Parcelable.Creator<ServerInformation> CREATOR
            = new Parcelable.Creator<ServerInformation>() {
        public ServerInformation createFromParcel(Parcel in) {
            return new ServerInformation(in);
        }

        public ServerInformation[] newArray(int size) {
            return new ServerInformation[size];
        }
    };
}
