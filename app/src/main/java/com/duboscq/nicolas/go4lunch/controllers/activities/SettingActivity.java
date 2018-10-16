package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.duboscq.nicolas.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    //FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        configureToolBar();
    }

    private void configureToolBar(){
        toolbar.setTitle(R.string.toolbar_title_settings);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
