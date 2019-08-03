package com.app.livit.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.app.livit.R;
import com.app.livit.fragment.home.HistoryDeliverymanFragment;
import com.app.livit.fragment.home.HistorySenderFragment;
import com.app.livit.utils.Utils;

/**
 * Created by Rémi OLLIVIER on 25/06/2018.
 */

public class HistoryViewPagerAdapter extends FragmentPagerAdapter {

    public HistoryViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * This method returns the fragment expected at the given position
     * @param position the position
     * @return the corresponding fragment
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return HistorySenderFragment.newInstance();
            case 1:
                return HistoryDeliverymanFragment.newInstance();
            default:
                return HistorySenderFragment.newInstance();
        }
    }

    /**
     * This method is used to know the size of the list (always returns 2)
     * @return the page's count (2)
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * Method that returnsthe title for the fragment at the given position
     * @param position the position
     * @return the fragment's title
     */
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return Utils.getContext().getString(R.string.tabs_sender);
            case 1:
                return Utils.getContext().getString(R.string.tabs_deliveryman);
            default:
                return null;
        }
    }
}