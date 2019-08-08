package com.app.livit.fragment.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;
import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.activity.MainActivity;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;

/**
 * Created by Rémi OLLIVIER on 05/04/2018.
 */

public class ProfileChoiceFragment extends Fragment {

    String email;
    String password;
    CognitoUserAttributes attr;
    private ProgressBar pb;
    private boolean isClicked = false;


    public static ProfileChoiceFragment newInstance() {

        ProfileChoiceFragment fragment = new ProfileChoiceFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_profile_choice, container, false);

        //init view
        final CardView cvDeliveryman = view.findViewById(R.id.cv_deliveryman);
        final CardView cvSender = view.findViewById(R.id.cv_sender);

        //go to the main activity with the correct role
        cvDeliveryman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                attr.addAttribute("nickname", "Liv'vit Pro");
                v.setClickable(false);
                cvSender.setClickable(false);
                isClicked = true;
                signup();
            }
        });
        cvSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attr.addAttribute("nickname", "Individual Liv'vit");
                v.setClickable(false);
                cvDeliveryman.setClickable(false);
                isClicked = true;
                signup();

            }
        });

        email = ((LoginActivity) getActivity()).getEmail();
        password = ((LoginActivity) getActivity()).getPassword();
        attr = ((LoginActivity) getActivity()).getUserAttributes();
        this.pb = view.findViewById(R.id.pb_createaccount);


        return view;
    }


    public void signup() {
        if (isClicked)
        {
            if (getActivity() == null) {
                Toast.makeText(Utils.getContext(), "Impossible de traiter la demande", Toast.LENGTH_SHORT).show();
                return;
            }
            this.pb.setVisibility(View.VISIBLE);
            ((LoginActivity) getActivity()).getCognitoUserPool().signUpInBackground(this.email, this.password, attr, null, new SignUpHandler() {
                @Override
                public void onSuccess(CognitoUser user, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                    pb.setVisibility(View.GONE);
                    if (!userConfirmed) {
                        // This user must be confirmed and a confirmation code was sent to the user
                        // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
                        // Get the confirmation code from user
                        Toast.makeText(Utils.getContext(), R.string.email_with_activation_link_sent, Toast.LENGTH_LONG).show();
                        if (getActivity() != null)
                            ((LoginActivity) getActivity()).goToLoginFragment();
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    pb.setVisibility(View.GONE);
                    if (exception.getClass() == UsernameExistsException.class) {
                        Toast.makeText(Utils.getContext(), R.string.account_already_existing, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Utils.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        Log.e("onFailure", exception.toString());
                    }
                }
            });
        }
    }

}
