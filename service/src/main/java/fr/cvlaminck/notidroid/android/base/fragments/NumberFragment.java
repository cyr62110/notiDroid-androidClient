package fr.cvlaminck.notidroid.android.base.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment displaying a number in full page.
 * Used to test the custom view pager indicator
 * TODO : remove this
 */
public class NumberFragment
    extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView text = new TextView(inflater.getContext());
        text.setText("1");
        text.setTextSize(25);
        return text;
    }

}
