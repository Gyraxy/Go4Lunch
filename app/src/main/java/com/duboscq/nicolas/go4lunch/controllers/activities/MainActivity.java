package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.controllers.fragments.MapViewFragment;
import com.duboscq.nicolas.go4lunch.controllers.fragments.RestaurantFragment;
import com.duboscq.nicolas.go4lunch.controllers.fragments.WorkmatesFragment;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModel;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModelFactory;
import com.duboscq.nicolas.go4lunch.utils.FirebaseUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.SphericalUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Nicolas DUBOSCQ on 27/09/2018
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_nav_bottom) BottomNavigationView bottomNavigationView;
    @BindView(R.id.activity_main_drawer) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_autocomplete_result_btn) Button autocomplete_result_btn;

    //FOR DATA
    private static final int SIGN_OUT_TASK = 10;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String NETWORK = "NETWORK";
    TextView profile_name_txt,profile_email_txt;
    MapViewFragment mapViewFragment;
    RestaurantFragment restaurantFragment;
    WorkmatesFragment workmatesFragment;
    FragmentManager fragmentManager = getSupportFragmentManager();
    RestaurantViewModel mModel;
    String key,my_location,todayDate;
    LatLng my_location_latlng;
    FusedLocationProviderClient mFusedLocationClient;

    //AUTOCOMPLETE
    Disposable disposable;
    Place autocomplete_place;
    List<Result> autocomplete_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("APP","Main Activity On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Notification subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Notification subscribed";
                        }
                        Log.i(NETWORK,msg);
                    }
                });
        key = getString(R.string.api_google_place_key);
        todayDate = getDateTime();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        configureToolBar();
        configureDrawerLayout();
        configureNavigationView();
        configureBottomOnClick();
        autocomplete_result_btn.setVisibility(View.GONE);
        updateProfileData();
        createModelViewAndInitFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        //SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //searchView.setQueryHint(getString(R.string.toolbar_search_query_hint));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.activity_main_search:
                showAutoCompleteFragment(my_location_latlng);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
                UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User current_user = documentSnapshot.toObject(User.class);
                        String your_lunch = current_user.getLunchId();
                        String your_lunch_date = current_user.getLunchDate();
                        if (your_lunch.equals("XXX") || !todayDate.equals(your_lunch_date)){
                            Toast.makeText(MainActivity.this,getString(R.string.no_lunch_chosen),Toast.LENGTH_SHORT).show();
                        } else if (todayDate.equals(your_lunch_date) && !your_lunch.equals("XXX")){
                            Intent i_lunch = new Intent(MainActivity.this,RestaurantActivity.class);
                            i_lunch.putExtra("restaurant_id",your_lunch);
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

    private void initFragment(double user_lat, double user_lng ) {
        mapViewFragment = new MapViewFragment();
        restaurantFragment = RestaurantFragment.newInstance(user_lat, user_lng);
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

        if (FirebaseUtils.getCurrentUser().getPhotoUrl() != null) {
            Glide.with(this)
                    .load(FirebaseUtils.getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_imv);
        }

        if (FirebaseUtils.getCurrentUser() != null) {
            UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    String username = TextUtils.isEmpty(currentUser.getUsername()) ? getString(R.string.info_no_username_found) : currentUser.getUsername();
                    profile_name_txt.setText(username);
                }
            });
            String usermail = FirebaseUtils.getCurrentUser().getEmail();
            profile_email_txt.setText(usermail);
        }
    }

    // ------------------
    // AUTOCOMPLETE UTILS
    // ------------------

    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    private void  showAutoCompleteFragment (LatLng my_position) {
        Intent intent = null;

        LatLngBounds latLngBounds = toBounds(my_position,20);

        AutocompleteFilter estabFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        AutocompleteFilter countryFilter = new AutocompleteFilter.Builder()
                .setCountry("FR")
                .build();
        try {
            intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(estabFilter)
                    .setFilter(countryFilter)
                    .setBoundsBias(latLngBounds)
                    .build(this);

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                autocomplete_place = PlaceAutocomplete.getPlace(this, data);
                httpRequestRestaurantAutoComplete();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("APP", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @OnClick (R.id.activity_main_autocomplete_result_btn)
    public void cancelAutocomplete(){
        autocomplete_result_btn.setText("");
        autocomplete_result_btn.setVisibility(View.GONE);
        mapViewFragment.getListWorkmatesJoining(mModel.getRestaurantResult().getValue());
        restaurantFragment.configureRecyclerView(mModel.getRestaurantResult().getValue());
    }

    // -----
    // UTILS
    // -----

    private void createModelViewAndInitFragment(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        my_location = location.getLatitude() + "," + location.getLongitude();
                        my_location_latlng = new LatLng(location.getLatitude(),location.getLongitude());
                        mModel = ViewModelProviders.of(MainActivity.this,new RestaurantViewModelFactory(key,my_location)).get(RestaurantViewModel.class);
                        initFragment(location.getLatitude(),location.getLongitude());

                    }
                }
            });
        } else {
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

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
        Date date = new Date();
        return dateFormat.format(date);
    }

    //HTTP GET AUTOCOMPLETE

    public void httpRequestRestaurantAutoComplete() {
        String autocomplete_place_txt = autocomplete_place.getLatLng().latitude + "," + autocomplete_place.getLatLng().longitude;
        disposable = APIStreams.getRestaurantList(0.01,key,autocomplete_place_txt).subscribeWith(new DisposableObserver<RestaurantPlace>() {
            @Override
            public void onNext(RestaurantPlace restaurantPlace) {
                Log.i(NETWORK, "ViewModel: On Next");
                autocomplete_result = new ArrayList<>();
                autocomplete_result = restaurantPlace.getResults();
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "ViewModel : On Error " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "ViewModel : On Complete !!");
                Log.i("AUTOCOMPLETE",autocomplete_result.get(0).getName());
                autocomplete_result_btn.setVisibility(View.VISIBLE);
                autocomplete_result_btn.setText(autocomplete_place.getName());
                mapViewFragment.getListWorkmatesJoining(autocomplete_result);
                restaurantFragment.configureRecyclerView(autocomplete_result);
            }
        });
    }
}
