package fr.cvlaminck.notidroid.android.base.fragments.account.signin;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.api.BackgroundExecutor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.client.RestClientException;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.adapters.lists.KnownServerAdapter;
import fr.cvlaminck.notidroid.android.base.cloud.endpoints.ServerInformationEndpoint;
import fr.cvlaminck.notidroid.android.base.data.assets.config.KnownServer;
import fr.cvlaminck.notidroid.android.base.fragments.account.AccountFragment;
import fr.cvlaminck.notidroid.cloud.client.api.NotidroidClientAPI;
import fr.cvlaminck.notidroid.cloud.client.api.servers.ServerInformationResource;

/**
 * Fragment where the user can select the server he will join to sync.
 * its notifications
 */
@EFragment(R.layout.signinserverselectionfragment)
public class SignInServerSelectionFragment
    extends AccountFragment {
    private final static String TAG = SignInServerSelectionFragment.class.getSimpleName();

    private KnownServerAdapter knownServerAdapter;

    private ServerSelectionListener serverSelectionListener = null;

    @RestService
    protected ServerInformationEndpoint serverInformationEndpoint;

    /**
     * Position of the server selected by the user in the list.
     * This position may also point to the 'Custom' entry in the list.
     */
    @InstanceState
    protected int selectedServerIndex = -1;

    /**
     * Name of the server selected by the user.
     */
    @InstanceState
    protected String serverName = null;

    /**
     * URL of the notidroid cloud backend that the user has chosen.
     * The url can be one of a know server or a custom one written by the user.
     */
    @InstanceState
    protected String serverUrl = null;

    /**
     * Do the server supports the API used by this client
     * application ?
     */
    @InstanceState
    protected boolean doesSupportCurrentAPIVersion = false;

    @ViewById
    protected TextView txtErrorMessage;

    @ViewById
    protected Spinner spServer;

    @ViewById
    protected EditText txtServerUrl;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof ServerSelectionListener)
            this.serverSelectionListener = (ServerSelectionListener) activity;
        else
            Log.w(TAG, "Activity '" + activity.getClass().getSimpleName() + "' must implement '" + ServerSelectionListener.class.getSimpleName() + "'" +
                " to receive events from the fragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void afterViews() {
        knownServerAdapter = new KnownServerAdapter(this.getActivity());
        spServer.setAdapter(knownServerAdapter);
        txtServerUrl.setVisibility(View.GONE);
        txtErrorMessage.setVisibility(View.GONE);
        btnNext.setEnabled(true);
        //If we have saved a selection in our state, we restore it
        if(serverUrl != null)
            txtServerUrl.setText(serverUrl);
        if(selectedServerIndex != -1)
            spServer.setSelection(selectedServerIndex);
    }

    @Override
    public void onStart() {
        super.onStart();
        //We disable the button that will be re-enabled after if the server does
        //support our version of the notidroid client API.
        btnNext.setEnabled(false);
        //If we have a server URL we check it otherwise we know that the server
        //already support our API version
        if(serverUrl != null) {
            if(doesSupportCurrentAPIVersion)
                onServerSupportClientAPI(serverName, serverUrl);
            else
                retrieveAndCheckServerInformation(serverUrl);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //We cancel all background tasks
        BackgroundExecutor.cancelAll("serverInformation", true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.serverSelectionListener = null;
    }

    @ItemSelect
    protected void spServer(boolean selected, int position) {
        if(selected) {
            btnNext.setEnabled(false);
            this.doesSupportCurrentAPIVersion = false;
            //If the user has selected the custom entry, we display the edittext view so
            //he can type its own server url. Otherwise, we validate that the server
            //support the API version used by this application.
            if(position == knownServerAdapter.getCount() - 1) {
                txtServerUrl.setVisibility(View.VISIBLE);
            } else {
                final KnownServer knownServer = (KnownServer) knownServerAdapter.getItem(position);
                retrieveAndCheckServerInformation(knownServer.getApiUrl());
            }
        }
    }

    @TextChange
    protected void txtServerUrlTextChanged(TextView textView) {
        //We only run this code when the 'Custom' entry is selected
        if(spServer.getSelectedItemPosition() != spServer.getCount() - 1)
            return;
        //If the user do not change the text for X ms, we launch the same check as
        //for known server.
        BackgroundExecutor.cancelAll("delayedServerInformation", true);
        delayedRetrieveAndCheckServerInformation(textView.getText().toString());
    }

    @Override
    protected void onNextClicked() {
        if(serverSelectionListener != null)
            serverSelectionListener.onServerSelection(serverName, serverUrl);
    }

    /**
     * Check that the API url provided by the user points to a notidroid
     * cloud backend. It also check that the backend supports the API version
     * used by this client application.
     *
     * @param apiUrl Url pointing to the root of the backend api.
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void retrieveAndCheckServerInformation(String apiUrl) {
        //TODO Start loading animation on the button
        //We remove the previous error message that is no more relevant
        txtErrorMessage.setVisibility(View.GONE);
        //We also forgot the state of the previous selected server
        doesSupportCurrentAPIVersion = false;
        //First we check that the url is well formed, otherwise we display an
        //error message
        if(false) {
            //TODO
        } else
            doRetrieveAndCheckServerInformation(apiUrl);
    }

    @Background(id = "delayedServerInformation")
    protected void delayedRetrieveAndCheckServerInformation(String apiUrl) {
        try {
            Thread.sleep(100);
            retrieveAndCheckServerInformation(apiUrl);
        } catch (InterruptedException e) {}
    }

    @Background(id = "serverInformation")
    protected void doRetrieveAndCheckServerInformation(String apiUrl) {
        ServerInformationResource serverInformation = null;
        try {
            serverInformationEndpoint.setRootUrl(apiUrl);
            serverInformation = serverInformationEndpoint.getServerInformation();
            onServerInformationRetrieved(apiUrl, serverInformation);
        } catch (RestClientException e) {
            onErrorWhileRetrievingServerInformation(apiUrl, e);
        }
    }

    @UiThread
    protected void onServerInformationRetrieved(String apiUrl, ServerInformationResource serverInformation) {
        //We must check that the version of the API that is used by this application is supported
        //by the cloudbackend
        if(ArrayUtils.contains(serverInformation.getSupportedAPIVersion(), NotidroidClientAPI.getAPIVersion())) {
            onServerSupportClientAPI(serverInformation.getPublicName(), apiUrl);
        } else {
            onServerDoNotSupportClientAPI(serverInformation.getPublicName(), apiUrl);
        }
    }

    @UiThread
    protected void onErrorWhileRetrievingServerInformation(String apiUrl, RestClientException e) {
        txtErrorMessage.setVisibility(View.VISIBLE);
        txtErrorMessage.setText(R.string.signinserverselectionfragment_txtErrorMessage_httpError);
    }

    private void onServerSupportClientAPI(String serverName, String apiUrl) {
        this.serverName = serverName;
        this.serverUrl = apiUrl;
        this.doesSupportCurrentAPIVersion = true;
        btnNext.setEnabled(true);
    }

    private void onServerDoNotSupportClientAPI(String serverName, String apiUrl) {
        txtErrorMessage.setVisibility(View.VISIBLE);
        txtErrorMessage.setText(R.string.signinserverselectionfragment_txtErrorMessage_UnsupportedAPIVersion);
    }

    /**
     * Interface that must be used implemented by the activity using this fragment.
     */
    public interface ServerSelectionListener {

        /**
         * Callback called when the user has selected a server hosting a compatible version
         * of notidroid cloud backend.
         *
         * @param serverName Public name of the server
         * @param apiUrl Url pointing to the root of the backend api.
         */
        public void onServerSelection(String serverName, String apiUrl);

    }

}
