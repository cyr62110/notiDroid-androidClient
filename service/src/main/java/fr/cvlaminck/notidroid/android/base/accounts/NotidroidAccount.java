package fr.cvlaminck.notidroid.android.base.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpAuthentication;

import java.io.IOException;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.oauth2.OAuth2HttpAuthentication;

/**
 * Helper class helping developer to manipulate Account created
 * by this application.
 * <p/>
 * Notidroid uses Account user data to store more information
 * like the url of the server where the user has registered its online
 * cloudAccount, the first name and the last name of the user, etc...
 * This class will help you to manipulate those data.
 */
public class NotidroidAccount {

    /**
     * Type of the auth token used by notidroid
     */
    private final static String AUTH_TOKEN_TYPE = "oauth2";

    /**
     * Url pointing to the root of the notidroid REST api.
     */
    private final static String KEY_API_URL = "api";

    /**
     * Url pointing to the root of the REST api of the oauth2
     * authorization server.
     */
    private final static String KEY_OAUTH2_URL = "oauth2";

    /**
     * Id of the cloud cloudAccount related to this local cloudAccount.
     */
    private final static String KEY_ONLINE_ID = "id";

    /**
     * First name of the cloudAccount owner.
     */
    private final static String KEY_FIRST_NAME = "fn";

    /**
     * Last name of the cloudAccount owner.
     */
    private final static String KEY_LAST_NAME = "ln";

    /**
     * Online id associated with this device.
     */
    private final static String KEY_DEVICE_ID = "did";

    private AccountManager accountManager = null;

    private Account account = null;

    NotidroidAccount(@NotNull Context context, @NotNull Account account) {
        this.account = account;
        this.accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    /**
     * Wrap an existing android Account into a NotidroidAccount object so
     * it can be more easily manipulated.
     *
     * @param context a android context
     * @param account an cloudAccount created by notidroid
     * @return a wrapped cloudAccount with helper methods
     */
    public static NotidroidAccount wrap(@NotNull Context context, @NotNull Account account) {
        final String notidroidAccountType = context.getString(R.string.accountType);
        if (!notidroidAccountType.equals(account.type))
            throw new IllegalStateException("This helper class can only wrap account of type '" + notidroidAccountType + "'");
        return new NotidroidAccount(context, account);
    }

    /**
     * Create a new cloudAccount on the device using user credentials.
     *
     * @param context
     * @param email     User email
     * @param password  Password that we will use to authenticate our user on the cloud backend.
     * @param apiUrl    Url of the server where the user has registered its cloudAccount, must point to the root of the REST api.
     * @param oauth2Url Url of the Oauth2 authorization server that will be used to authenticates our user.
     * @param onlineId  Id of the online cloudAccount.
     * @return a wrapped cloudAccount with helper methods
     */
    public static NotidroidAccount create(@NotNull Context context, @NotNull String email, String password,
                                          @NotNull String apiUrl, @NotNull String oauth2Url, long onlineId) {
        final String notidroidAccountType = context.getString(R.string.accountType);
        final AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        final Bundle userData = new Bundle();
        userData.putString(KEY_API_URL, apiUrl);
        userData.putString(KEY_OAUTH2_URL, oauth2Url);
        userData.putLong(KEY_ONLINE_ID, onlineId);

        final Account account = new Account(email, notidroidAccountType);
        if (!accountManager.addAccountExplicitly(account, password, userData))
            return null;
        else
            return NotidroidAccount.wrap(context, account);
    }

    /**
     * Find the account associated with the provided email address, return null if none.
     *
     * @param context
     * @param email   User email address
     * @return a wrapped account with helper methods
     */
    public static NotidroidAccount find(@NotNull Context context, @NotNull String email) {
        final String notidroidAccountType = context.getString(R.string.accountType);
        final AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account account = null;
        final Account[] accounts = accountManager.getAccountsByType(notidroidAccountType);
        for (Account a : accounts) {
            if (a.name.equals(email)) {
                account = a;
                break;
            }
        }

        return (account != null) ? NotidroidAccount.wrap(context, account) : null;
    }

    /**
     * Return the cloudAccount wrapped by this helper class.
     */
    public Account getAccount() {
        return account;
    }

    public String getEmail() {
        return account.name;
    }

    public String getPassword() {
        return accountManager.getPassword(account);
    }

    public String getFirstName() {
        return accountManager.getUserData(account, KEY_FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        accountManager.setUserData(account, KEY_FIRST_NAME, firstName);
    }

    public String getLastName() {
        return accountManager.getUserData(account, KEY_LAST_NAME);
    }

    public void setLastName(String lastName) {
        accountManager.setUserData(account, KEY_LAST_NAME, lastName);
    }

    public Long getDeviceId() {
        final String sDeviceId = accountManager.getUserData(account, KEY_DEVICE_ID);
        return (sDeviceId != null) ? Long.parseLong(sDeviceId) : null;
    }

    public void setDeviceId(long deviceId) {
        accountManager.setUserData(account, KEY_DEVICE_ID, Long.toString(deviceId));
    }

    public String getCloudAPIUrl() {
        return accountManager.getUserData(account, KEY_API_URL);
    }

    public String getOauth2AuthorizationServerUrl() {
        return accountManager.getUserData(account, KEY_OAUTH2_URL);
    }

    public Long getOnlineId() {
        Long id = null;
        final String sOnlineId = accountManager.getUserData(account, KEY_ONLINE_ID);
        if (sOnlineId != null)
            id = Long.valueOf(Long.parseLong(sOnlineId));
        return id;
    }

    /**
     * Return an HttpAuthentication that can be used to access the part of the online
     * that requires an authentication.
     *
     * @return an HttpAuthentication with
     */
    public HttpAuthentication blockingGetHttpAuthentication() throws AuthenticatorException, OperationCanceledException, IOException {
        final String accessToken = accountManager.blockingGetAuthToken(account, AUTH_TOKEN_TYPE, true);
        return (accessToken != null) ? new OAuth2HttpAuthentication(accessToken) : null;
    }

}
