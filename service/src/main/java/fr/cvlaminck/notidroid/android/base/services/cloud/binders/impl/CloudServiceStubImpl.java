package fr.cvlaminck.notidroid.android.base.services.cloud.binders.impl;

import android.os.RemoteException;

import fr.cvlaminck.notidroid.android.api.applications.Application;
import fr.cvlaminck.notidroid.android.api.cloud.CloudService;
import fr.cvlaminck.notidroid.android.base.services.cloud.NotidroidCloudService;

/**
 *
 */
public class CloudServiceStubImpl
    extends CloudService.Stub {
    private final static boolean DEBUG = true;
    private final static String TAG = CloudServiceStubImpl.class.getSimpleName();

    private NotidroidCloudService cloudService = null;

    public CloudServiceStubImpl(NotidroidCloudService cloudService) {
        this.cloudService = cloudService;
    }

    @Override
    public Application getHostingApplication() throws RemoteException {
        return cloudService.getHostingApplication();
    }

    @Override
    public Application getApplicationByPackageName(String packageName, boolean ignoreCache) throws RemoteException {
        return cloudService.getApplicationByPackageName(packageName, ignoreCache);
    }

    @Override
    public Application getCurrentApplication(boolean ignoreCache) throws RemoteException {
        return null;
    }
}
