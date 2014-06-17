package fr.cvlaminck.notidroid.android.base.data.assets.config;

/**
 * JSON representation of an online server hosting Notidroid cloud backend.
 * All known servers are written in the knownServers.json file and will be
 * displayed when an user creates its account so he can register on one of them.
 */
public class KnownServer {

    /**
     * Name of the server. This is the public name of the server
     * that is displayed when the user goes on the web interface
     * of the server.
     */
    private String serverName;

    /**
     * URL pointing to the root of the notidroid client REST API
     * that is used to communicate with the server.
     */
    private String serverUrl;

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
}
