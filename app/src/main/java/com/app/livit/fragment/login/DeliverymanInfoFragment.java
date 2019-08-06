package com.app.livit.fragment.login;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.activity.MainActivity;
import com.app.livit.model.DmanDetails;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;
import com.bumptech.glide.Glide;
import com.facebook.login.Login;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.model.UserInfo;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Rémi OLLIVIER on 18/06/2018.
 */

public class DeliverymanInfoFragment extends Fragment {
    private static final String EMAIL = "EMAIL";
    private static final String FIRSTNAME = "FIRSTNAME";
    private static final String LASTNAME = "LASTNAME";
    private static final String PICTUREURL = "PICTUREURL";
    private static final int REQUESTCODE = 12;

    private EditText etPhoneNumber;//todo begin phone number by +225 for prod, keep +33 for the demo
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

    public static DeliverymanInfoFragment newInstance() {

        //when we create the fragment, put info in the bundle to use it in
        DeliverymanInfoFragment fragment = new DeliverymanInfoFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_deliveryman_details, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.etPhoneNumber = view.findViewById(R.id.number_to_verify);
        this.etFirstname = view.findViewById(R.id.residance);
        this.etLastname = view.findViewById(R.id.age);
        this.btCreateUserInfo = view.findViewById(R.id.bt_createuserinfo);
        this.pb = view.findViewById(R.id.pb_createuserinfo);
        this.ivUser = view.findViewById(R.id.iv_userinfo_picture);
        this.tvAddPicture = view.findViewById(R.id.tv_userinfo_addpicture);
        this.ivDeletePicture = view.findViewById(R.id.iv_delete_picture);

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

        this.btCreateUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!Utils.isPhoneNumberValid(etPhoneNumber.getText().toString()) || !etPhoneNumber.getText().toString().startsWith("+")) {
                    etPhoneNumber.setError(getString(R.string.error_invalid_phone));
                    return;
                }
                if (etFirstname.getText().toString().isEmpty()) {
                    etFirstname.setError(getString(R.string.empty_field));
                    return;
                }
                if (etLastname.getText().toString().isEmpty()) {
                    etLastname.setError(getString(R.string.empty_field));
                    return;
                }
                if (imageFilePath == null && pictureUrl == null) {
                    Toast.makeText(Utils.getContext(), "Veuillez ajouter votre photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                  Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadDetails();
                    }
                });
                  t.start();
            }
        });

    }

    private void uploadDetails() {
        UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
        DmanDetails user = new DmanDetails(userInfo.getFirstname(), userInfo.getLastname(),this.etLastname.getText().toString(),this.etFirstname.getText().toString(),this.etPhoneNumber.getText().toString(),userInfo.getEmail(),"No");

        ((LoginActivity)getActivity()).getmDatabase().child(userInfo.getUserID()).setValue(user);
        uploadImage(userInfo);

        ((LoginActivity)getActivity()).goToRoleChoiceFragment();
    }


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
        if (resultCode == RESULT_OK && requestCode == REQUESTCODE) {
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


    public void uploadImage(UserInfo user)
    {


        Uri file = Uri.fromFile(new File(imageFilePath));
        StorageReference storageRef = ((LoginActivity)getActivity()).getStorage().getReference();
        UploadTask uploadTask = storageRef.child("images/"+user.getUserID()+"Id-photo").putFile(file);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

    }

}

