package fr.cvlaminck.notidroid.android.base.fragments.account;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.widget.Button;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Base class for fragments used in the sign-in/log-in tunnel.
 */
@EFragment
public abstract class AccountFragment
    extends Fragment {
    private static final String TAG = AccountFragment.class.getSimpleName();

    private AccountFragmentStateListener accountFragmentStateListener = null;

    @ViewById
    protected Button btnNext = null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if((activity instanceof AccountFragmentStateListener)) {
            accountFragmentStateListener = (AccountFragmentStateListener) activity;
        } else
            Log.w(TAG, "Activity '" + activity.getClass().getSimpleName() + "' must implement '" + AccountFragmentStateListener.class.getSimpleName() + "'" +
                    " to receive events from the fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        accountFragmentStateListener = null;
    }

    @Click
    protected void btnNextClicked() {
        onNextClicked();
        if(accountFragmentStateListener != null)
            accountFragmentStateListener.onNextClicked();
    }

    /**
     * Function called when the next button is clicked by the
     * user. Concrete classes must override this function to
     * send information fulfilled by the user back to the
     * activity
     */
    protected abstract void onNextClicked();


    /**
     * Listener that must be used by the activity to follow user interactions
     * with the fragment.
     */
    public interface AccountFragmentStateListener {

        /**
         * Callback called when the user click on the 'next' button in one
         * of the fragments of the sign-in/login tunnel.
         */
        public void onNextClicked();

    }

}
