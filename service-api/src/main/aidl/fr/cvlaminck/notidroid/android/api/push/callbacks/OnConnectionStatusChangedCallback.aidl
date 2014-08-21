package fr.cvlaminck.notidroid.android.api.push.callbacks;

import fr.cvlaminck.notidroid.android.api.push.ConnectionStatus;

/**
 *
 *
 * @since 0.2
 */
interface OnConnectionStatusChangedCallback {

    void onConnectionStatusChanged(in ConnectionStatus status);

}
