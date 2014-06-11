package fr.cvlaminck.notidroid.android.base.activities.accounts;

import android.app.Activity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import fr.cvlaminck.notidroid.android.base.R;

/**
 * Activity that ask if the user wants to create a new online account
 * or log-in with an existing one.
 */
@EActivity(R.layout.createorloginactivity)
public class CreateOrLoginActivity
    extends Activity {

    @Click
    protected void btnSignUpClicked() {

    }

    @Click
    protected void btnLogInClicked() {
        LoginActivity_.intent(this).start();
    }

}
