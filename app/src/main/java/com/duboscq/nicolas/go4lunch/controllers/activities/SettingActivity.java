package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;

import java.net.SocketTimeoutException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity {

    //FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_setting_language_fr_rbtn) RadioButton french_rbtn;
    @BindView(R.id.activity_setting_language_en_rbtn) RadioButton english_rbtn;
    @BindView(R.id.activity_setting_switch) Switch notification_switch;

    //FOR DATA
    String radio_language;
    String language;
    String TAG_LANGUAGE = "TAG_LANGUAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
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
                    Intent refresh = new Intent(SettingActivity.this, SettingActivity.class);
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
                    Intent refresh = new Intent(SettingActivity.this, SettingActivity.class);
                    startActivity(refresh);
                    finish();
                }
                break;
        }
    }

    // CONFIGURATION

    private void configureToolBar(){
        toolbar.setTitle(R.string.toolbar_title_settings);
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
}
