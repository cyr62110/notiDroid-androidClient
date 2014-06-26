package fr.cvlaminck.notidroid.android.base.data.assets.config;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * JSON representation of an online server hosting Notidroid cloud backend.
 * All known servers are written in the knownServers.json file and will be
 * displayed when an user creates its account so he can register on one of them.
 */
public class KnownServer
    implements Serializable {

    /**
     * Name of the server. This is the public name of the server
     * that is displayed when the user goes on the web interface
     * of the server.
     */
    private String serverName;

    /**
     * URL of the notidroid server. This address will be displayed to the user
     * so its highly recommended to make it point on the web front.
     */
    private String serverUrl;

    /**
     * URL of the REST api exposed by the notidroid server. This address is never displayed
     * to the user.
     */
    private String apiUrl;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
