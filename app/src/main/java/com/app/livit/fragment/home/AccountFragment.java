package com.app.livit.fragment.home;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.activity.MainActivity;
import com.app.livit.event.delivery.UpdateUserInfoFailureEvent;
import com.app.livit.event.delivery.UpdateUserInfoSuccessEvent;
import com.app.livit.event.userinfo.CreatePreferencesFailureEvent;
import com.app.livit.event.userinfo.CreatePreferencesSuccessEvent;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.event.userinfo.GetPreferencesFailureEvent;
import com.app.livit.event.userinfo.GetPreferencesSuccessEvent;
import com.app.livit.event.userinfo.UpdatePreferencesFailureEvent;
import com.app.livit.event.userinfo.UpdatePreferencesSuccessEvent;
import com.app.livit.event.userinfo.UpdateProfileFailureEvent;
import com.app.livit.event.userinfo.UpdateProfileSuccessEvent;
import com.app.livit.fragment.login.ChangePasswordFragment;
import com.app.livit.model.QRCodeHelper;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.ImageFinder;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.test.model.Preferences;
import com.test.model.Profile;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.amazonaws.util.json.JsonUtils.JsonEngine.Gson;

/**
 * Created by Rémi OLLIVIER on 04/04/2018.
 * Fragment that takes places in the navigation drawer
 * Displays the user's account and allows user to edit his info and preferences
 */

public class AccountFragment extends Fragment {
    private static final int REQUESTCODE = 58749;
    private static final int MENU_ITEM_DISCONNECT = 1;
    private boolean disconnecting = false;

    private SeekBar sbDistance;
    private SeekBar sbWeight;
    private TextView tvMaxDistance;
    private TextView tvMaxWeight;
    private Button btValidate;
    private Switch swAvailable;
    private Button btCar;
    private Button btMoto;
    private Button btTruck;
    private Button btBike;
    private Button btChangeUserInfo;
    private EditText etMail;
    private EditText etPhoneNumber;
    private EditText etFirstname;
    private EditText etLastname;
    private LinearLayout llMyName;
    private LinearLayout llEditInfo;
    private ProgressBar pbUpdateUserInfo;
    private CardView cvMyVehicle;
    private String vehicleSelected;
    private ImageView ivMe;
    private ImageView qrcode;
    private TextView tvMyName;
    private Preferences preferences;
    private String deliverymanProfileId;
    private boolean editing = false;
    private String imageFilePath;
    private ImageView ivDeletePicture;
    private TextView tvAddPicture;

    public static AccountFragment newInstance() {

        AccountFragment fragment = new AccountFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_my_infos, container, false);
        setHasOptionsMenu(true);

        initView(view);

