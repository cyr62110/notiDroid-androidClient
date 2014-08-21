package fr.cvlaminck.notidroid.android.api.push;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Status of the connection between this device and the message broker.
 *
 * @since 0.2
 */
public class ConnectionStatus
        implements Parcelable {

    private DetailedStatus detailedStatus;

    public ConnectionStatus() {
        this.detailedStatus = DetailedStatus.NOT_STARTED;
    }

    private ConnectionStatus(Parcel in) {
        this.detailedStatus = DetailedStatus.valueOf(in.readString());
    }

    /**
     * Status of the connection between this device and the message
     * broker.
     */
    public Status getStatus() {
        return detailedStatus.getStatus();
    }

    /**
     * Detailed status of the connection between this device and the message
     * broker. This level of details is pretty much never needed.
     */
    public DetailedStatus getDetailedStatus() {
        return detailedStatus;
    }

    public void setDetailedStatus(DetailedStatus detailedStatus) {
        this.detailedStatus = detailedStatus;
    }

    /**
     *
     */
    public enum Status {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    /**
     * Enumeration of all precise state of the connection.
     * You would normally nether need this much of details and rather use the Status instead
     * of this DetailedStatus.
     */
    public enum DetailedStatus {
        /**
         * Initial status of the connection. This status is used when the service is starting.
         */
        NOT_STARTED(Status.DISCONNECTED),
        /**
         * This device does not have any Notidroid account. The user must login or create
         * an account on its device.
         */
        NO_ACCOUNT(Status.DISCONNECTED),
        /**
         * Can only occur if the user has leaved the login/sign-in procedure before its end.
         * We do not have any device id to identify our device on the message broker so this
         * device cannot connect to it.
         */
        NO_DEVICE_ID(Status.DISCONNECTED),
        /**
         * No connection to any network is available to connect to Internet.
         */
        NO_NETWORK_AVAILABLE(Status.DISCONNECTED),
        /**
         * The message broker is not available.
         */
        SERVICE_NOT_AVAILABLE(Status.DISCONNECTED),
        /**
         * The service is retrieving an authentication token to authenticate the user
         * on the message broker.
         */
        RETRIEVING_AUTH_TOKEN(Status.CONNECTING),
        /**
         * The service has failed to retrieve a valid access token.
         * The authenticator uses notification to notify the error to the client
         * and help the user to solve the issue.
         */
        CANNOT_RETRIEVE_AUTH_TOKEN(Status.DISCONNECTED),
        /**
         * The authentication with the broker failed. The Oauth2 access token
         * may be expired and need to be refreshed.
         */
        AUTHENTICATION_FAILED(Status.DISCONNECTED),
        /**
         * The service is trying to establish the connection
         * with the message broker using the MQTT protocol.
         */
        CONNECTING_WITH_MQTT(Status.CONNECTING),
        /**
         * The connection is established and working. Data can be sent to
         * and received from the message broker.
         */
        CONNECTED(Status.CONNECTED),
        /**
         * Status used when the logic behind is not implemented.
         */
        UNKNOWN(Status.DISCONNECTED);

        private Status status;

        private DetailedStatus(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(detailedStatus.name());
    }

    public static final Creator<ConnectionStatus> CREATOR = new Creator<ConnectionStatus>() {
        @Override
        public ConnectionStatus createFromParcel(Parcel parcel) {
            return new ConnectionStatus(parcel);
        }

        @Override
        public ConnectionStatus[] newArray(int size) {
            return new ConnectionStatus[size];
        }
    };

}
