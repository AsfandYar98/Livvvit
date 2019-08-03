package com.app.livit.fragment.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.livit.R;
import com.app.livit.activity.MainActivity;
import com.app.livit.adapter.HistoryViewPagerAdapter;
import com.app.livit.utils.PreferencesHelper;

/**
 * Created by Rémi OLLIVIER on 03/04/2018.
 */

public class HistoryFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public static HistoryFragment newInstance() {

        HistoryFragment fragment = new HistoryFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_history, container, false);

        //get the view's items
        viewPager = view.findViewById(R.id.vp_history);
        tabLayout = view.findViewById(R.id.tabs);
        viewPager.setAdapter(new HistoryViewPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        //displays correct colors to the toolbar principally
        if (getActivity() != null)
            ((MainActivity) getActivity()).changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());

        return view;
    }
}
