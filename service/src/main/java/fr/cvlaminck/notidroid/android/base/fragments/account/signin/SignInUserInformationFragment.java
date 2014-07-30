package fr.cvlaminck.notidroid.android.base.fragments.account.signin;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.fragments.account.AccountFragment;

/**
 * Fragment where the user can enter its personal information to
 * customize the service. We ask the bare minimum which is :
 * - first name
 * - last name
 * - email
 */
@EFragment(R.layout.signinuserinformationfragment)
public class SignInUserInformationFragment
        extends AccountFragment {
    private static final String TAG = SignInUserInformationFragment.class.getSimpleName();

    private UserInformationListener userInformationListener = null;

    @ViewById
    protected TextView txtFirstName;

    @ViewById
    protected TextView txtLastName;

    /**
     * First name of the user
     */
    @InstanceState
    protected String firstName;

    /**
     * Last name of the user
     */
    @InstanceState
    protected String lastName;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof UserInformationListener)
            userInformationListener = (UserInformationListener) activity;
        else
            Log.w(TAG, "Activity '" + activity.getClass().getSimpleName() + "' must implement '" + UserInformationListener.class.getSimpleName() + "'" +
                    " to receive events from the fragment");
    }

    @Override
    public void onStart() {
        super.onStart();
        txtFirstName.setText(firstName);
        txtLastName.setText(lastName);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.firstName = txtFirstName.getText().toString();
        this.lastName = txtLastName.getText().toString();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected void onNextClicked() {
        this.firstName = txtFirstName.getText().toString();
        this.lastName = txtLastName.getText().toString();
        //We send back information to our listener
        if(userInformationListener != null)
            userInformationListener.onUserInformationFulfilled(firstName, lastName);
    }

    @TextChange
    protected void txtFirstNameTextChanged(TextView textView) {
        validateForm();
    }

    @TextChange
    protected void txtLastNameTextChanged(TextView textView) {
        validateForm();
    }

    /**
     * Validate that all fields are not empty. If so enable the 'Next' button,
     * disable it.
     */
    private void validateForm() {
        if (txtFirstName.length() > 0 && txtLastName.length() > 0)
            btnNext.setEnabled(true);
        else
            btnNext.setEnabled(false);
    }

    public interface UserInformationListener {

        /**
         * Callback called when the user has fulfilled all the fields on the current
         * fragment and has clicked on the 'Next' button.
         *
         * @param firstName User's first name
         * @param lastName User's last name
         */
        public void onUserInformationFulfilled(String firstName, String lastName);

    }

}
