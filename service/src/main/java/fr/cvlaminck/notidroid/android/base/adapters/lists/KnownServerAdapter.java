package fr.cvlaminck.notidroid.android.base.adapters.lists;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.cvlaminck.notidroid.android.base.data.assets.config.KnownServer;
import fr.cvlaminck.notidroid.android.base.views.accounts.KnownServerView;
import fr.cvlaminck.notidroid.android.base.views.accounts.KnownServerView_;

/**
 * Adapter containing all known Notidroid cloud backends. It also add a Custom entry if the user wants
 * to provide its own server instead of picking one in the list.
 */
public class KnownServerAdapter
        extends BaseAdapter {
    private static final String TAG = KnownServerAdapter.class.getSimpleName();

    private Context context = null;

    private List<KnownServer> knownServers = null;

    public KnownServerAdapter(@NotNull Context context) {
        knownServers = new ArrayList<>();
        readKnowServersFromAsset();
    }

    /**
     * Read known servers from the asset file.
     */
    private void readKnowServersFromAsset() {
        try {
            final InputStream inputStream = context.getAssets().open("config/knownServers.json");
            final ObjectMapper objectMapper = new ObjectMapper();
            knownServers.addAll(objectMapper.readValue(inputStream, knownServers.getClass()));
        } catch (IOException e) {
            Log.e(TAG, "Cannot read the list of know servers from assets due to : " + e.getMessage());
        }
    }

    @Override
    public int getCount() {
        return knownServers.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position < knownServers.size())
            return knownServers.get(position);
        else
            return null; //In the case of the Custom entry, we return null
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = KnownServerView_.build(context, null);
        final KnownServerView knownServerView = (KnownServerView) convertView;
        knownServerView.setKnownServer((KnownServer) getItem(position));
        return knownServerView;
    }
}
