package fr.cvlaminck.notidroid.android.base.extractors;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import org.androidannotations.annotations.EBean;

import fr.cvlaminck.notidroid.cloud.client.api.devices.AndroidUserDeviceResource;
import fr.cvlaminck.notidroid.cloud.client.api.devices.UserDeviceResource;

/**
 * Utility class used to extract information about the device running this app.
 */
@EBean
public class UserDeviceInfoExtractor {

    private Context context;

    public UserDeviceInfoExtractor(Context context) {
        this.context = context;
    }

    /**
     * Fulfill an UserDeviceResource with information about the
     * current device.
     *
     * @return an UserDeviceResource with information about this device
     */
    public UserDeviceResource extract() {
        final AndroidUserDeviceResource device = new AndroidUserDeviceResource();
        extractBuildInfo(device);
        extractScreenInfo(device);
        return device;
    }

    private void extractBuildInfo(AndroidUserDeviceResource device) {
        device.setBrand(Build.BRAND);
        device.setModel(Build.MODEL);
        device.setSerial(Build.SERIAL);
        device.setRelease(Build.VERSION.RELEASE);
        device.setSdkInt(Build.VERSION.SDK_INT);
    }

    private void extractScreenInfo(AndroidUserDeviceResource device) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        device.setScreenHeightInPixels(displayMetrics.heightPixels);
        device.setScreenWidthInPixels(displayMetrics.widthPixels);
        device.setScreenDensity(displayMetrics.densityDpi);
    }

}
