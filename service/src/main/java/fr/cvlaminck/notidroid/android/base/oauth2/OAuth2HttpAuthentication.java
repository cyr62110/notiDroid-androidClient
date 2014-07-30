package fr.cvlaminck.notidroid.android.base.oauth2;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpAuthentication;

/**
 * HttpAuthentication implementation that must be used to access parts of the
 * server API that require authentication.
 */
public class OAuth2HttpAuthentication
    extends HttpAuthentication {

    private String accessToken = null;

    public OAuth2HttpAuthentication(@NotNull String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String getHeaderValue() {
        return "Bearer " + accessToken;
    }

}
