package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.RestaurantHelper;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.controllers.fragments.MapViewFragment;
import com.duboscq.nicolas.go4lunch.controllers.fragments.RestaurantFragment;
import com.duboscq.nicolas.go4lunch.controllers.fragments.WorkmatesFragment;
import com.duboscq.nicolas.go4lunch.models.firebase.Restaurant;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModel;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModelFactory;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.utils.PermissionUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nicolas DUBOSCQ on 27/09/2018
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_nav_bottom) BottomNavigationView bottomNavigationView;
    @BindView(R.id.activity_main_drawer) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;

    //FOR DATA
    private static final int SIGN_OUT_TASK = 10;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    TextView profile_name_txt,profile_email_txt;
    MapViewFragment mapViewFragment;
    RestaurantFragment restaurantFragment;
    WorkmatesFragment workmatesFragment;
    FragmentManager fragmentManager = getSupportFragmentManager();
    RestaurantViewModel mModel;
    String key,my_location,todayDate;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        key = getString(R.string.api_google_place_key);
        todayDate = getDateTime();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createModelViewandInitFragment();
        configureToolBar();
        configureDrawerLayout();
        configureNavigationView();
        configureBottomOnClick();
        updateProfileData();
    }

    // -------------
    // CONFIGURATION
    // -------------

    // TOOLBAR

    private void configureToolBar(){
        toolbar.setTitle(R.string.toolbar_title_hungry);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.toolbar_search_query_hint));
        return true;
    }

    // NAVIGATION DRAWER

    private void configureNavigationView(){
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.activity_main_your_profile:
                Intent i_profile = new Intent(this, SettingProfileActivity.class);
                i_profile.putExtra("Activity","Profile");
                startActivity(i_profile);
                break;
            case R.id.activity_main_your_lunch:
                UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User current_user = documentSnapshot.toObject(User.class);
                        String your_lunch = current_user.getLunch();
                        String your_lunch_url = current_user.getLunchUrl();
                        String your_lunch_date = current_user.getLunchDate();
                        if (your_lunch == null || !todayDate.equals(your_lunch_date)){
                            Toast.makeText(MainActivity.this,"You did not choose your lunch yet.",Toast.LENGTH_SHORT).show();
                        } else if (todayDate.equals(your_lunch_date) && your_lunch != null){
                            Intent i_lunch = new Intent(MainActivity.this,RestaurantActivity.class);
                            i_lunch.putExtra("restaurant_id",your_lunch);
                            i_lunch.putExtra("restaurant_image_url",your_lunch_url);
                            startActivity(i_lunch);
                        }
                    }
                });
                break;
            case R.id.activity_main_settings:
                Intent i_setting = new Intent(this, SettingProfileActivity.class);
                i_setting.putExtra("Activity","Settings");
                startActivity(i_setting);
                break;
            case R.id.activity_main_logout:
                signOutUserFromFirebase();
                Toast.makeText(getApplicationContext(),getString(R.string.auth_loggout),Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // BOTTOM MENU

    private void configureBottomOnClick() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.bottom_nav_map:
                        toolbar.setTitle(R.string.toolbar_title_hungry);
                        fragmentManager.beginTransaction().show(mapViewFragment).hide(restaurantFragment).hide(workmatesFragment).commit();
                        mapViewFragment.centerMyLocation();
                        return true;
                    case R.id.bottom_nav_list_view:
                        toolbar.setTitle(R.string.toolbar_title_hungry);
                        fragmentManager.beginTransaction().show(restaurantFragment).hide(mapViewFragment).hide(workmatesFragment).commit();
                        return true;
                    case R.id.bottom_nav_workmates:
                        toolbar.setTitle(R.string.toolbar_title_workmates);
                        fragmentManager.beginTransaction().show(workmatesFragment).hide(restaurantFragment).hide(mapViewFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

    //--------
    //FRAGMENT
    //--------

    private void initFragment() {
        mapViewFragment = new MapViewFragment();
        restaurantFragment = new RestaurantFragment();
        workmatesFragment = new WorkmatesFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_activity_frame_layout,workmatesFragment).hide(workmatesFragment);
        fragmentTransaction.add(R.id.main_activity_frame_layout,restaurantFragment).hide(restaurantFragment);
        fragmentTransaction.add(R.id.main_activity_frame_layout, mapViewFragment);
        fragmentTransaction.commit();
    }

    // ----
    //  UI
    // ----

    private void updateProfileData() {

        View nav_header = navigationView.getHeaderView(0);
        profile_name_txt = nav_header.findViewById(R.id.nav_header_profile_name_txt);
        profile_email_txt = nav_header.findViewById(R.id.nav_header_profile_email_txt);
        ImageView profile_imv = nav_header.findViewById(R.id.nav_header_profile_imv);

        if (this.getCurrentUser().getPhotoUrl() != null) {
            Glide.with(this)
                    .load(this.getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_imv);
        }

        if (this.getCurrentUser() != null) {
            UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    String username = TextUtils.isEmpty(currentUser.getUsername()) ? getString(R.string.info_no_username_found) : currentUser.getUsername();
                    profile_name_txt.setText(username);
                }
            });
            String usermail = getCurrentUser().getEmail();
            profile_email_txt.setText(usermail);
        }
    }

    // -----
    // UTILS
    // -----

    private void createModelViewandInitFragment(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        my_location = location.getLatitude() + "," + location.getLongitude();
                        mModel = ViewModelProviders.of(MainActivity.this,new RestaurantViewModelFactory(key,my_location)).get(RestaurantViewModel.class);
                        initFragment();
                    }
                }
            });
        } else {
            PermissionUtils.requestPermission((AppCompatActivity) this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
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
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
        Date date = new Date();
        return dateFormat.format(date);
    }
}
