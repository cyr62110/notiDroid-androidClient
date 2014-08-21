package fr.cvlaminck.notidroid.android.base.services.cloud;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;

import fr.cvlaminck.notidroid.android.api.applications.Application;
import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.accounts.NotidroidAccount;
import fr.cvlaminck.notidroid.android.base.services.cloud.binders.impl.CloudServiceStubImpl;

/**
 * Android service exposing all functions of the notidroid cloud backend to
 * all Android applications. This service is also used by this application
 * to synchronize user notifications across its devices.
 */
@EService
public class NotidroidCloudService
        extends Service {
    private final static String TAG = NotidroidCloudService.class.getSimpleName();
    private final static boolean DEBUG = true;

    private PackageManager packageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        this.packageManager = this.getBaseContext().getPackageManager();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new CloudServiceStubImpl(this);
    }

    private NotidroidAccount getAccount() {
        final NotidroidAccount account = NotidroidAccount.findOne(this);
        if (account == null)
            throw new IllegalStateException("No account available on this device.");
        if (account.getDeviceId() == null)
            throw new IllegalStateException("Account registration was aborted during the process. No device id associated to this device.");
        return account;
    }

    /**
     * Returns information about this service including the package name
     * of the application hosting this service.
     */
    private PackageItemInfo getCurrentServiceInfo() {
        final ComponentName currentServiceName = new ComponentName(this, NotidroidCloudService_.class);
        PackageItemInfo currentServiceInfo = null;
        try {
            currentServiceInfo = packageManager.getServiceInfo(currentServiceName, 0);
        } catch (PackageManager.NameNotFoundException e) { /* Should never happen since if this code run the application is installed */ }
        return currentServiceInfo;
    }

    public Application getHostingApplication() {
        final Application app = new Application();
        app.setId(0);
        app.setDisplayName(getString(R.string.app_name));
        return app;
    }

    public Application getApplicationByPackageName(String packageName, boolean ignoreCache) {
        Application application = null;
        if(packageName == null || packageName.length() == 0)
            throw new IllegalArgumentException("packageName must not be empty or null");
        if (DEBUG)
            Log.d(TAG, String.format("Called getApplicationByPackageName with parameters {packageName='%s'; ignoreCache=%b}",
                    packageName, ignoreCache));
        final String currentPackageName = getCurrentServiceInfo().packageName;
        if(packageName.equals(currentPackageName)) {
            if(DEBUG)
                Log.d(TAG, "Application package name matching this app package name. Returning hosting application information.");
            application = getHostingApplication();
        } else {
            //Retrieve application info from cache
            //If not available or ignoreCache, request the server using the REST interface.
            //TODO for now
            application = getHostingApplication();
        }
        return application;
    }

}
