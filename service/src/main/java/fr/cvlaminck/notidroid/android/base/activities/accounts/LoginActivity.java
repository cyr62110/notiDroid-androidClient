package fr.cvlaminck.notidroid.android.base.activities.accounts;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.fragments.NumberFragment;

/**
 * Activity where the user can sign-in
 */
@EActivity(R.layout.loginactivity)
public class LoginActivity
    extends Activity {

    @ViewById
    protected ViewPager vpTunnel;

    @AfterViews
    protected void afterViews() {
        LoginFragmentPagerAdapter adapter = new LoginFragmentPagerAdapter(getFragmentManager());
        vpTunnel.setAdapter(adapter);
    }

    private class LoginFragmentPagerAdapter extends FragmentPagerAdapter {

        public LoginFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new NumberFragment();
        }

        @Override
        public int getCount() {
            return 10;
        }
    }

}
