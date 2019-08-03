package com.app.livit.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.livit.R;
import com.app.livit.event.delivery.NewDeliveryEvent;
import com.app.livit.fragment.ContactFragment;
import com.app.livit.fragment.DeliveryDetailsFragment;
import com.app.livit.fragment.home.AboutFragment;
import com.app.livit.fragment.home.AccountFragment;
import com.app.livit.fragment.home.HistoryFragment;
import com.app.livit.fragment.home.HomeDeliverymanFragment;
import com.app.livit.fragment.home.HomeSenderFragment;
import com.app.livit.network.DeliveryService;
import com.app.livit.service.UpdatePositionService;
import com.app.livit.utils.AESCrypt;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;
import com.app.livit.view.ClickableToolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * MainActivity created by Rémi OLLIVIER in march 2018
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private ClickableToolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private View headerView;
    private TextView tvName;
    private ImageView ivPicture;
    private boolean isToolbarVisible = false;
    private Intent foregroundNotificationServiceIntent;
    private View separatorView;
    private NavigationView nvDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        //if parameters are missing, finish to avoid unexpected behavior
        if (getIntent() == null)
            finish();

        // Set a Toolbar to replace the ActionBar.
        this.toolbar = findViewById(R.id.toolbar);
        this.toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        this.setSupportActionBar(this.toolbar);
        setSupportActionBar(this.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.empty);
            //display burger menu icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.mDrawer = findViewById(R.id.drawer_layout);

        this.drawerToggle = this.setupDrawerToggle();
        this.mDrawer.addDrawerListener(this.drawerToggle);
        this.nvDrawer = findViewById(R.id.nv_main);

        //setup navigation drawer header
        if (this.nvDrawer != null) {
            this.headerView = this.nvDrawer.getHeaderView(0);
            this.tvName = this.headerView.findViewById(R.id.nav_tv_name);
            this.ivPicture = this.headerView.findViewById(R.id.iv_profile);
            this.separatorView = this.headerView.findViewById(R.id.separator);
            setupDrawerContent(this.nvDrawer);
            this.nvDrawer.getMenu().performIdentifierAction(R.id.nav_home, Menu.NONE);            //select automatically first item drawer
        }

        //setup foreground service notification
        foregroundNotificationServiceIntent = new Intent(this, UpdatePositionService.class);

        //if the user is delvieryman, start the service
        if (PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0) {
            startDeliverymanService();
            changeMainActivityColors(Constants.PROFILETYPE_DELIVERYMAN);
        }

        //setup backstack changed listener, basically to manage toolbar color/visibility
        this.getSupportFragmentManager().addOnBackStackChangedListener(new BackStackListener());
    }

    /**
     * Lifecycle events methods
     */
    @Override
    protected void onResume() {
        super.onResume();
        //EventBus.getDefault().register(this);
        refreshIdentity(false);
        placeToolbarBelowFragment(isToolbarVisible);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.drawerToggle.onConfigurationChanged(newConfig);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, this.mDrawer, this.toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem, navigationView);
                        return true;
                    }
                });
    }

    /**
     * Method that is called when a menu item is selected
     * @param menuItem the item selected
     * @param navigationView the navigation view
     */
    private void selectDrawerItem(MenuItem menuItem, NavigationView navigationView) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                if (PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_SENDER) == 0)
                    fragmentClass = HomeSenderFragment.class;
                else
                    fragmentClass = HomeDeliverymanFragment.class;
                this.isToolbarVisible = false;
                this.toolbar.setTitle("");
                break;
            case R.id.nav_history:
                fragmentClass = HistoryFragment.class;
                this.isToolbarVisible = true;
                this.toolbar.setTitle(R.string.title_history);
                break;
            case R.id.nav_account:
                fragmentClass = AccountFragment.class;
                this.isToolbarVisible = true;
                this.toolbar.setTitle(R.string.title_account);
                break;
            case R.id.nav_contact:
                fragmentClass = ContactFragment.class;
                this.isToolbarVisible = true;
                this.toolbar.setTitle("Contactez nous");
                break;
            /*case R.id.nav_about:
                fragmentClass = AboutFragment.class;
                this.isToolbarVisible = true;
                this.toolbar.setTitle(R.string.title_about);
                break;*/
            case R.id.nav_share:
                shareIt();
                this.mDrawer.closeDrawers();
                return;
            default:
                if (PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_SENDER) == 0)
                    fragmentClass = HomeSenderFragment.class;
                else
                    fragmentClass = HomeDeliverymanFragment.class;
                this.isToolbarVisible = false;
                this.toolbar.setTitle("");
        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }
        placeToolbarBelowFragment(this.isToolbarVisible);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commit();

        // Highlight the selected item has been done by NavigationView
        navigationView.setCheckedItem(menuItem.getItemId());
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        this.mDrawer.closeDrawers();
    }

    /**
     * Method to display delivery's detail fragment
     * @param deliveryId the delivery's id
     */
    public void goToDeliveryDetailsFragment(String deliveryId) {
        this.isToolbarVisible = true;
        changeMainActivityColors(Constants.PROFILETYPE_SENDER);
        this.toolbar.setTitle(R.string.details);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment, DeliveryDetailsFragment.newInstance(deliveryId)).addToBackStack(null).commit();
    }

    /**
     * Method called when an item is selected
     * @param item the selected item
     * @return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //check if this is the share item (return false)
        return item.getItemId() == R.id.nav_share && (this.drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
    }

    /**
     * This method is used to hide toolbar if necessary (transparent toolbar and statusbar)
     * Manages the toolbar's color depending on the user's role too
     * @param below the value used to hide/show the toolbar. True to display, false to hide
     */
    private void placeToolbarBelowFragment(boolean below) {
        FrameLayout fl = findViewById(R.id.fragment);
        if (below) {
            ((RelativeLayout.LayoutParams) fl.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.toolbar);
            ((RelativeLayout.LayoutParams) fl.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            if (Constants.PROFILETYPE_DELIVERYMAN.compareTo(PreferencesHelper.getInstance().isDeliveryManActivated()) == 0) {
                this.toolbar.setBackgroundColor(getResources().getColor(R.color.orange));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setStatusBarColor(getResources().getColor(R.color.orange_dark));
            } else {
                this.toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }
            this.toolbar.setClickable(true);
        } else {
            ((RelativeLayout.LayoutParams) fl.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
            ((RelativeLayout.LayoutParams) fl.getLayoutParams()).removeRule(RelativeLayout.BELOW);
            this.toolbar.setBackgroundColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            this.toolbar.setClickable(false);
            this.toolbar.setTitle(R.string.empty);
        }
    }

    /**
     * This public method is used to start the deliveryman service when the user is read to deliver a package
     * This service sends position updates periodically to the backend
     */
    public void startDeliverymanService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(foregroundNotificationServiceIntent);
        } else {
            startService(foregroundNotificationServiceIntent);
        }
    }

    /**
     * This public method is used to stop the deliveryman's service, especially when he is not deliveryman anymore
     */
    public void stopDeliverymanService() {
        stopService(foregroundNotificationServiceIntent);
    }

    /**
     * This public method is used to go to the home fragment
     */
    public void goToHomeFragment() {
        Class fragmentClass;
        if (PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_SENDER) == 0)
            fragmentClass = HomeSenderFragment.class;
        else
            fragmentClass = HomeDeliverymanFragment.class;
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
            this.nvDrawer.setCheckedItem(R.id.nav_home);
            this.isToolbarVisible = false;
            changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method changes the view from the navigation drawer
     * @param refreshNeeded the boolean used to know if a refresh is needed (info have been updated)
     */
    public void refreshIdentity(boolean refreshNeeded) {
        if (Utils.getFullUserInfo() == null)
            return;
        UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
        if (userInfo != null && headerView != null) {
            if (refreshNeeded) {
                //refresh picture
                Glide.with(this)
                        .load(userInfo.getPicture())
                        .apply(new RequestOptions().error(R.drawable.user).centerCrop())
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .into(this.ivPicture);
            } else {
                Glide.with(this)
                        .load(userInfo.getPicture())
                        .apply(new RequestOptions().error(R.drawable.user).centerCrop())
                        .into(this.ivPicture);
            }
            this.tvName.setText(getString(R.string.formatted_name, userInfo.getFirstname(), userInfo.getLastname()));
        }
    }

    /**
     * This method is used to adapt activity's colors to the user's role (orange for deliveryman, blue for sender
     * @param profilteType the user's profile type : "sender" or "deliveryman"
     */
    public void changeMainActivityColors(String profilteType) {
        if (profilteType.compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0) {
            this.headerView.setBackgroundColor(getResources().getColor(R.color.orange));
            this.separatorView.setBackgroundColor(getResources().getColor(R.color.orange_dark));
        } else {
            this.headerView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            this.separatorView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        //update toolbar visibility
        placeToolbarBelowFragment(this.isToolbarVisible);
    }

    /**
     * Override back button's behavior to manage backstack and fragments transactions
     */
    @Override
    public void onBackPressed() {
        if (this.mDrawer.isDrawerOpen(GravityCompat.START)) {
            this.mDrawer.closeDrawers();
            return;
        }

        //if backstack is composed of one or less fragment
        if (getSupportFragmentManager().getBackStackEntryCount() < 2)
            this.finish();
        else
            getSupportFragmentManager().popBackStack();
    }

    /**
     * This method is used to determine the current fragment's class
     * @return the current fragment
     */
    public Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    /**
     * The backstack listener used to manage navigation between the fragments of this activity
     * Each fragment's case set toolbar's visibility and checks the associated item in navigation drawer
     * For home fragment, checks if the role changed or not
     */
    private class BackStackListener implements FragmentManager.OnBackStackChangedListener {
        public void onBackStackChanged() {
            Fragment current = getCurrentFragment();
            if (current instanceof HomeDeliverymanFragment) {
                nvDrawer.setCheckedItem(R.id.nav_home);
                isToolbarVisible = false;
                if (PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_SENDER) == 0) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, HomeSenderFragment.newInstance()).commit();
                }
            } else if (current instanceof HomeSenderFragment) {
                nvDrawer.setCheckedItem(R.id.nav_home);
                isToolbarVisible = false;
                if (PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, HomeDeliverymanFragment.newInstance()).commit();
                }
            } else if (current instanceof HistoryFragment) {
                isToolbarVisible = true;
                nvDrawer.setCheckedItem(R.id.nav_history);
            } else if (current instanceof AccountFragment) {
                isToolbarVisible = true;
                nvDrawer.setCheckedItem(R.id.nav_account);
            } /*else if (current instanceof AboutFragment) {
                isToolbarVisible = true;
                nvDrawer.setCheckedItem(R.id.nav_about);
            }*/
            changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());
        }
    }
    private void shareIt() {
        //sharing implementation here
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download Livit app");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Je te recommande cette application très pratique pour envoyer des colis : https://play.google.com/store/apps/details?id=com.google.android.apps.plus");
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
