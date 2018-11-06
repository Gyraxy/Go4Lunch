package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingProfileActivity extends AppCompatActivity {

    //FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_setting_language_fr_rbtn) RadioButton french_rbtn;
    @BindView(R.id.activity_setting_language_en_rbtn) RadioButton english_rbtn;
    @BindView(R.id.activity_setting_switch) Switch notification_switch;
    @BindView(R.id.activity_profile_image_imv) ImageView profile_imv;
    @BindView(R.id.activity_profile_username_edt) EditText username_update_edt;
    @BindView(R.id.activity_profile_progress_bar) ProgressBar progressBar;

    @BindView(R.id.activity_profile_image_layout) RelativeLayout profile_layout;
    @BindView(R.id.activity_setting_switch_layout) LinearLayout setting_switch_layout;
    @BindView(R.id.activity_setting_language_layout) LinearLayout setting_language_layout;
    @BindView(R.id.activity_setting_view) View setting_view;

    //FOR DATA
    String radio_language;
    String language;
    String TAG_LANGUAGE = "TAG_LANGUAGE";
    String activity;

    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        ButterKnife.bind(this);
        activity = getIntent().getExtras().getString("Activity");
        if (activity.equals("Settings")){
            setSettingLayout();
            getLanguage();
            configureToolBar();
        } else if (activity.equals("Profile")){
            setProfile_layout();
            configureToolBar();
        }
    }

    //--------
    // ACTIONS
    //--------

    @OnClick({R.id.activity_setting_language_fr_rbtn, R.id.activity_setting_language_en_rbtn})
    public void onRadioButtonClicked(RadioButton radioButton) {
        boolean checked = radioButton.isChecked();
        switch (radioButton.getId()) {
            case R.id.activity_setting_language_fr_rbtn:
                if (checked) {
                    AlertDialog.Builder language_diag = new AlertDialog.Builder(this);
                    language_diag.setMessage(getString(R.string.dialog_message))
                            .setPositiveButton(getString(R.string.dialog_btn_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Locale locale = new Locale("fr");
                                    Locale.setDefault(locale);
                                    Configuration config = new Configuration();
                                    config.locale = locale;
                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                    SharedPreferencesUtility.putString(getApplicationContext(), TAG_LANGUAGE, "français");
                                    Intent refresh = new Intent(SettingProfileActivity.this, AuthActivity.class);
                                    startActivity(refresh);
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.dialog_btn_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    language_diag.show();
                }
                break;
            case R.id.activity_setting_language_en_rbtn:
                if (checked) {
                    AlertDialog.Builder language_diag = new AlertDialog.Builder(this);
                    language_diag.setMessage(getString(R.string.dialog_message))
                            .setPositiveButton(getString(R.string.dialog_btn_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Locale locale = new Locale("en");
                                    Locale.setDefault(locale);
                                    Configuration config = new Configuration();
                                    config.locale = locale;
                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                    SharedPreferencesUtility.putString(getApplicationContext(),TAG_LANGUAGE,"English");
                                    Intent refresh = new Intent(SettingProfileActivity.this, AuthActivity.class);
                                    startActivity(refresh);
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.dialog_btn_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    language_diag.show();
                }
                break;
        }
    }

    @OnClick (R.id.activity_profile_delete_floating_btn)
    public void deleteAccount(){
        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_answer_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressBar.setVisibility(View.VISIBLE);
                        deleteUserFromFirebase();
                    }
                })
                .setNegativeButton(R.string.popup_message_answer_no, null)
                .show();
    }

    @OnClick (R.id.activity_profile_edit_floating_btn)
    public void updateUsername(){
        this.updateUsernameInFirebase();
        Toast.makeText(this,getString(R.string.username_updated),Toast.LENGTH_SHORT).show();
    }

    //--------------
    // CONFIGURATION
    //--------------

    //TOOLBAR

    private void configureToolBar(){
        if (activity.equals("Settings")){
            toolbar.setTitle(R.string.toolbar_title_settings);
        } else if (activity.equals("Profile")){
            toolbar.setTitle(R.string.toolbar_title_profile);
        }
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    //CHECK DEVICE LANGUAGE OR SAVED LANGUAGE AND CHECK RADIOBUTTON

    private void getLanguage(){
        language = SharedPreferencesUtility.getString(this,TAG_LANGUAGE);
        if (language == null){
            radio_language = Locale.getDefault().getDisplayLanguage();
        } else radio_language = language;

        switch (radio_language){
            case "français":
                french_rbtn.setChecked(true);
                break;
            case "English":
                english_rbtn.setChecked(true);
                break;
                default:
                    break;
        }
    }

    //LAYOUT CONFIGURATION DEPEND IF PROFILE OR SETTING

    private void setSettingLayout(){
        profile_layout.setVisibility(View.GONE);
    }

    private void setProfile_layout(){
        setting_language_layout.setVisibility(View.GONE);
        setting_switch_layout.setVisibility(View.GONE);
        setting_view.setVisibility(View.GONE);

        if (this.getCurrentUser().getPhotoUrl() != null) {
            Glide.with(this)
                    .load(this.getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_imv);
        }
    }

    //------
    // UTILS
    //------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private void deleteUserFromFirebase(){

        if (this.getCurrentUser() != null) {
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
        }
    }

    private void updateUsernameInFirebase(){

        this.progressBar.setVisibility(View.VISIBLE);
        String username = this.username_update_edt.getText().toString();

        if (this.getCurrentUser() != null){
            if (!username.isEmpty() &&  !username.equals(getString(R.string.info_no_username_found))){
                UserHelper.updateUsername(username, this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case DELETE_USER_TASK:
                        progressBar.setVisibility(View.GONE);
                        Intent close = new Intent(getApplicationContext(),AuthActivity.class);
                        startActivity(close);
                        break;
                    case UPDATE_USERNAME:
                        progressBar.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        };
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
