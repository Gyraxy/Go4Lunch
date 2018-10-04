package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.duboscq.nicolas.go4lunch.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthActivity extends AppCompatActivity {

    //FOR DESIGN
    @BindView(R.id.login_facebook_imv) ImageView login_facebook_btn;
    @BindView(R.id.login_google_imv) ImageView login_google_btn;
    @BindView(R.id.auth_activity_linear_layout) LinearLayout auth_linear_layout;

    //FOR DATA
    private static final int RC_SIGN_IN = 123;
    private static final int SIGN_OUT_TASK = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logoUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    //ACTIONS

    @OnClick(R.id.login_facebook_imv)
    public void onClickFacebookLogin() {
        if (checkProviderLogged().equals("facebook")){
            startMainActivity();
        } else if (checkProviderLogged().equals("google")) {
            signOutUserFromFirebase();
            startSignInFacebook();
        }
        else {
            startSignInFacebook();
        }
    }

    @OnClick(R.id.login_google_imv)
    public void onClickGoogleLogin() {
        if (checkProviderLogged().equals("google")){
            startMainActivity();
        } else if (checkProviderLogged().equals("facebook")) {
            signOutUserFromFirebase();
            startSignInGooglePlus();
        }
        else {
            startSignInGooglePlus();
        }
    }

    public void startMainActivity() {
        Intent login = new Intent(this, MainActivity.class);
        startActivity(login);
    }

    // ----
    // UI
    // ----

    private void showSnackBar(LinearLayout linearLayout, String message){
        Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void logoUpdate(){
        String provider = checkProviderLogged();
        switch (provider) {
            case "facebook":
                login_google_btn.setImageResource(R.drawable.google_login);
                login_facebook_btn.setImageResource(R.drawable.facebook_logged);
                break;
            case "google":
                login_facebook_btn.setImageResource(R.drawable.facebook_login);
                login_google_btn.setImageResource(R.drawable.google_logged);
                break;
            default:
                login_facebook_btn.setImageResource(R.drawable.facebook_login);
                login_google_btn.setImageResource(R.drawable.google_login);
                break;
        }
    }

    // ----------------
    // AUTHENTIFICATION
    // ----------------

    // Authentication mode: Google
    private void startSignInGooglePlus(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .build(),
                RC_SIGN_IN);
    }

    // Authentication mode: Facebook
    private void startSignInFacebook(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .build(),
                RC_SIGN_IN);
    }

    private String checkProviderLogged(){
        if (isCurrentUserLogged()){
            if (FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(1).getProviderId().equals("google.com")){
                return "google";
            } else if (FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(1).getProviderId().equals("facebook.com")) {
                return "facebook";
            } else return "null";
        } else return "null";
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected Boolean isCurrentUserLogged(){
        return (
            this.getCurrentUser() != null);
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                showSnackBar(this.auth_linear_layout, getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.auth_linear_layout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.auth_linear_layout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.auth_linear_layout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    private void signOutUserFromFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin) {
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin) {
                    case SIGN_OUT_TASK:
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
