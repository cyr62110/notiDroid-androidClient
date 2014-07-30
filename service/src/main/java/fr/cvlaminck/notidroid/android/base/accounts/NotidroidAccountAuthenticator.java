package fr.cvlaminck.notidroid.android.base.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;

import fr.cvlaminck.notidroid.android.base.activities.accounts.CreateOrLoginActivity_;
import fr.cvlaminck.notidroid.android.base.cloud.endpoints.OAuth2Endpoint;
import fr.cvlaminck.notidroid.android.base.cloud.endpoints.OAuth2Endpoint_;
import fr.cvlaminck.notidroid.cloud.client.api.oauth2.OAuth2AccessToken;

/**
 * AccountAuthenticator for notidroid cloudAccount.
 * This class handle all the authentication process for notidroid cloudAccount including
 * retrieving OAuth2 token.
 */
@EBean
public class NotidroidAccountAuthenticator
        extends AbstractAccountAuthenticator {

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
     * KEY_AUTHTOKEN.
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        //First, we wrap the account to get access to helper methods
        final NotidroidAccount notidroidAccount = NotidroidAccount.wrap(context, account);
        //We may have already retrieved a valid token, so we check that
        String accessToken = getStoredOAuth2TokenForAccount(notidroidAccount);
        //If we do not have a token or our token has expired, we request a new one using the password
        if(accessToken == null) {
            //We retrieve the password that is stored in the AccountManager
            final String password = notidroidAccount.getPassword();
            //Then we configure our endpoint to request the server where the account has been created.
            oAuth2Endpoint.setRootUrl(notidroidAccount.getOauth2AuthorizationServerUrl());
            //We also prepare our basic authentication to retrieve the token
            oAuth2Endpoint.setHttpBasicAuth(notidroidAccount.getEmail(), password);
            //Then we make the request
            try {
                final Date requestDate = new Date();
                final OAuth2AccessToken token = oAuth2Endpoint.getToken();
                accessToken = token.getAccessToken();
                storeOAuth2TokenForAccount(notidroidAccount, requestDate, token);
            } catch (HttpClientErrorException ex) {
                //TODO Handle error here
                throw new NetworkErrorException(ex);
            }
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

    /**
     * Retrieve the previous access token retrieved to authenticate the provided account on the server.
     * Returns null if the token has expired or the
     * <p/>
     * TODO : Enhance/Secure the way the token is stored
     *
     * @param account
     * @return
     */
    private String getStoredOAuth2TokenForAccount(NotidroidAccount account) {
        //For now the token is stored in account properties, not very secure
        final String token = accountManager.getUserData(account.getAccount(), "tk");
        final String sTokenExpirationDate = accountManager.getUserData(account.getAccount(), "tked");
        //Then we check the token validity
        if (token != null) {
            final Date tokenExpirationDate = new Date(Long.parseLong(sTokenExpirationDate));
            if(tokenExpirationDate.after(new Date()))
                return token;
        }
        return null;
    }

    /**
     * Store the token so it can be used without having to follow the full OAuth2 authentication procedure
     * again.
     *
     * @param account
     * @param requestDate Date when the Oauth2 request has been sent to the server
     * @param token Token resource retrieved from the server
     */
    private void storeOAuth2TokenForAccount(NotidroidAccount account, Date requestDate, OAuth2AccessToken token) {
        accountManager.setUserData(account.getAccount(), "tk", token.getAccessToken());
        accountManager.setUserData(account.getAccount(), "tked", Long.toString(requestDate.getTime() + token.getExpiresIn()));
    }


}
