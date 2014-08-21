// CloudService.aidl
package fr.cvlaminck.notidroid.android.api.cloud;

import fr.cvlaminck.notidroid.android.api.applications.Application;

/**
 * @since 0.2
 */
interface CloudService {

    /**
     * Return the representation of the application hosting
     * the service and communicating with the cloud backend.
     * The application hosting all services always have an
     * id equals to 0.
     *
     * @since 0.2
     */
    Application getHostingApplication();

    /**
     * Return the representation of the application using the provided package name
     * on Android. Information are retrieved from the local cache or from
     * the cloud backend.
     * </p>
     * /!\ Uses networking. Must be called from background threads only.
     *
     * @param packageName Package name of the application we wants its information.
     * @param ignoreCache Force this operation to request the cloud backend.
     * @since 0.2
     */
    Application getApplicationByPackageName(in String packageName, in boolean ignoreCache);

    /**
     * Same as calling getApplicationByPackageName with the package name of the application
     * binding the cloud service.
     *
     * @since 0.2
     */
    Application getCurrentApplication(in boolean ignoreCache);

}
