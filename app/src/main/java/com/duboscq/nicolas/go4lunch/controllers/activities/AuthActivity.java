package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthActivity extends AppCompatActivity {

    //FOR DESIGN
    @BindView(R.id.login_facebook_imv)
    ImageView login_facebook_btn;
    @BindView(R.id.login_google_imv)
    ImageView login_google_btn;

    //FOR DATA
    private static final int RC_SIGN_IN = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //ACTIONS

    @OnClick(R.id.login_facebook_imv)
    public void onClickFacebookLogin() {
        if (isCurrentUserLogged()){
            startMainActivity();
        } else {
            startSignInFacebook();
        }
    }

    @OnClick(R.id.login_google_imv)
    public void onClickGoogleLogin() {
        if (isCurrentUserLogged()){
            startMainActivity();
        } else {
            startSignInGooglePlus();
        }
    }

    public void startMainActivity() {
        Intent login = new Intent(this, MainActivity.class);
        startActivity(login);
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

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected Boolean isCurrentUserLogged(){
        return (
            this.getCurrentUser() != null);
    }
}
