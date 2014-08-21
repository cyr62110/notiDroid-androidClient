package fr.cvlaminck.notidroid.android.base.services.push.listeners;

import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;

import org.jetbrains.annotations.NotNull;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.accounts.NotidroidAccount;
import fr.cvlaminck.notidroid.android.base.services.push.NotidroidPushNotificationService;

/**
 *
 */
public class OnAccountsUpdateListenerImpl
    implements OnAccountsUpdateListener{

    private String accountType = null;

    private NotidroidPushNotificationService pushNotificationService = null;

    public OnAccountsUpdateListenerImpl(@NotNull NotidroidPushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
        this.accountType = pushNotificationService.getString(R.string.accountType);
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        final NotidroidAccount account = NotidroidAccount.findOne(pushNotificationService);
        if(account != null && account.getDeviceId() != null)
            pushNotificationService.connect();
    }
}
