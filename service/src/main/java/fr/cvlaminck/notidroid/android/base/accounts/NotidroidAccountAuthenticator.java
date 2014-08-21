package fr.cvlaminck.notidroid.android.base.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;

import fr.cvlaminck.notidroid.android.base.activities.accounts.CreateOrLoginActivity_;
import fr.cvlaminck.notidroid.android.base.cloud.endpoints.OAuth2Endpoint;
import fr.cvlaminck.notidroid.cloud.client.api.oauth2.OAuth2AccessToken;

/**
 * AccountAuthenticator for notidroid cloudAccount.
 * This class handle all the authentication process for notidroid cloudAccount including
 * retrieving OAuth2 token.
 */
@EBean
public class NotidroidAccountAuthenticator
        extends AbstractAccountAuthenticator {
    private final static boolean DEBUG = true;
    private final static String TAG = NotidroidAccountAuthenticator.class.getSimpleName();

    @RestService
    protected OAuth2Endpoint oAuth2Endpoint;

    private Context context = null;

    private AccountManager accountManager = null;

    public NotidroidAccountAuthenticator(Context context) {
        super(context);
        this.context = context;
        this.accountManager = AccountManager.get(context);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] features, Bundle options) throws NetworkErrorException {
        //Account creation is done through a dedicated activity. So we return an intent in the result bundle.
        final Intent createOrLoginIntent = CreateOrLoginActivity_.intent(context).get();
        final Bundle resultBundle = new Bundle();
        resultBundle.putParcelable(AccountManager.KEY_INTENT, createOrLoginIntent);
        return resultBundle;
    }

    /**
     * Since we are using the OAuth2 protocol, this method will return the OAuth2 Access Token in the Bundle using the
     * KEY_AUTHTOKEN. Also tokens are invalidated by clients when they receive an auth error from servers (cloudBackend, message broker, etc...) so
     * this function does not need to handle token expiration nor token caching.
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        //First, we wrap the account to get access to helper methods
        final NotidroidAccount notidroidAccount = NotidroidAccount.wrap(context, account);
        final String accessToken;
        //We retrieve the password that is stored in the AccountManager
        final String password = notidroidAccount.getPassword();
        //Then we configure our endpoint to request the server where the account has been created.
        oAuth2Endpoint.setRootUrl(notidroidAccount.getOauth2AuthorizationServerUrl());
        //We also prepare our basic authentication to retrieve the token
        oAuth2Endpoint.setHttpBasicAuth(notidroidAccount.getEmail(), password);
        //Then we make the request to get an auth token.
        try {
            final OAuth2AccessToken token = oAuth2Endpoint.getToken();
            accessToken = token.getAccessToken();
        } catch (HttpClientErrorException ex) {
            //TODO Handle error here
            throw new NetworkErrorException(ex);
        }

        final Bundle resultBundle = new Bundle();
        resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        resultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        resultBundle.putString(AccountManager.KEY_AUTHTOKEN, accessToken);
        return resultBundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String newCredentials, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        //All user cloudAccount are the same. They are no difference between two
        //cloudAccount, so we do not require any features.
        return null;
    }

}
