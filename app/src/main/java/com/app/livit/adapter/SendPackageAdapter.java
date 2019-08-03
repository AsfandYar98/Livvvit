package com.app.livit.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.app.livit.fragment.sendpackage.*;

/**
 * Created by Rémi OLLIVIER on 26/04/2018.
 */

public class SendPackageAdapter extends FragmentPagerAdapter {

    public SendPackageAdapter(FragmentManager fm) {
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
                return SendPackageAddressPickupFragment.newInstance();
            case 1:
                return SendPackageAddressDropoffFragment.newInstance();
            case 2:
                return SendPackageWeightFragment.newInstance();
            case 3:
                return SendPackagePictureFragment.newInstance();
            case 4:
                return SendPackageSizeFragment.newInstance();
            case 5:
                return SendPackageContactFragment.newInstance();
            //case 5:
              //  return SendPackageInsuranceFragment.newInstance();
            default:
                return SendPackageWeightFragment.newInstance();
        }
    }

    /**
     * This method is used to know the size of the list (always returns 5)
     * @return the page's count (5)
     */
    @Override
    public int getCount() {
        return 6;
    }
}
