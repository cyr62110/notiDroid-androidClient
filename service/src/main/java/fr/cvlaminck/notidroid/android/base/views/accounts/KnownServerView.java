package fr.cvlaminck.notidroid.android.base.views.accounts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import fr.cvlaminck.notidroid.android.base.R;
import fr.cvlaminck.notidroid.android.base.data.assets.config.KnownServer;

/**
 * ViewGroup presenting the name and the url of a notidroid server. This view is displayed in
 * the first step of the login/signin process where the user needs to select its server.
 * If the knowServer server is null, this view will display a custom entry which allows the user
 * to enter a custom url and select its own server.
 */
@EViewGroup(R.layout.knownserverview)
public class KnownServerView
    extends LinearLayout {

    @ViewById
    protected TextView txtServerName;

    @ViewById
    protected TextView txtServerUrl;

    private KnownServer knownServer;

    public KnownServerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setKnownServer(KnownServer knownServer) {
        this.knownServer = knownServer;
        if(this.knownServer == null)
            displayCustom();
        else
            displayKnowServer();
    }

    private void displayCustom() {
        txtServerName.setText(R.string.knownserverview_txtServerName);
        txtServerUrl.setText(R.string.knownserverview_txtServerUrl);
    }

    private void displayKnowServer() {
        txtServerName.setText(knownServer.getServerName());
        txtServerUrl.setText(knownServer.getServerUrl());
    }
}
