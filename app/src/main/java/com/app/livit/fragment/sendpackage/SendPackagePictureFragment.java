package com.app.livit.fragment.sendpackage;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.livit.R;
import com.app.livit.activity.SendPackageActivity;
import com.app.livit.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rémi OLLIVIER on 22/05/2018.
 */

public class SendPackagePictureFragment extends Fragment {
    private static final int REQUESTCODE = 137;

    private ImageView ivPackage;
    private TextView tvAddPicture;
    private ImageView ivDeletePicture;
    private String imageFilePath;

    public static SendPackagePictureFragment newInstance() {

        SendPackagePictureFragment fragment = new SendPackagePictureFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_delivery_steppicture, container, false);

        this.ivPackage = view.findViewById(R.id.iv_delivery_picture);
        this.tvAddPicture = view.findViewById(R.id.tv_delivery_addpicture);
        this.ivDeletePicture = view.findViewById(R.id.iv_delete_picture);
        Button btValidate = view.findViewById(R.id.bt_delivery_finish);
        btValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((SendPackageActivity) getActivity()).goToNextFragment(3);
            }
        });

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
                ivPackage.setVisibility(View.GONE);
                ivDeletePicture.setVisibility(View.GONE);
            }
        });

        return view;
    }

    /**
     * Lifecycle events
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && ((SendPackageActivity) getActivity()).getPhotoPath() != null) {
            Glide.with(Utils.getContext())
                    .load(((SendPackageActivity) getActivity()).getPhotoPath())
                    .apply(new RequestOptions().error(R.drawable.package_image).centerCrop())
                    .into(ivPackage);
            this.ivPackage.setVisibility(View.VISIBLE);
            this.ivDeletePicture.setVisibility(View.VISIBLE);
            this.tvAddPicture.setVisibility(View.GONE);
        }
    }

    /**
     * This method requests the user to pick a picture
     */
    private void choosePicture() {
        Pix.start(this, REQUESTCODE);
    }

    /**
     * This method gets the result of the picture pick requested to the user
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the extra data contained in the result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUESTCODE) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if (returnValue.size() != 0) { //if there is a return value, get the first result
                this.imageFilePath = returnValue.get(0);
                if (getActivity() != null) {
                    Uri photoURI = Uri.fromFile(new File(imageFilePath));
                    ((SendPackageActivity) getActivity()).setPhotoPath(photoURI);//set the picture path in activity
                }
                Glide.with(Utils.getContext()).load(returnValue.get(0)).into(this.ivPackage);//load image
                tvAddPicture.setVisibility(View.GONE);
                ivDeletePicture.setVisibility(View.VISIBLE);
                ivPackage.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * This method is called after a permission request has been done
     * @param requestCode the request code
     * @param permissions the requested permissions
     * @param grantResults the results for the requested permissions
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
}