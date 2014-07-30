package fr.cvlaminck.notidroid.android.base.services.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import fr.cvlaminck.notidroid.android.base.accounts.NotidroidAccountAuthenticator;

/**
 * Service providing the account authenticator for notidroid accounts
 * to the Android system.
 */
@EService
public class AccountAuthenticatorService
    extends Service {

    @Bean
    protected NotidroidAccountAuthenticator accountAuthenticator;

    @Override
    public IBinder onBind(Intent intent) {
        return accountAuthenticator.getIBinder();
    }

}
