package fr.cvlaminck.notidroid.android.base.fragments.account.signin;

import android.app.Activity;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.fragments.account.AccountFragment;

/**
 * Fragment where the user can provide its email address and its password that
 * will be used as its credentials.
 */
@EFragment(R.layout.signincredentialsfragment)
public class SignInCredentialsFragment
        extends AccountFragment {
    private final static String TAG = SignInCredentialsFragment.class.getSimpleName();

    private CredentialsListener credentialsListener = null;

    @ViewById
    protected TextView lblSubTitle;

    @ViewById
    protected EditText txtEmail;

    @ViewById
    protected EditText txtPassword;

    @ViewById
    protected EditText txtRetypePassword;

    @InstanceState
    protected String email = null;

    @InstanceState
    protected String password = null;

    @InstanceState
    protected String retypedPassword = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CredentialsListener)
            credentialsListener = (CredentialsListener) activity;
        else
            Log.w(TAG, "Activity '" + activity.getClass().getSimpleName() + "' must implement '" + CredentialsListener.class.getSimpleName() + "'" +
                    " to receive events from the fragment");
    }

    @Override
    public void onStart() {
        super.onStart();
        txtEmail.setText(email);
        txtPassword.setText(password);
        txtRetypePassword.setText(retypedPassword);
    }

    @Override
    public void onStop() {
        super.onStop();
        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();
        retypedPassword = txtRetypePassword.getText().toString();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        credentialsListener = null;
    }

    @TextChange
    protected void txtEmailTextChanged(TextView textView) {
        validateForm();
    }

    @TextChange
    protected void txtPasswordTextChanged(TextView textView) {
        validateForm();
    }

    @TextChange
    protected void txtRetypePasswordTextChanged(TextView textView) {
        validateForm();
    }

    @Override
    protected void onNextClicked() {
        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();
        if (credentialsListener != null)
            credentialsListener.onCredentialsFulfilled(email, password);
    }

    private void validateForm() {
        boolean btnNextShouldBeEnabled = true;
        txtRetypePassword.setError(null);
        txtEmail.setError(null);

        if (txtEmail.length() > 0) {
            //We check if the email follow the pattern of a valid email address
            if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail.getText()).matches())
                txtEmail.setError(this.getString(R.string.signincredentialsfragment_txtEmail_error));
        } else
            btnNextShouldBeEnabled = false;

        if(txtPassword.length() > 0) {
            //We should check that the password is only made of valid character
            //TODO
        } else
            btnNextShouldBeEnabled = false;

        if (txtPassword.length() > 0 && txtRetypePassword.length() > 0) {
            //We check if both passwords matches
            final String password = txtPassword.getText().toString();
            final String retypedPassword = txtRetypePassword.getText().toString();
            if (!password.equals(retypedPassword)) {
                btnNextShouldBeEnabled = false;
                //We display an error message if the second password has the same length or is
                //longer than the original one
                if (txtRetypePassword.length() >= txtPassword.length())
                    txtRetypePassword.setError(this.getString(R.string.signincredentialsfragment_txtRetypePassword_error));
            }
        } else
            btnNextShouldBeEnabled = false;
        btnNext.setEnabled(btnNextShouldBeEnabled);
    }

    public interface CredentialsListener {

        /**
         * Callback called when the user has filled all fields included in this fragment and has
         * clicked on the 'Next' button.
         *
         * @param email    User's email
         * @param password User's password
         */
        public void onCredentialsFulfilled(String email, String password);

    }

}
