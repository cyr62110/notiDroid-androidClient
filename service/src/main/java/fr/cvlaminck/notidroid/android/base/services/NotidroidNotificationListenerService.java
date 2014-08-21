package fr.cvlaminck.notidroid.android.base.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.androidannotations.annotations.EService;

/**
 *
 * @since 0.2
 */
@EService
public class NotidroidNotificationListenerService
    extends NotificationListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }

}
