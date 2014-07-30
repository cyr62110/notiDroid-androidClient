package fr.cvlaminck.notidroid.android.base.activities.accounts;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTunnelStepsStrip;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.fragments.NumberFragment;
import fr.cvlaminck.notidroid.cloud.client.api.devices.AndroidUserDeviceResource;

/**
 * Activity where the user can sign-in
 */
@EActivity(R.layout.loginactivity)
public class LoginActivity
    extends Activity {
    private final static String TAG = LoginActivity.class.getSimpleName();

    @ViewById
    protected ViewPager vpTunnel;

    @AfterViews
    protected void afterViews() {
        LoginFragmentPagerAdapter adapter = new LoginFragmentPagerAdapter(getFragmentManager());
        vpTunnel.setAdapter(adapter);

        final AndroidUserDeviceResource device = new AndroidUserDeviceResource();
        final DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        device.setScreenHeightInPixels(displayMetrics.heightPixels);
        device.setScreenWidthInPixels(displayMetrics.widthPixels);
        device.setScreenDensity(displayMetrics.densityDpi);
    }

    private class LoginFragmentPagerAdapter extends FragmentPagerAdapter {

        public LoginFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PagerTunnelStepsStrip.STEP_TITLE;
        }

        @Override
        public Fragment getItem(int position) {
            return new NumberFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
