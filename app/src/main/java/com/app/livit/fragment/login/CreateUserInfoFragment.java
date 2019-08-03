package com.app.livit.fragment.login;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.event.userinfo.CreateProfileFailureEvent;
import com.app.livit.event.userinfo.CreateProfileSuccessEvent;
import com.app.livit.event.userinfo.CreateUserInfoFailureEvent;
import com.app.livit.event.userinfo.CreateUserInfoSuccessEvent;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.ImageFinder;
import com.app.livit.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.test.model.Profile;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rémi OLLIVIER on 18/06/2018.
 */

public class CreateUserInfoFragment extends Fragment {
    private static final String EMAIL = "EMAIL";
    private static final String FIRSTNAME = "FIRSTNAME";
    private static final String LASTNAME = "LASTNAME";
    private static final String PICTUREURL = "PICTUREURL";
    private static final int REQUESTCODE = 12;

    private EditText etPhoneNumber;//todo begin phone number by +225 for prod, keep +33 for the demo
    private EditText etMail;
    private EditText etFirstname;
    private EditText etLastname;
    private CircleImageView ivUser;
    private ImageView ivDeletePicture;
    private TextView tvAddPicture;
    private Button btCreateUserInfo;
    private ProgressBar pb;
    private String imageFilePath;
    private UserInfo userInfo;
    private ProfileService profileService;
    private String pictureUrl;