        return view;
    }

    /**
     * Statements to make after the view is created
     * @param view the created view
     * @param savedInstanceState the saved instance state (nullable)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //get deliveryman's profile and get preferences linked to it
        for (Profile profile : Utils.getFullUserInfo().getProfiles()) {
            if (profile.getPtype().compareTo(getString(R.string.deliveryman_string)) == 0) {
                //todo see why not profile ID when the user has just been created
                this.deliverymanProfileId = profile.getProfileID();
                this.btValidate.setEnabled(false);
                new ProfileService().getPreferences(this.deliverymanProfileId);
            }
        }
    }

    /**
     * Created the menu item to allow the user to disconnect
     * @param menu the menu
     * @param inflater the inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        menu.add(Menu.NONE, MENU_ITEM_DISCONNECT, Menu.NONE, R.string.sign_out).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_DISCONNECT:
                UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
                userInfo.setUserArn(" ");
                this.disconnecting = true;
                new ProfileService().updateUserInfo(userInfo);
                return true;

            default:
                return false;
        }
    }

    /**
     * Lifecycle events
     */
    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        this.swAvailable.setChecked(PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0);
        if (editing) {
            this.llMyName.setVisibility(View.GONE);
            this.llEditInfo.setVisibility(View.VISIBLE);
            this.ivDeletePicture.setVisibility(this.ivMe.getVisibility());
            this.btChangeUserInfo.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_save));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * View initialization
     * Get UI items and set account values
     * @param view the view to init
     */
    private void initView(View view) {
        this.getViews(view);
        this.ivMe = view.findViewById(R.id.iv_me);
        this.tvMyName = view.findViewById(R.id.tv_my_name);
        UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
        if (userInfo == null) {
            new ProfileService().getFullUserInfo();
        } else {
            initUserInfo(userInfo);
            generateQR();
        }
    }

    private void generateQR() {
        UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
        String user = "Name= " + userInfo.getFirstname() + " " + userInfo.getLastname() + "\n" + "Email = " + userInfo.getEmail() +"\n" + "Phone Number= "+ userInfo.getPhoneNumber();
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(user, BarcodeFormat.QR_CODE, 400,400);
            this.qrcode.setImageBitmap(bitmap);
        }
        catch (Exception e){

        }

    }

    /**
     * This method is used to init the view with user info
     * @param userInfo the user info to display
     */
    private void initUserInfo(UserInfo userInfo) {
        this.tvMyName.setText(getString(R.string.formatted_name, userInfo.getFirstname(), userInfo.getLastname()));
        Glide.with(this)
                .load(userInfo.getPicture())
                .apply(new RequestOptions().error(R.drawable.user).centerCrop())
                .into(this.ivMe);
        this.etPhoneNumber.setText(userInfo.getPhoneNumber());
        this.etMail.setText(userInfo.getEmail());
        this.etLastname.setText(userInfo.getLastname());
        this.etFirstname.setText(userInfo.getFirstname());

        this.btValidate.setOnClickListener(new ValidateClickListener());
        this.btCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVehicle(Constants.VEHICLE_CAR);
            }
        });
        this.btMoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVehicle(Constants.VEHICLE_MOTO);
            }
        });
        this.btTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVehicle(Constants.VEHICLE_VAN);
            }
        });
        this.btBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVehicle(Constants.VEHICLE_BICYCLE);
            }
        });
        this.sbWeight.setOnSeekBarChangeListener(new WeightChangeListener());
        this.sbDistance.setOnSeekBarChangeListener(new DistanceChangeListener());
        this.swAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Utils.isDelivering()) {
                    Toast.makeText(Utils.getContext(), "Impossible de désactiver le mode livreur pendant une livraison", Toast.LENGTH_SHORT).show();
                    return;
                }

                //display cardview for preferences if the deliveryman mode is on
                cvMyVehicle.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                LatLng lastPosition = PreferencesHelper.getInstance().getLastPosition();

                //get deliveryman profile and update current position
                for (Profile profile : Utils.getFullUserInfo().getProfiles()) {
                    if (profile.getPtype().compareTo(getString(R.string.deliveryman_string)) == 0) {
                        profile.setCurrentlyActive(BigDecimal.valueOf(isChecked ? 1 : 0));
                        if (lastPosition != null) {
                            profile.setLatitude(BigDecimal.valueOf(lastPosition.latitude));
                            profile.setLongitude(BigDecimal.valueOf(lastPosition.longitude));
                        }
                        new ProfileService().updateProfile(profile);
                    }
                }
            }
        });
    }

    /**
     * This method gets all the views
     */
    private void getViews(View view) {
        this.sbDistance = view.findViewById(R.id.sb_maxdistance);
        this.sbWeight = view.findViewById(R.id.sb_maxweight);
        this.tvMaxDistance = view.findViewById(R.id.tv_chosendistance);
        this.tvMaxWeight = view.findViewById(R.id.tv_chosenweight);
        this.btValidate = view.findViewById(R.id.bt_validate);
        this.swAvailable = view.findViewById(R.id.sw_available);
        this.btBike = view.findViewById(R.id.bt_bike);
        this.qrcode = view.findViewById(R.id.qrCodeImageView);
        this.btCar = view.findViewById(R.id.bt_car);
        this.btMoto = view.findViewById(R.id.bt_moto);
        this.btTruck = view.findViewById(R.id.bt_truck);
        this.cvMyVehicle = view.findViewById(R.id.cv_my_vehicle);
        this.btChangeUserInfo = view.findViewById(R.id.bt_change_userinfo);
        this.llMyName = view.findViewById(R.id.ll_my_name);
        this.llEditInfo = view.findViewById(R.id.ll_edit_info);
        this.etFirstname = view.findViewById(R.id.et_updateuserinfo_firstname);
        this.etLastname = view.findViewById(R.id.et_updateuserinfo_lastname);
        this.etMail = view.findViewById(R.id.et_updateuserinfo_mail);
        this.etPhoneNumber = view.findViewById(R.id.et_updateuserinfo_phone);
        this.pbUpdateUserInfo = view.findViewById(R.id.pb_change_userinfo);
        this.ivDeletePicture = view.findViewById(R.id.iv_delete_picture);
        this.tvAddPicture = view.findViewById(R.id.tv_add_picture);
        final TextView tvChangePassword = view.findViewById(R.id.tv_change_password);

        this.vehicleSelected = PreferencesHelper.getInstance().getDeliveryVehicle();
        selectVehicle(this.vehicleSelected);
        //set visibility ton gone if sender, otherwise set visibility to visible
        if (PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0) {
            this.cvMyVehicle.setVisibility(View.VISIBLE);
            this.swAvailable.setChecked(true);
        } else {
            this.cvMyVehicle.setVisibility(View.GONE);
            this.swAvailable.setChecked(false);
        }
        this.btChangeUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editing) {
                    llMyName.setVisibility(View.GONE);
                    llEditInfo.setVisibility(View.VISIBLE);
                    ivDeletePicture.setVisibility(View.VISIBLE);
                    btChangeUserInfo.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_save));
                    editing = true;
                } else {
                    updateUserInfo();
                    generateQR();
                }
            }
        });
        this.ivDeletePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAddPicture.setVisibility(View.VISIBLE);
                ivMe.setVisibility(View.GONE);
                ivDeletePicture.setVisibility(View.GONE);
            }
        });
        this.tvAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {

            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                if (provider.getLogins().containsKey(Constants.AWSTOKENVERIFICATIONURL)) {
                    tvChangePassword.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changePassword();
                        }
                    });
                } else {
                    tvChangePassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                //TODO relogin
            }
        });

        this.tvMaxDistance.setText(getString(R.string.max_distance, PreferencesHelper.getInstance().getDeliveryMaxDistance()));
        this.sbDistance.setProgress(PreferencesHelper.getInstance().getDeliveryMaxDistance());
        this.tvMaxWeight.setText(getString(R.string.max_weight, PreferencesHelper.getInstance().getDeliveryMaxWeight()));
        this.sbWeight.setProgress(PreferencesHelper.getInstance().getDeliveryMaxWeight());
    }

    /**
     * This method is called when a vehicle button is selected
     * It checks the correct vehicle and changes the max values for weight and distance
     * @param selectedVehicle
     */
    private void selectVehicle(String selectedVehicle) {
        this.vehicleSelected = selectedVehicle;
        this.btCar.setSelected(false);
        this.btBike.setSelected(false);
        this.btTruck.setSelected(false);
        this.btMoto.setSelected(false);

        if (this.vehicleSelected.compareTo(Constants.VEHICLE_CAR) == 0) {
            this.btCar.setSelected(true);
            this.sbDistance.setMax(100);
            this.sbWeight.setMax(20);
        }
        if (this.vehicleSelected.compareTo(Constants.VEHICLE_BICYCLE) == 0) {
            this.btBike.setSelected(true);
            this.sbDistance.setMax(20);
            this.sbWeight.setMax(5);
        }
        if (this.vehicleSelected.compareTo(Constants.VEHICLE_VAN) == 0) {
            this.btTruck.setSelected(true);
            this.sbDistance.setMax(100);
            this.sbWeight.setMax(50);
        }
        if (this.vehicleSelected.compareTo(Constants.VEHICLE_MOTO) == 0) {
            this.btMoto.setSelected(true);
            this.sbDistance.setMax(40);
            this.sbWeight.setMax(10);
        }
    }

    /**
     * This method is used to go to the change password fragment
     */
    private void changePassword() {
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, ChangePasswordFragment.newInstance()).addToBackStack(null).commit();
    }

    /**
     * This class implements the OnSeekBarChangeListener to update the max selected value
     * for the weight when the user changes it by sliding
     */
    private class WeightChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvMaxWeight.setText(getString(R.string.max_weight, progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //empty
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //empty
        }
    }

    /**
     * This class implements the OnSeekBarChangeListener to update the max selected value
     * for the distance when the user changes it by sliding
     */
    private class DistanceChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvMaxDistance.setText(getString(R.string.max_distance, progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //empty
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //empty
        }
    }

    /**
     * This class is used to handle the click on the validate button.
     * It makes a call to the create preferences webservices if they don't exist
     * otherwise it calls the update preferences web service
     */
    private class ValidateClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            btValidate.setEnabled(false);
            if (preferences == null) {
                preferences = new Preferences();
                preferences.setMaxDistance(BigDecimal.valueOf(sbDistance.getProgress()));
                preferences.setMaxWeight(BigDecimal.valueOf(sbWeight.getProgress()));
                preferences.setVtype(vehicleSelected);
                preferences.setProfileID(deliverymanProfileId);
                new ProfileService().createPreferences(preferences);
            } else {
                preferences.setMaxDistance(BigDecimal.valueOf(sbDistance.getProgress()));
                preferences.setMaxWeight(BigDecimal.valueOf(sbWeight.getProgress()));
                preferences.setVtype(vehicleSelected);
                new ProfileService().updatePreferences(preferences);
            }
        }
    }

    @Subscribe
    public void onEvent(UpdateProfileSuccessEvent event) {
        Toast.makeText(Utils.getContext(), "Mis à jour", Toast.LENGTH_SHORT).show();
        if (swAvailable.isChecked()) {
            if (getActivity() != null)
                ((MainActivity) getActivity()).startDeliverymanService();
        } else {
            if (getActivity() != null)
                ((MainActivity) getActivity()).stopDeliverymanService();
        }
        PreferencesHelper.getInstance().setDeliveryVehicle(vehicleSelected);
        PreferencesHelper.getInstance().setDeliveryMaxDistance(sbDistance.getProgress());
        PreferencesHelper.getInstance().setDeliveryMaxWeight(sbWeight.getProgress());
        PreferencesHelper.getInstance().setDeliveryManActivated(swAvailable.isChecked() ? Constants.PROFILETYPE_DELIVERYMAN : Constants.PROFILETYPE_SENDER);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());
        }
    }

    /**
     *
     */
    private void updateUserInfo() {
        if (!Utils.isEmail(this.etMail.getText().toString())) {
            this.etMail.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (!Utils.isPhoneNumberValid(this.etPhoneNumber.getText().toString()) || !this.etPhoneNumber.getText().toString().startsWith("+")) {
            this.etPhoneNumber.setError(getString(R.string.error_invalid_phone));
            return;
        }
        if (this.etFirstname.getText().toString().isEmpty()) {
            this.etFirstname.setError(getString(R.string.empty_field));
            return;
        }
        if (this.etLastname.getText().toString().isEmpty()) {
            this.etLastname.setError(getString(R.string.empty_field));
            return;
        }
        //if no image is set and no image is selected
        if (this.imageFilePath == null && this.ivMe.getVisibility() == View.GONE) {
            if (getActivity() != null)
                new AlertDialog.Builder(getActivity()).setMessage("Vous ne pouvez pas enregistrer sans avoir de photo, garder l'ancienne ?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
                                userInfo.setFirstname(etFirstname.getText().toString());
                                userInfo.setLastname(etLastname.getText().toString());
                                userInfo.setEmail(etMail.getText().toString());
                                userInfo.setPhoneNumber(etPhoneNumber.getText().toString());
                                if (imageFilePath != null) {
                                    String fileName = Utils.getUserId() + ImageFinder.getExtension(imageFilePath);
                                    userInfo.setPicture(Constants.USERSS3URL + fileName);
                                    pbUpdateUserInfo.setVisibility(View.VISIBLE);
                                    btChangeUserInfo.setVisibility(View.GONE);
                                    AWSUtils.uploadFile(Constants.USERSS3BUCKET, imageFilePath, fileName, Utils.getContext(), new UploadListener(userInfo));
                                } else {
                                    new ProfileService().updateUserInfo(userInfo);
                                    pbUpdateUserInfo.setVisibility(View.VISIBLE);
                                    btChangeUserInfo.setVisibility(View.GONE);
                                }
                            }
                        })
                        .setNegativeButton(R.string.add_picture, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //empty
                            }
                        })
                        .setTitle(R.string.no_picture).show();
            return;
        }

        UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
        if (userInfo != null) {
            userInfo.setEmail(etMail.getText().toString());
            userInfo.setFirstname(etFirstname.getText().toString());
            userInfo.setLastname(etLastname.getText().toString());
            userInfo.setPhoneNumber(etPhoneNumber.getText().toString());
            if (this.imageFilePath != null) {
                String fileName = Utils.getUserId() + ImageFinder.getExtension(this.imageFilePath);
                userInfo.setPicture(Constants.USERSS3URL + fileName);
                AWSUtils.uploadFile(Constants.USERSS3BUCKET, imageFilePath, fileName, Utils.getContext(), new UploadListener(userInfo));
            } else {
                userInfo.setPicture(Utils.getFullUserInfo().getInfos().get(0).getPicture());
                new ProfileService().updateUserInfo(userInfo);
            }
            pbUpdateUserInfo.setVisibility(View.VISIBLE);
            btChangeUserInfo.setVisibility(View.GONE);
        }
    }

    /**
     * This method requests the user to choose or take a picture
     */
    private void choosePicture() {
        Pix.start(this, REQUESTCODE);
    }

    /**
     * This method handles the activity result of the picture selection
     * @param requestCode the code used to identify the picture choice
     * @param resultCode the result code (mostly canceled or ok)
     * @param data the associated data of the intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUESTCODE) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if (returnValue.size() != 0) {
                this.imageFilePath = returnValue.get(0);
                Glide.with(Utils.getContext()).load(returnValue.get(0)).into(this.ivMe);//load image
                tvAddPicture.setVisibility(View.GONE);
                ivDeletePicture.setVisibility(View.GONE);
                ivMe.setVisibility(View.VISIBLE);
            }
        } else if (requestCode == REQUESTCODE) {
            ivDeletePicture.setVisibility(View.GONE);
        }
    }

    /**
     * This method is called when the user is asked permissions and handles the different cases depending on the result
     * @param requestCode the initial request code
     * @param permissions the requested permissions
     * @param grantResults the given results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this, REQUESTCODE);
                } else {
                    Toast.makeText(Utils.getContext(), "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * This class handles the picture transfer to the S3 bucket and updates the UI when needed, then it updates user infos
     */
    private class UploadListener implements TransferListener {
        private UserInfo info;

        UploadListener(UserInfo info) {
            this.info = info;
        }
        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("onError", "Error during upload: " + id, e);
            btChangeUserInfo.setVisibility(View.VISIBLE);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("onProgressChanged", "bytesCurrent " + bytesCurrent + " bytesTotal " + bytesTotal);
            int percent = (int) (((double) bytesCurrent / bytesTotal) * 100);
            Log.d("Pourcentage", String.valueOf(percent) + "%");
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d("onStateChanged", "State " + newState.name());
            if (newState == TransferState.COMPLETED) {
                Toast.makeText(Utils.getContext(), "Upload terminé", Toast.LENGTH_SHORT).show();
                new ProfileService().updateUserInfo(info);
            }
        }
    }

    /**
     * This method is used to disconnect from Cognito/Facebook/Google
     * It deletes the user ARN to avoid receiving push notifications that he doesn't need
     */
    private void disconnect() {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                //if the user logged in with credentials
                if (provider.getLogins().containsKey(Constants.AWSTOKENVERIFICATIONURL)) {
                    credentialsLogout();
                } else { //logged in with social connectors
                    //reset session's credentials and go to login activity
                    GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(Utils.getContext());
                    if (googleSignInAccount != null) {
                        googleLogout();
                    }

                    if (AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired()) {
                        facebookLogout();
                    }

                    provider.clearCredentials();
                    provider.clear();
                    if (getActivity() != null) {
                        ((MainActivity) getActivity()).stopDeliverymanService();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    }
                }
            }

            @Override
            public void onFailure(Exception exception) {
                //TODO relogin
            }
        });
    }

    private void facebookLogout() {
        LoginManager.getInstance().logOut();
    }

    private void googleLogout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_SIGNIN_CLIENTID)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Utils.getContext(), gso);
        mGoogleSignInClient.signOut();
    }

    private void credentialsLogout() {
        AWSUtils.logout(new GenericHandler() {
            @Override
            public void onSuccess() {
                Log.d("Logged out", "Success");
                //reset password, finish activity and go to login activity
                PreferencesHelper.getInstance().setPassword(null);
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).stopDeliverymanService();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                Log.d("Logged out", "Failure " + exception.getMessage());
            }
        });
    }

    /**
     * Received event
     * @param event the event's name is explicit enough to explain the method behavior
     */
    @Subscribe
    public void onEvent(UpdateProfileFailureEvent event) {
        Toast.makeText(Utils.getContext(), "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show();
        btValidate.setEnabled(true);
    }

    @Subscribe
    public void onEvent(GetPreferencesSuccessEvent event) {
        this.preferences = event.getPreferences();
        if (this.preferences != null) {
            Log.d("preferences id", this.preferences.getPreferenceID());

            this.vehicleSelected = preferences.getVtype();
            selectVehicle(this.vehicleSelected);

            this.tvMaxDistance.setText(getString(R.string.max_distance, preferences.getMaxDistance().intValue()));
            this.sbDistance.setProgress(preferences.getMaxDistance().intValue());
            this.tvMaxWeight.setText(getString(R.string.max_weight, preferences.getMaxWeight().intValue()));
            this.sbWeight.setProgress(preferences.getMaxWeight().intValue());

            PreferencesHelper.getInstance().setDeliveryVehicle(vehicleSelected);
            PreferencesHelper.getInstance().setDeliveryMaxDistance(sbDistance.getProgress());
            PreferencesHelper.getInstance().setDeliveryMaxWeight(sbWeight.getProgress());
        } else
            Log.d("GetPreferencesSuccessEv", "Null");
        this.btValidate.setEnabled(true);
    }

    @Subscribe
    public void onEvent(GetPreferencesFailureEvent event) {
        if (event.getFailure().getCode() == 404) {
            this.btValidate.setEnabled(true);
            Toast.makeText(Utils.getContext(), "Veuillez renseigner vos préférences", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(Utils.getContext(), "Erreur lors de la récupération des préférences", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(CreatePreferencesSuccessEvent event) {
        if (event.getPreferences() != null)
            Log.d("CreatePreferencesScsEve", "Not null");
        this.preferences = event.getPreferences();
        PreferencesHelper.getInstance().setDeliveryVehicle(vehicleSelected);
        PreferencesHelper.getInstance().setDeliveryMaxDistance(sbDistance.getProgress());
        PreferencesHelper.getInstance().setDeliveryMaxWeight(sbWeight.getProgress());
        PreferencesHelper.getInstance().setDeliveryManActivated(swAvailable.isChecked() ? Constants.PROFILETYPE_DELIVERYMAN : Constants.PROFILETYPE_SENDER);
        this.btValidate.setEnabled(true);
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, ParametersValidatedFragment.newInstance(vehicleSelected)).addToBackStack(null).commit();
    }

    @Subscribe
    public void onEvent(CreatePreferencesFailureEvent event) {
        Toast.makeText(Utils.getContext(), "Erreur lors de l'enregistrement des préférences", Toast.LENGTH_SHORT).show();
        this.btValidate.setEnabled(true);
    }

    @Subscribe
    public void onEvent(UpdatePreferencesSuccessEvent event) {
        if (event.getPreferences() != null)
            Log.d("UpdatePreferencesScsEve", "Not null");
        this.preferences = event.getPreferences();
        PreferencesHelper.getInstance().setDeliveryVehicle(vehicleSelected);
        PreferencesHelper.getInstance().setDeliveryMaxDistance(sbDistance.getProgress());
        PreferencesHelper.getInstance().setDeliveryMaxWeight(sbWeight.getProgress());
        PreferencesHelper.getInstance().setDeliveryManActivated(swAvailable.isChecked() ? Constants.PROFILETYPE_DELIVERYMAN : Constants.PROFILETYPE_SENDER);
        this.btValidate.setEnabled(true);
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, ParametersValidatedFragment.newInstance(vehicleSelected)).addToBackStack(null).commit();

    }

    @Subscribe
    public void onEvent(UpdatePreferencesFailureEvent event) {
        Toast.makeText(Utils.getContext(), "Erreur lors de la modification des préférences", Toast.LENGTH_SHORT).show();
        btValidate.setEnabled(true);
    }

    @Subscribe
    public void onEvent(UpdateUserInfoSuccessEvent event) {
        if (this.disconnecting) {
            disconnect();
        } else {
            Utils.getFullUserInfo().getInfos().remove(0);
            Utils.getFullUserInfo().getInfos().add(event.getUserInfo());
            if (getActivity() != null)
                ((MainActivity) getActivity()).refreshIdentity(true);
            this.btChangeUserInfo.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            this.editing = false;
            this.btChangeUserInfo.setVisibility(View.VISIBLE);
            this.pbUpdateUserInfo.setVisibility(View.GONE);
            this.llMyName.setVisibility(View.VISIBLE);
            this.tvAddPicture.setVisibility(View.GONE);
            this.ivDeletePicture.setVisibility(View.GONE);
            this.ivMe.setVisibility(View.VISIBLE);
            this.llEditInfo.setVisibility(View.GONE);
            this.tvMyName.setText(getString(R.string.formatted_name, event.getUserInfo().getFirstname(), event.getUserInfo().getLastname()));
        }
    }

    @Subscribe
    public void onEvent(UpdateUserInfoFailureEvent event) {
        this.btChangeUserInfo.setVisibility(View.VISIBLE);
        this.pbUpdateUserInfo.setVisibility(View.GONE);
        Toast.makeText(Utils.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(GetFullUserInfoSuccessEvent event) {
        initUserInfo(event.getFullUserInfo().getInfos().get(0));
    }

    @Subscribe
    public void onEvent(GetFullUserInfoFailureEvent event) {
        Toast.makeText(Utils.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
    }
}
