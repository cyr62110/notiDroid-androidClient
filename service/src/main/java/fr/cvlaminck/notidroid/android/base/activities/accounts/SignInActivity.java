package fr.cvlaminck.notidroid.android.base.activities.accounts;

import android.app.Activity;
import android.support.v4.view.ViewPager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.adapters.pager.TunnelFragmentPagerAdapter;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInCredentialsFragment_;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInServerSelectionFragment_;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInUserInformationFragment_;

/**
 *
 */
@EActivity(R.layout.signinactivity)
public class SignInActivity
        extends Activity {

    @ViewById
    protected ViewPager vpTunnel;

    @AfterViews
    protected void afterViews() {
        //We setup our tunnel
        final TunnelFragmentPagerAdapter adapter = new TunnelFragmentPagerAdapter(getFragmentManager());
        adapter.addStep(SignInServerSelectionFragment_.class);
        adapter.addStep(SignInUserInformationFragment_.class);
        adapter.addSubStep(SignInCredentialsFragment_.class);
        //And we set it into our ViewPager
        vpTunnel.setAdapter(adapter);
    }

}