    public static CreateUserInfoFragment newInstance(String email, String firstname, String lastname, String pictureUrl) {

        //when we create the fragment, put info in the bundle to use it in
        CreateUserInfoFragment fragment = new CreateUserInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EMAIL, email);
        bundle.putString(FIRSTNAME, firstname);
        bundle.putString(LASTNAME, lastname);
        bundle.putString(PICTUREURL, pictureUrl);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_create_userinfo, container, false);
        this.etPhoneNumber = view.findViewById(R.id.et_createuserinfo_phone);
        this.etMail = view.findViewById(R.id.et_createuserinfo_mail);
        this.etFirstname = view.findViewById(R.id.et_createuserinfo_firstname);
        this.etLastname = view.findViewById(R.id.et_createuserinfo_lastname);
        this.btCreateUserInfo = view.findViewById(R.id.bt_createuserinfo);
        this.pb = view.findViewById(R.id.pb_createuserinfo);
        this.ivUser = view.findViewById(R.id.iv_userinfo_picture);
        this.tvAddPicture = view.findViewById(R.id.tv_userinfo_addpicture);
        this.ivDeletePicture = view.findViewById(R.id.iv_delete_picture);
        this.profileService = new ProfileService();

        this.tvAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        this.ivDeletePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAddPicture.setVisibility(View.VISIBLE);
                ivUser.setVisibility(View.GONE);
                ivDeletePicture.setVisibility(View.GONE);
                imageFilePath = null;
            }
        });

        //if there are arguments, set up the view
        if (getArguments() != null) {
            this.etMail.setText(getArguments().getString(EMAIL));
            this.etFirstname.setText(getArguments().getString(FIRSTNAME));
            this.etLastname.setText(getArguments().getString(LASTNAME));
            this.pictureUrl = getArguments().getString(PICTUREURL);
            if (this.pictureUrl != null) {
                Glide.with(Utils.getContext()).load(this.pictureUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        tvAddPicture.setVisibility(View.VISIBLE);
                        ivUser.setVisibility(View.GONE);
                        ivDeletePicture.setVisibility(View.GONE);
                        pictureUrl = null;//if the link is invalid, no picture
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        tvAddPicture.setVisibility(View.GONE);
                        ivUser.setVisibility(View.VISIBLE);
                        ivDeletePicture.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).into(this.ivUser);
            }
        }

        this.btCreateUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserInfo();
            }
        });

        return view;
    }

    /**
     * This method checks if the info are fully filled.
     * If the info are fully filled it creates the user info
     * Otherwise it displays an error to the user
     */
    private void createUserInfo() {
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
        if (this.imageFilePath == null && this.pictureUrl == null) {
            Toast.makeText(Utils.getContext(), "Veuillez ajouter votre photo", Toast.LENGTH_SHORT).show();
            return;
        }

        userInfo = new UserInfo();
        userInfo.setFirstname(this.etFirstname.getText().toString());
        userInfo.setLastname(this.etLastname.getText().toString());
        userInfo.setEmail(this.etMail.getText().toString());
        userInfo.setPhoneNumber(this.etPhoneNumber.getText().toString());

        //first step of the creation process : upload the picture
        if (this.imageFilePath != null) {
            String fileName = Utils.getUserId() + ImageFinder.getExtension(this.imageFilePath);
            userInfo.setPicture(Constants.USERSS3URL + fileName);
            AWSUtils.uploadFile(Constants.USERSS3BUCKET, imageFilePath, fileName, Utils.getContext(), new UploadListener());
            pb.setIndeterminate(false);
            pb.setProgress(0);
        } else {
            userInfo.setPicture(this.pictureUrl);
            pb.setIndeterminate(true);
            profileService.createUserInfo(userInfo);
        }
        pb.setVisibility(View.VISIBLE);
        btCreateUserInfo.setClickable(false);
        this.pb.setVisibility(View.VISIBLE);
    }

    /**
     * Lifecycle events
     */
    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (this.imageFilePath != null) { //load image if it has already been choosen by the user
            Glide.with(Utils.getContext())
                    .load(this.imageFilePath)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ivUser.setVisibility(View.GONE);
                            ivDeletePicture.setVisibility(View.GONE);
                            tvAddPicture.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ivUser.setVisibility(View.VISIBLE);
                            ivDeletePicture.setVisibility(View.VISIBLE);
                            tvAddPicture.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivUser);
        } else if (this.pictureUrl != null) {
            Glide.with(Utils.getContext())
                    .load(this.pictureUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ivUser.setVisibility(View.GONE);
                            ivDeletePicture.setVisibility(View.GONE);
                            tvAddPicture.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ivUser.setVisibility(View.VISIBLE);
                            ivDeletePicture.setVisibility(View.VISIBLE);
                            tvAddPicture.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivUser);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(CreateUserInfoSuccessEvent event) {
        pb.setProgress(33);//1/3 of the process
        Utils.getFullUserInfo().getInfos().add(event.getUserInfo());
        Profile profile = new Profile();
        profile.setPtype(getString(R.string.deliveryman_string));
        profile.setCurrentlyActive(BigDecimal.ZERO);
        this.profileService.createProfile(profile);
    }

    @Subscribe
    public void onEvent(CreateUserInfoFailureEvent event) {
        this.pb.setVisibility(View.GONE);
        Log.e("CreateUserInfoFailureEv", getString(R.string.error));
        Toast.makeText(Utils.getContext(), "Il y a eu une erreur", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(CreateProfileSuccessEvent event) {
        Utils.getFullUserInfo().getProfiles().add(event.getProfile());
        if (event.getProfile().getPtype().compareTo(getString(R.string.deliveryman_string)) == 0) {
            Profile profile = new Profile();
            profile.setPtype(getString(R.string.sender_string));
            profile.setCurrentlyActive(BigDecimal.ZERO);
            profileService.createProfile(profile);
            pb.setProgress(66);//2/3 of the process done
        } else {
            pb.setProgress(100);//process done
            pb.setVisibility(View.GONE);
            if (getActivity() != null) {
                ((LoginActivity) getActivity()).goToRoleChoiceFragment();
            }
        }
    }

    @Subscribe
    public void onEvent(CreateProfileFailureEvent event) {
        Toast.makeText(Utils.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is used to make the user choose a picture
     */
    private void choosePicture() {
        Pix.start(this, REQUESTCODE);
    }

    /**
     * Handle the result of the image picker
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the date returned by the image picker
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUESTCODE) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if (returnValue.size() != 0) {
                this.imageFilePath = returnValue.get(0);
                Glide.with(Utils.getContext()).load(returnValue.get(0)).into(this.ivUser);//load image
                tvAddPicture.setVisibility(View.GONE);
                ivDeletePicture.setVisibility(View.VISIBLE);
                ivUser.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * This method handles the permission request result
     * @param requestCode the request code
     * @param permissions the list of permissions
     * @param grantResults the result of each permission requested
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

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("onError", "Error during upload: " + id, e);
            btCreateUserInfo.setClickable(true);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("onProgressChanged", "bytesCurrent " + bytesCurrent + " bytesTotal " + bytesTotal);
            int percent = (int) (((double) bytesCurrent / bytesTotal) * 100);
            Log.d("Pourcentage", String.valueOf(percent) + "%");
            pb.setProgress(percent);
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d("onStateChanged", "State " + newState.name());
            if (newState == TransferState.COMPLETED) {
                pb.setProgress(0);
                Toast.makeText(Utils.getContext(), "Upload terminé", Toast.LENGTH_SHORT).show();
                profileService.createUserInfo(userInfo);
            } else if (newState == TransferState.FAILED) {
                pb.setProgress(0);
                pb.setVisibility(View.GONE);
                Toast.makeText(Utils.getContext(), "Erreur lors de l'upload", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
