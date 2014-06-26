package fr.cvlaminck.notidroid.android.base.fragments.account.signin;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.Spinner;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.adapters.lists.KnownServerAdapter;
import fr.cvlaminck.notidroid.android.base.data.assets.config.KnownServer;

/**
 * Fragment where the user can select the server he will join to sync.
 * its notifications
 */
@EFragment(R.layout.signinserverselectionfragment)
public class SignInServerSelectionFragment
    extends Fragment {

    private KnownServerAdapter knownServerAdapter;

    @InstanceState
    protected int selectedServerIndex = -1;

    @InstanceState
    protected String customServerUrl = null;

    @ViewById
    protected Spinner spServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void afterViews() {
        knownServerAdapter = new KnownServerAdapter(this.getActivity());
        spServer.setAdapter(knownServerAdapter);
        //If we have saved a selection in our state, we restore it
        if(selectedServerIndex != -1)
            spServer.setSelection(selectedServerIndex);
    }

    @ItemSelect
    protected void spServerSelected(boolean selected, int position) {
        if(selected) {
            //If the user has selected the custom entry, we display the custom layout

        }
    }



}
