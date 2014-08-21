package fr.cvlaminck.notidroid.android.base.utils;

import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.util.Log;

import org.androidannotations.annotations.EBean;

/**
 * Class containing utility methods for Stub implementation.
 */
@EBean
public class StubImplUtils {
    private final static String TAG = StubImplUtils.class.getSimpleName();
    private final static boolean DEBUG = true;

    private PackageManager packageManager = null;

    public StubImplUtils(Context context) {
        this.packageManager = context.getPackageManager();
    }

    /**
     * Return the package name of the application binding the service. Only works
     * for service using AIDL to describe their interface.
     */
    public String getCallingPackageName(int callingUid) {
        final String packageName = packageManager.getNameForUid(callingUid);
        if(DEBUG)
            Log.d(TAG, String.format("Returning calling package name '%s' associated with uid %d", packageName, callingUid));
        return packageName;
    }

}
