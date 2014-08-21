package fr.cvlaminck.notidroid.android.app.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;
import android.widget.Toast;

import fr.cvlaminck.notidroid.android.api.cloud.CloudService;
import fr.cvlaminck.notidroid.android.api.cloud.intents.BindCloudServiceIntentBuilder;
import fr.cvlaminck.notidroid.android.api.push.ConnectionStatus;
import fr.cvlaminck.notidroid.android.api.push.helpers.PushAwareActivity;
import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;
import fr.cvlaminck.notidroid.android.api.push.messages.TextPushMessage;
import fr.cvlaminck.notidroid.android.api.push.service.PushNotificationService;
import fr.cvlaminck.notidroid.android.app.R;

/**
 *
 */
public class HomeActivity
    extends PushAwareActivity {

    protected TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeactivity);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtStatus.setText("Test");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnectionStatusChanged(ConnectionStatus connectionStatus) {
        txtStatus.setText(connectionStatus.getDetailedStatus().name());
    }

    @Override
    public void onPushNotificationServiceConnected(PushNotificationService pushNotificationService) {

    }

    @Override
    public void onMessageReceived(PushMessage message) {
        if(message instanceof TextPushMessage) {
            Toast.makeText(this, ((TextPushMessage) message).getText(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPushNotificationServiceDisconnected() {

    }

}
