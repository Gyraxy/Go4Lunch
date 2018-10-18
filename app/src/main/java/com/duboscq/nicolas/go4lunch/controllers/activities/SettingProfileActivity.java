package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;
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


    @BindView(R.id.activity_profile_image_layout) RelativeLayout profile_layout;
    @BindView(R.id.activity_setting_switch_layout) LinearLayout setting_switch_layout;
    @BindView(R.id.activity_setting_language_layout) LinearLayout setting_language_layout;
    @BindView(R.id.activity_setting_view) View setting_view;

    //FOR DATA
    String radio_language;
    String language;
    String TAG_LANGUAGE = "TAG_LANGUAGE";
    String activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        ButterKnife.bind(this);
        activity = getIntent().getExtras().getString("Activity");
        if (activity.equals("Settings")){
            setSettingLayout();
        } else if (activity.equals("Profile")){
            setProfile_layout();
        }
        configureToolBar();
        getLanguage();
    }

    // ACTIONS

    @OnClick({R.id.activity_setting_language_fr_rbtn, R.id.activity_setting_language_en_rbtn})
    public void onRadioButtonClicked(RadioButton radioButton) {
        boolean checked = radioButton.isChecked();
        switch (radioButton.getId()) {
            case R.id.activity_setting_language_fr_rbtn:
                if (checked) {
                    Locale locale = new Locale("fr");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
                    SharedPreferencesUtility.putString(this,TAG_LANGUAGE,"Français");
                    Intent refresh = new Intent(SettingProfileActivity.this, SettingProfileActivity.class);
                    startActivity(refresh);
                    finish();
                }
                break;
            case R.id.activity_setting_language_en_rbtn:
                if (checked) {
                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
                    SharedPreferencesUtility.putString(this,TAG_LANGUAGE,"English");
                    Intent refresh = new Intent(SettingProfileActivity.this, SettingProfileActivity.class);
                    startActivity(refresh);
                    finish();
                }
                break;
        }
    }

    // CONFIGURATION

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

    private void getLanguage(){
        language = SharedPreferencesUtility.getString(this,TAG_LANGUAGE);
        if (language == null){
            radio_language = Locale.getDefault().getDisplayLanguage();
        } else radio_language = language;
        Log.e("TAG",radio_language);

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

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }
}
