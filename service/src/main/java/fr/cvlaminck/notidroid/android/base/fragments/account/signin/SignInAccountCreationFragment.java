package fr.cvlaminck.notidroid.android.base.fragments.account.signin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.http.HttpAuthentication;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

import fr.cvlaminck.notidroid.android.api.accounts.NotidroidCloudAccount;
import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.accounts.NotidroidAccount;
import fr.cvlaminck.notidroid.android.base.cloud.endpoints.UserDeviceEndpoint;
import fr.cvlaminck.notidroid.android.base.cloud.endpoints.PublicUserEndpoint;
import fr.cvlaminck.notidroid.android.base.extractors.UserDeviceInfoExtractor;
import fr.cvlaminck.notidroid.cloud.client.api.devices.UserDeviceResource;
import fr.cvlaminck.notidroid.cloud.client.api.users.UserResource;
import fr.cvlaminck.notidroid.cloud.client.api.users.UserWithCredentialsResource;

/**
 * Fragment that will create users online cloudAccount and also
 * register its device on the cloud backend. This fragment will
 * also create the android Account that will be used by the rest of
 * the application.
 */
@EFragment(R.layout.signinaccountcreationfragment)
public class SignInAccountCreationFragment
        extends Fragment {
    public final static String TAG = SignInAccountCreationFragment.class.getSimpleName();

    /**
     * Display an icon showing that some operations are in progress to
     * the user.
     */
    private final static int STATUS_ICON_IN_PROGRESS = 0;
    /**
     * Display an icon showing that its cloudAccount have been fully
     * registered.
     */
    private final static int STATUS_ICON_OK = 1;
    /**
     * Display an icon showing that a fatal error occurred during the
     * operation
     */
    private final static int STATUS_ICON_ERROR = 2;

    /**
     * The procedure has not been started and can be started
     * by the activity.
     */
    private final static int STATUS_NOT_STARTED = -1;

    /**
     * Initial status, nothing has been done.
     */
    private final static int STATUS_INITIAL = 0;

    /**
     * The online cloudAccount has been created on the backend.
     * But this device has not been registered on the user cloudAccount.
     */
    private final static int STATUS_ACCOUNT_CREATED = 1;
    /**
     * This device has been registered on the user cloudAccount.
     * Everything is fine.
     */
    private final static int STATUS_DEVICE_REGISTERED = 2;

    private final static int ERROR_MASK = 1000;
    /**
     *
     */
    private final static int ERROR_NO_ERROR = 0;
    /**
     * The email address is already associated with a online
     * cloudAccount on the selected cloud backend.
     */
    private final static int ERROR_EMAIL_IN_USE = 1;
    /**
     * We have lost the internet connection. We cant do anything
     * about this.
     */
    private final static int ERROR_CONNECTION_LOST = 2;
    /**
     * We have received an internal server error from the server
     * during the procedure. Something goes wrong so fatal error too.
     */
    private final static int ERROR_INTERNAL_SERVER_ERROR = 3;
    /**
     * Registration of user is not allowed on the server or our
     * current is not in the server whitelist.
     */
    private final static int ERROR_REGISTRATION_NOT_ALLOWED = 4;

    @Bean
    protected UserDeviceInfoExtractor userDeviceInfoExtractor;

    @ViewById
    protected TextView lblTitle;

    @ViewById
    protected TextView lblSubTitle;

    @ViewById
    protected Button btnPrimary;

    @ViewById
    protected Button btnSecondary;

    @SystemService
    protected AccountManager accountManager;

    @RestService
    protected PublicUserEndpoint publicUserEndpoint;

    @RestService
    protected UserDeviceEndpoint userDeviceEndpoint;

    /**
     * Status of the ongoing registration procedure.
     */
    @InstanceState
    protected int status = STATUS_NOT_STARTED;

    /**
     * Account that we are trying to register on the cloud backend.
     */
    @InstanceState
    protected NotidroidCloudAccount cloudAccount = null;

    /**
     * Android cloudAccount that have been created for the user
     */
    @InstanceState
    protected Account notidroidAccount = null;

    /**
     * Password that the user wants to use to protect its
     * data.
     */
    @InstanceState
    protected String password = null;

    private AccountCreationStateListener accountCreationStateListener = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AccountCreationStateListener)
            accountCreationStateListener = (AccountCreationStateListener) activity;
        else
            Log.w(TAG, "Activity '" + activity.getClass().getSimpleName() + "' must implement '" + AccountCreationStateListener.class.getSimpleName() + "'" +
                    " to receive events from the fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        accountCreationStateListener = null;
    }

    @AfterViews
    protected void afterViews() {
        if (status != STATUS_NOT_STARTED)
            displayStatusAndContinue(status);
    }

    @Click
    protected void btnPrimaryClicked() {
        final int statusCode = status % ERROR_MASK;
        final int errorCode = status / ERROR_MASK;
        //We take care of the only case where the error does
        //not give the function of the button
        if (statusCode == STATUS_DEVICE_REGISTERED) {
            //TODO
        } else
            switch (errorCode) {
                case ERROR_EMAIL_IN_USE:
                    if (accountCreationStateListener != null) {
                        this.status = STATUS_NOT_STARTED; //Since we will leave this page, we reinit its status.
                        accountCreationStateListener.onAccountCreationFailed();
                        accountCreationStateListener.onUserMustLogin(cloudAccount.getEmail());
                    }
                    break;
                case ERROR_REGISTRATION_NOT_ALLOWED:
                    if (accountCreationStateListener != null) {
                        this.status = STATUS_NOT_STARTED; //Since we will leave this page, we reinit its status.
                        accountCreationStateListener.onAccountCreationFailed();
                        accountCreationStateListener.onUserMustSelectAnotherServer();
                    }
                    break;
                case ERROR_CONNECTION_LOST:
                case ERROR_INTERNAL_SERVER_ERROR:
                    displayStatusAndContinue(statusCode);
                    break;
            }
    }

    @Click
    protected void btnSecondaryClicked() {
        final int statusCode = status % ERROR_MASK;
        final int errorCode = status / ERROR_MASK;
        switch (errorCode) {
            case ERROR_EMAIL_IN_USE:
                if (accountCreationStateListener != null) {
                    this.status = STATUS_NOT_STARTED; //Since we will leave this page, we reinit its status.
                    accountCreationStateListener.onAccountCreationFailed();
                    accountCreationStateListener.onUserMustChangeCredentials();
                }
        }
    }

    /**
     * Start the registration procedure. Must be called by the
     * activity when the user enter this fragment.
     */
    public void startRegistration(NotidroidCloudAccount account, String password) {
        if (this.status == STATUS_NOT_STARTED) {
            this.cloudAccount = account;
            this.password = password;
            if (lblTitle != null)
                displayStatusAndContinue(STATUS_INITIAL);
            else
                this.status = STATUS_INITIAL;
        }
    }

    /**
     * Show to the user the UI corresponding to the provided status. Calling
     * this function will also continue the registration procedure from where it stopped.
     *
     * @param status Status that must be displayed. It also contains the error code.
     */
    private void displayStatusAndContinue(int status) {
        //We save the status in the state of the fragment so we can support orientation changes, etc...
        this.status = status;
        //We split the value into the status code and the error code.
        final int statusCode = status % ERROR_MASK;
        final int errorCode = status / ERROR_MASK;
        //Then we call the right function for the given status code.
        switch (statusCode) {
            case STATUS_INITIAL:
                displayInitialStatus(errorCode);
                break;
            case STATUS_ACCOUNT_CREATED:
                displayAccountCreated(errorCode);
                break;
            case STATUS_DEVICE_REGISTERED:
                displayDeviceRegistered(errorCode);
                break;
        }
    }

    /**
     * Change the icon showing the status of the registration
     * operation.
     *
     * @param statusIcon One of the STATUS_ICON_* constants.
     */
    private void showStatusIcon(int statusIcon) {
        //TODO : Fill this method when all icons have been designed
    }

    /**
     * Change the UI to notify the user that we have lost the internet
     * connection.
     */
    private void displayErrorConnectionLost() {
        showStatusIcon(STATUS_ICON_ERROR);
        lblSubTitle.setVisibility(View.VISIBLE);
        btnPrimary.setVisibility(View.VISIBLE);
        btnSecondary.setVisibility(View.GONE);
        lblTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblTitle_registrationFailed));
        lblSubTitle.setText("Retry. Your internet connection has been cut during the procedure.");
        btnPrimary.setText(this.getString(R.string.signinaccountcreationfragment_btnPrimary_retry));
    }

    /**
     * Change the UI to notify the user that our server has encountered a fatal error.
     */
    private void displayErrorInternalServerError() {
        showStatusIcon(STATUS_ICON_ERROR);
        lblSubTitle.setVisibility(View.VISIBLE);
        btnPrimary.setVisibility(View.VISIBLE);
        btnSecondary.setVisibility(View.GONE);
        lblTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblTitle_registrationFailed));
        lblSubTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblSubTitle_internalError));
        btnPrimary.setText(this.getString(R.string.signinaccountcreationfragment_btnPrimary_retry));
    }

    /**
     * At this step, we must register the user cloudAccount on the cloud backend and creates
     * its local android Account with its credentials so our application can use it later
     * to do REST calls.
     *
     * @param errorCode
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void displayInitialStatus(int errorCode) {
        switch (errorCode) {
            case ERROR_NO_ERROR:
                //We display ...
                showStatusIcon(STATUS_ICON_IN_PROGRESS);
                lblSubTitle.setVisibility(View.GONE);
                btnPrimary.setVisibility(View.GONE);
                btnSecondary.setVisibility(View.GONE);
                lblTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblTitle_inProgress));
                //...and we process
                processInitialStatus();
                break;
            case ERROR_EMAIL_IN_USE:
                showStatusIcon(STATUS_ICON_ERROR);
                lblSubTitle.setVisibility(View.VISIBLE);
                btnPrimary.setVisibility(View.VISIBLE);
                btnSecondary.setVisibility(View.VISIBLE);
                lblTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblTitle_registrationFailed));
                lblSubTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblSubTitle_emailAlreadyInUse));
                btnPrimary.setText(this.getString(R.string.signinaccountcreationfragment_btnPrimary_login));
                btnSecondary.setText(this.getString(R.string.signinaccountcreationfragment_btnSecondary_changeMyEmail));
                break;
            case ERROR_REGISTRATION_NOT_ALLOWED:
                showStatusIcon(STATUS_ICON_ERROR);
                lblSubTitle.setVisibility(View.VISIBLE);
                btnPrimary.setVisibility(View.VISIBLE);
                btnSecondary.setVisibility(View.GONE);
                lblTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblTitle_registrationFailed));
                lblSubTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblSubTitle_registrationNotAllowed));
                btnPrimary.setText(this.getString(R.string.signinaccountcreationfragment_btnPrimary_chooseAnotherServer));
                break;
            case ERROR_INTERNAL_SERVER_ERROR:
                displayErrorInternalServerError();
                break;
            case ERROR_CONNECTION_LOST:
                displayErrorConnectionLost();
                break;
        }
    }

    /**
     * Function called when the fragment starts an online operation.
     * We acquire a wake-lock so we are sure that the device will not be
     * turned off during the operation and that we will receive the server
     * response if we are connected. We also lock the orientation so our activity
     * will be recreated during the procedure.
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void onOnlineOperationStarted() {
        //TODO
    }

    /**
     * Function called when all online operation are finished. So we
     * release our wake-lock
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void onOnlineOperationFinished() {
        //TODO
    }

    /**
     *
     */
    @Background
    protected void processInitialStatus() {
        //We acquire a partial wake-lock so we can be sure that the online calls will end
        onOnlineOperationStarted();
        //We create the resource that we will use to create the online cloudAccount
        final UserWithCredentialsResource user = new UserWithCredentialsResource();
        user.setFirstName(cloudAccount.getFirstName());
        user.setLastName(cloudAccount.getLastName());
        user.setEmail(cloudAccount.getEmail());
        user.setPassword(password);
        //Then we call the REST API of the server
        try {
            //Finally, we create the local Android cloudAccount. It will be use to register this
            //device on the user cloudAccount.
            publicUserEndpoint.setRootUrl(cloudAccount.getServer().getApiUrl());
            UserResource onlineAccount = publicUserEndpoint.createOnlineAccount(user);
            //Then we create the local android cloudAccount
            final NotidroidAccount localAccount = NotidroidAccount.create(getActivity(), cloudAccount.getEmail(), password,
                    cloudAccount.getServer().getApiUrl(), cloudAccount.getServer().getOAuth2AuthorizationServerUrl(),
                    cloudAccount.getServer().getMessageBrokerUrl(), onlineAccount.getId());
            localAccount.setFirstName(cloudAccount.getFirstName());
            localAccount.setLastName(cloudAccount.getLastName());
            //We have finished this step, so we can update our status
            displayStatusAndContinue(STATUS_ACCOUNT_CREATED);
        } catch (HttpClientErrorException ex) {
            //We handle http error codes
            switch (ex.getStatusCode()) {
                case CONFLICT:
                    displayStatusAndContinue(STATUS_INITIAL + ERROR_EMAIL_IN_USE * ERROR_MASK);
                    break;
                case FORBIDDEN:
                    displayStatusAndContinue(STATUS_INITIAL + ERROR_REGISTRATION_NOT_ALLOWED * ERROR_MASK);
                    break;
                case UNPROCESSABLE_ENTITY:
                case INTERNAL_SERVER_ERROR:
                    displayStatusAndContinue(STATUS_INITIAL + ERROR_INTERNAL_SERVER_ERROR * ERROR_MASK);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace(); //TODO find the exception thrown when the connection drop
        } finally {
            //We release all the locks
            onOnlineOperationFinished();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void displayAccountCreated(int errorCode) {
        switch (errorCode) {
            case ERROR_NO_ERROR:
                //We display ...
                showStatusIcon(STATUS_ICON_IN_PROGRESS);
                lblSubTitle.setVisibility(View.GONE);
                btnPrimary.setVisibility(View.GONE);
                btnSecondary.setVisibility(View.GONE);
                lblTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblTitle_inProgress));
                //...and we process
                processAccountCreated();
                break;
            case ERROR_INTERNAL_SERVER_ERROR:
                displayErrorInternalServerError();
                break;
            case ERROR_CONNECTION_LOST:
                displayErrorConnectionLost();
                break;
        }
    }

    /**
     * Now that we have created the online account and the android account for the user.
     * We must register its device and get the device id associated with it.
     */
    @Background
    protected void processAccountCreated() {
        //First, we need to retrieve the android account
        final NotidroidAccount account = NotidroidAccount.find(getActivity(), cloudAccount.getEmail());
        //We also need to extract all info about this device. Since the account is new, this device is not registered
        UserDeviceResource device = userDeviceInfoExtractor.extract();
        //Then, we use this account to retrieve the Oauth2 access_token required to authenticate
        //our requests.
        try {
            onOnlineOperationStarted();

            final HttpAuthentication httpAuthentication = account.blockingGetHttpAuthentication();
            userDeviceEndpoint.setRootUrl(account.getCloudAPIUrl());
            userDeviceEndpoint.setAuthentication(httpAuthentication);
            //We register this device and get the device id associated with it.
            device = userDeviceEndpoint.registerDevice(device);
            //We store this device id in the account so we can use it latter
            account.setDeviceId(device.getId());
            //We have finished this step so we continue
            displayStatusAndContinue(STATUS_DEVICE_REGISTERED);
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            //TODO
        } catch (AuthenticatorException e) {
            e.printStackTrace();
            //TODO
        } catch (OperationCanceledException e) {
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
        } finally {
            onOnlineOperationFinished();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void displayDeviceRegistered(int errorCode) {
        //In this step, we do not have any processing. So the errorCode is useless.
        showStatusIcon(STATUS_ICON_OK);
        lblSubTitle.setVisibility(View.GONE);
        btnPrimary.setVisibility(View.VISIBLE);
        btnSecondary.setVisibility(View.GONE);
        lblTitle.setText(this.getString(R.string.signinaccountcreationfragment_lblTitle_registrationFinished));
        //TODO : add a subtitle...
        btnPrimary.setText(this.getString(R.string.signinaccountcreationfragment_btnPrimary_startUsing));
    }

    public interface AccountCreationStateListener {

        /**
         * Callback function called when the user start the procedure
         * to create its account. The parent activity must lock the user
         * on this fragment until the procedure is finished or fails.
         */
        public void onAccountCreationStarted();

        /**
         * Callback function called if the procedure failed.
         */
        public void onAccountCreationFailed();

        /**
         * Callback function called if the email is already in use on the
         * server and the user wants to log in with this email
         * instead of creating an new cloudAccount.
         */
        public void onUserMustLogin(String email);

        /**
         * Callback function called if the current does not accept the user
         * registration and the user has selected to choose another server
         * in the list.
         */
        public void onUserMustSelectAnotherServer();

        /**
         * Callback function called if the email is already in use on the
         * server and the user has chosen to enter another email address.
         */
        public void onUserMustChangeCredentials();

        /**
         * Callback function invoked when the procedure is fully finished :
         * Online cloudAccount created, android Account created, device registered.
         */
        public void onAccountCreationFinished();

    }

}
