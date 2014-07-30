package fr.cvlaminck.notidroid.android.base.activities.accounts;

import android.app.Activity;
import android.support.v4.view.ViewPager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import fr.cvlaminck.notidroid.android.api.accounts.NotidroidCloudAccount;
import fr.cvlaminck.notidroid.android.api.accounts.ServerInformation;
import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.adapters.pager.TunnelFragmentPagerAdapter;
import fr.cvlaminck.notidroid.android.base.fragments.account.AccountFragment;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInAccountCreationFragment;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInAccountCreationFragment_;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInCredentialsFragment;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInCredentialsFragment_;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInServerSelectionFragment;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInServerSelectionFragment_;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInUserInformationFragment;
import fr.cvlaminck.notidroid.android.base.fragments.account.signin.SignInUserInformationFragment_;

/**
 *
 */
@EActivity(R.layout.signinactivity)
public class SignInActivity
        extends Activity
        implements AccountFragment.AccountFragmentStateListener,
        SignInServerSelectionFragment.ServerSelectionListener,
        SignInUserInformationFragment.UserInformationListener,
        SignInCredentialsFragment.CredentialsListener,
        SignInAccountCreationFragment.AccountCreationStateListener {

    /**
     * Object that will store user information that we are gathering
     * through the tunnel.
     */
    @InstanceState
    protected NotidroidCloudAccount userAccount = null;

    /**
     * Password that must be used to access to the user's data
     * on the cloud backend.
     */
    @InstanceState
    protected String password = null;

    @ViewById
    protected ViewPager vpTunnel;

    private TunnelFragmentPagerAdapter pagerAdapter = null;

    @AfterViews
    protected void afterViews() {
        //If we have not retrieved the userAccount in the instance state, we create a new one
        if(userAccount == null)
            userAccount = new NotidroidCloudAccount();
        //We setup our tunnel
        pagerAdapter = new TunnelFragmentPagerAdapter(getFragmentManager());
        pagerAdapter.addStep(SignInServerSelectionFragment_.class);
        pagerAdapter.addStep(SignInUserInformationFragment_.class);
        pagerAdapter.addSubStep(SignInCredentialsFragment_.class);
        pagerAdapter.addStep(SignInAccountCreationFragment_.class);
        //And we set it into our ViewPager
        vpTunnel.setAdapter(pagerAdapter);
        vpTunnel.setOnPageChangeListener(onPageChangeListener);
    }

    @Override
    public void onNextClicked() {
        //If it is not the last step of the tunnel, we navigate to the next step
        if(vpTunnel.getCurrentItem() != pagerAdapter.getCount() - 1) {
            vpTunnel.setCurrentItem(vpTunnel.getCurrentItem() + 1, true);
        }
    }

    @Override
    public void onServerSelection(String serverName, String apiUrl) {
        final ServerInformation serverInformation = new ServerInformation();
        serverInformation.setPublicName(serverName);
        serverInformation.setApiUrl(apiUrl);
        userAccount.setServer(serverInformation);
    }

    @Override
    public void onUserInformationFulfilled(String firstName, String lastName) {
        userAccount.setFirstName(firstName);
        userAccount.setLastName(lastName);
    }

    @Override
    public void onCredentialsFulfilled(String email, String password) {
        userAccount.setEmail(email);
        this.password = password;
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            //If the last page is displayed, we must launch the account creation procedure.
            if(i == pagerAdapter.getCount() - 1) {
                final SignInAccountCreationFragment fragment = (SignInAccountCreationFragment) pagerAdapter.getFragmentAt(i);
                fragment.startRegistration(userAccount, password);
                //TODO : We must lock the navigation
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    @Override
    public void onAccountCreationStarted() {
        //TODO : lock the ViewPager and the back button
    }

    @Override
    public void onAccountCreationFailed() {
        //TODO : release all lock on the ViewPager and the back button
    }

    @Override
    public void onUserMustLogin(String email) {
        //The user wants to use the login page instead of the registration one, so we send it back
        //to the right fragment
        //TODO
    }

    @Override
    public void onUserMustSelectAnotherServer() {
        //The user wants to change the server he has selected, so we send it back
        //to the right fragment
        //TODO
    }

    @Override
    public void onUserMustChangeCredentials() {
        //The user wants to change its password, so we send it back
        //to the right fragment
        //TODO
    }

    @Override
    public void onAccountCreationFinished() {
        //We launch the notidroid application where the user
        //can use its account.
        //TODO
    }
}
