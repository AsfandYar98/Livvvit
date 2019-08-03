package com.app.livit.fragment.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.livit.BuildConfig;
import com.app.livit.R;

/**
 * Created by Rémi OLLIVIER on 25/05/2018.
 * Simple fragment that takes places in the navigation drawer
 * Displays the about text and version number
 */

public class AboutFragment extends Fragment {

    public static AboutFragment newInstance() {

        AboutFragment fragment = new AboutFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_about, container, false);

        ((TextView) view.findViewById(R.id.tv_about_versionnumber)).setText(BuildConfig.VERSION_NAME);

        return view;
    }
}
