package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.utils.FirebaseUtils;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class AuthActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    //FOR DESIGN
    @BindView(R.id.login_facebook_imv) ImageView login_facebook_btn;
    @BindView(R.id.login_google_imv) ImageView login_google_btn;
    @BindView(R.id.auth_activity_linear_layout) LinearLayout auth_linear_layout;

    //FOR DATA
    private static final int RC_SIGN_IN = 123;
    private static final int SIGN_OUT_TASK = 10;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String PERMS = Manifest.permission.ACCESS_FINE_LOCATION;
    String language;
    String TAG_LANGUAGE = "TAG_LANGUAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        configureLanguage();
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

    // -------
    // ACTIONS
    // -------

    // Authentification with Facebook
    @OnClick(R.id.login_facebook_imv)
    public void onClickFacebookLogin() {
        if (checkProviderLogged().equals("facebook")){
            enableLocation();
        } else if (checkProviderLogged().equals("google")) {
            signOutUserFromFirebase();
            startSignInFacebook();
        }
        else {
            startSignInFacebook();
        }
    }

    // Authentification with Google
    @OnClick(R.id.login_google_imv)
    public void onClickGoogleLogin() {
        if (checkProviderLogged().equals("google")){
            enableLocation();
        } else if (checkProviderLogged().equals("facebook")) {
            signOutUserFromFirebase();
            startSignInGooglePlus();
        }
        else {
            startSignInGooglePlus();
        }
    }

    // ----
    // UI
    // ----

    private void showSnackBar(LinearLayout linearLayout, String message){
        Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    //Update logo from sign in with to continue with
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

    // --------------------
    // LANGUAGE PREFERENCES
    // --------------------

    //Get Language of the Application : Local or chosen one by user in the application
    private void configureLanguage(){
        language = SharedPreferencesUtility.getString(this,TAG_LANGUAGE);
        if (language != null){
            switch (language){
                case "Français":
                    Locale locale_fr = new Locale("fr");
                    Locale.setDefault(locale_fr);
                    Configuration config_fr = new Configuration();
                    config_fr.locale = locale_fr;
                    getBaseContext().getResources().updateConfiguration(config_fr,getBaseContext().getResources().getDisplayMetrics());
                    break;
                case "English":
                    Locale locale_en = new Locale("en");
                    Locale.setDefault(locale_en);
                    Configuration config_en = new Configuration();
                    config_en.locale = locale_en;
                    getBaseContext().getResources().updateConfiguration(config_en,getBaseContext().getResources().getDisplayMetrics());
                    break;
                default:
                    break;
            }
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

    // Check Authentification provider : Google or Facebook
    private String checkProviderLogged(){
        if (FirebaseUtils.isCurrentUserLogged()){
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

    // Intent to begin MainActivity
    private void startMainActivity() {
        Intent login = new Intent(this, MainActivity.class);
        startActivity(login);
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                createUserInFirestore();
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

    // --------------------
    // PERMISSIONS
    // --------------------

    private void enableLocation(){
        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, LOCATION_PERMISSION_REQUEST_CODE, PERMS)
                        .setRationale(R.string.popup_title_permission_location_access)
                        .setPositiveButtonText(R.string.popup_message_answer_yes)
                        .setNegativeButtonText(R.string.popup_message_answer_no)
                        .build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startMainActivity();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    // --------------------
    // REST REQUEST
    // --------------------

    private void createUserInFirestore(){
        UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);

                if (currentUser == null){
                    String urlPicture = (FirebaseUtils.getCurrentUser().getPhotoUrl() != null) ? FirebaseUtils.getCurrentUser().getPhotoUrl().toString() : null;
                    String username = FirebaseUtils.getCurrentUser().getDisplayName();
                    String uid = FirebaseUtils.getCurrentUser().getUid();

                    UserHelper.createUser(uid, username, urlPicture, "XXX","XXX", "XX-XX-XXXX").addOnFailureListener(onFailureListener());
                } else {

                }
            }
        });
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }


}
