package com.app.livit.fragment.sendpackage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.livit.R;
import com.app.livit.adapter.SendPackageAdapter;

/**
 * Created by Rémi OLLIVIER on 23/05/2018.
 */

public class SendPackageViewPagerFragment extends Fragment {
    private ViewPager mViewPager;
    private ImageView leftarrow;
    private ImageView rightarrow;

    public static SendPackageViewPagerFragment newInstance() {

        SendPackageViewPagerFragment fragment = new SendPackageViewPagerFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_viewpager, container, false);

        SendPackageAdapter pagerAdapter;
        if (getActivity() != null) {
            pagerAdapter = new SendPackageAdapter(getActivity().getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = view.findViewById(R.id.container);
            mViewPager.setAdapter(pagerAdapter);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        leftarrow = getActivity().findViewById(R.id.leftarrow);
        rightarrow = getActivity().findViewById(R.id.rightarrow);

        leftarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ind = mViewPager.getCurrentItem();
                if(ind!=0)
                {
                    mViewPager.setCurrentItem(ind-1,true);
                }
            }
        });

        rightarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ind = mViewPager.getCurrentItem();
                if(ind!=5)
                {
                    mViewPager.setCurrentItem(ind+1, true);
                }
            }
        });
    }

    public void goToNextFragment(int index) {
        this.mViewPager.setCurrentItem(index + 1, true);
    }
}
