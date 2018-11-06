package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.RestaurantWorkmatesRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.api.RestaurantHelper;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.models.firebase.Restaurant;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RestaurantActivity extends AppCompatActivity implements RestaurantWorkmatesRecyclerViewAdapter.Listener{

    //FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_restaurant_image_imv) ImageView restaurant_picture_imv;
    @BindView(R.id.activity_restaurant_name_txt) TextView restaurant_name_txt;
    @BindView(R.id.activity_restaurant_adress_txt) TextView restaurant_adress_txt;
    @BindView(R.id.activity_restaurant_call_btn) Button restaurant_call_btn;
    @BindView(R.id.activity_restaurant_like_btn) Button restaurant_like_btn;
    @BindView(R.id.activity_restaurant_website_btn) Button restaurant_website_btn;
    @BindView(R.id.activity_restaurant_selection_floating_btn) FloatingActionButton restaurant_selection;
    @BindView(R.id.activity_restaurant_one_star) ImageView one_star_imv;
    @BindView(R.id.activity_restaurant_two_star) ImageView two_star_imv;
    @BindView(R.id.activity_restaurant_three_star) ImageView three_star_imv;
    @BindView(R.id.activity_restaurant_workmates_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.activity_restaurant_workmates_recycler_view_empty) TextView textViewRecyclerViewEmpty;


    //FOR DATA
    String restaurant_id,restaurant_image_url;
    private Disposable disposable;
    String NETWORK = "NETWORK";
    String restaurant_adress_http, restaurant_name_http, restaurant_phone_http, restaurant_website_http,todayDate,last_restaurant_chosen_id;
    User modelCurrentUser;
    private static final int REQUEST_PHONE_CALL = 1;
    private static final String PERMS = Manifest.permission.CALL_PHONE;
    RestaurantWorkmatesRecyclerViewAdapter workmatesRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        ButterKnife.bind(this);
        configureToolBar();
        disableButtonClick();
        restaurant_id = getIntent().getExtras().getString("restaurant_id",null);
        restaurant_image_url = getIntent().getExtras().getString("restaurant_image_url",null);
        last_restaurant_chosen_id = SharedPreferencesUtility.getString(this,"LAST_RESTAURANT_CHOSEN");
        todayDate = getDateTime();
        checkRestaurantchosen();
        configureAndShowRestaurantList();
        this.getCurrentUserFromFirestore();
        showPictureRestaurant();
        showRestaurantRating();
        configureRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRestaurantRating();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        disposable.isDisposed();
    }

    // TOOLBAR
    private void configureToolBar() {
        toolbar.setTitle(R.string.toolbar_title_restaurant_activity);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    // ACTION
    @OnClick(R.id.activity_restaurant_website_btn)
    public void goWebsite() {
        if (restaurant_website_http != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(restaurant_website_http));
            startActivity(browserIntent);
        } else Toast.makeText(this, getString(R.string.restaurant_no_website), Toast.LENGTH_SHORT).show();
    }

    @AfterPermissionGranted(REQUEST_PHONE_CALL)
    @OnClick(R.id.activity_restaurant_call_btn)
    public void onClickCallRestaurant() {
        this.phoneRestaurant();
    }

    @OnClick(R.id.activity_restaurant_like_btn)
    public void addLikeRestaurant() {
        Toast.makeText(this,getString(R.string.restaurant_like_action),Toast.LENGTH_SHORT).show();
        RestaurantHelper.getRestaurant(restaurant_id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);

                if (restaurant == null){
                    RestaurantHelper.createRestaurant(restaurant_id,1);
                } else {
                    int nb_likes = restaurant.getLikes();
                    RestaurantHelper.updateRestaurantLikes(restaurant_id,nb_likes+1);
                }
            }
        });
    }

    @OnClick(R.id.activity_restaurant_selection_floating_btn)
    public void chooseRestaurant() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String user_uid = user.getUid();
                String restaurant_chosen = user.getLunch();
                String date = user.getLunchDate();
                if (!date.equals(todayDate)){
                    userChooseRestaurant(user_uid,restaurant_chosen,user);
                } else if (date.equals(todayDate) && !restaurant_id.equals(restaurant_chosen)){
                    userChooseRestaurant(user_uid,restaurant_chosen,user);
                } else if (date.equals(todayDate) && restaurant_id.equals(restaurant_chosen)){
                    Toast.makeText(RestaurantActivity.this,getString(R.string.restaurant_already_chosen),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void configureAndShowRestaurantList() {
        disposable = APIStreams.getRestaurantDetail(restaurant_id,getString(R.string.api_google_place_key)).subscribeWith(new DisposableObserver<RestaurantDetail>() {
            @Override
            public void onNext(RestaurantDetail restaurantDetail) {
                Log.i(NETWORK, "RestauActivity : On Next");
                restaurant_adress_http=restaurantDetail.getResult().getFormattedAddress();
                restaurant_name_http=restaurantDetail.getResult().getName();
                if (restaurantDetail.getResult().getWebsite() != null){
                    restaurant_website_http=restaurantDetail.getResult().getWebsite();
                } else restaurant_website_http = null;
                if (restaurantDetail.getResult().getFormattedPhoneNumber() != null){
                    restaurant_phone_http=restaurantDetail.getResult().getFormattedPhoneNumber();
                } else restaurant_phone_http = null;
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "RestauActivity : On Error " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "RestauActivity : On Complete !!");
                restaurant_adress_txt.setText(restaurant_adress_http);
                restaurant_name_txt.setText(restaurant_name_http);
                restaurant_call_btn.setClickable(true);
                restaurant_website_btn.setClickable(true);
            }
        });
    }

    public void showPictureRestaurant(){
        if (restaurant_image_url != null){
            Glide.with(this).load(restaurant_image_url).into(restaurant_picture_imv);
        }
    }

    public void disableButtonClick(){
        restaurant_call_btn.setClickable(false);
        restaurant_website_btn.setClickable(false);
    }

    public void showRestaurantRating(){
        RestaurantHelper.getRestaurant(restaurant_id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                if (restaurant != null){
                    int nb_likes = restaurant.getLikes();
                    if (1 < nb_likes && nb_likes<6){
                        one_star_imv.setVisibility(View.VISIBLE);
                        two_star_imv.setVisibility(View.INVISIBLE);
                        three_star_imv.setVisibility(View.INVISIBLE);
                    } else if (5 < nb_likes && nb_likes<10){
                        one_star_imv.setVisibility(View.VISIBLE);
                        two_star_imv.setVisibility(View.VISIBLE);
                        three_star_imv.setVisibility(View.INVISIBLE);
                    } else if (10 < nb_likes) {
                        one_star_imv.setVisibility(View.VISIBLE);
                        two_star_imv.setVisibility(View.VISIBLE);
                        three_star_imv.setVisibility(View.VISIBLE);
                    }
                } else {
                    one_star_imv.setVisibility(View.INVISIBLE);
                    two_star_imv.setVisibility(View.INVISIBLE);
                    three_star_imv.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void userChooseRestaurant(String user_uid, String restaurant_chosen, User user){
        UserHelper.updateUserLunch(user_uid,restaurant_id);
        UserHelper.updateUserLunchUrl(user_uid,restaurant_image_url);
        UserHelper.updateUserLunchDate(user_uid,todayDate);
        restaurant_selection.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        if (!restaurant_chosen.equals(null)){
            UserHelper.deleteUserInRestaurantList(restaurant_chosen,"users"+todayDate,getCurrentUser().getUid());
        }
        RestaurantHelper.createUserforRestaurant(restaurant_id,user_uid,user.getUsername(),todayDate,user.getUrlPicture(),restaurant_id,todayDate,restaurant_image_url);
        SharedPreferencesUtility.putString(RestaurantActivity.this,"LAST_RESTAURANT_CHOSEN",restaurant_id);
        Toast.makeText(RestaurantActivity.this,getString(R.string.restaurant_chosen),Toast.LENGTH_SHORT).show();
    }

    // --------------------
    // UI
    // --------------------

    //RECYCLERVIEW CONFIGURATION
    private void configureRecyclerView(){

        this.workmatesRecyclerViewAdapter = new RestaurantWorkmatesRecyclerViewAdapter(generateOptionsForAdapter(UserHelper.getAllRestaurantWorkmates(restaurant_id,"users"+todayDate)), Glide.with(this), this, this.getCurrentUser().getUid(), getString(R.string.workmate_joining));
        workmatesRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(workmatesRecyclerViewAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(RestaurantActivity.this));
        recyclerView.setAdapter(this.workmatesRecyclerViewAdapter);
    }

    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }

    private void checkRestaurantchosen(){
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String restaurant_chosen = user.getLunch();
                String date_lunch = user.getLunchDate();
                if (date_lunch.equals(todayDate) && restaurant_chosen.equals(restaurant_id)){
                    restaurant_selection.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }
            }
        });
    }


    // --------------------
    // CALLBACK
    // --------------------

    @Override
    public void onDataChanged() {
        textViewRecyclerViewEmpty.setVisibility(this.workmatesRecyclerViewAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 2 - Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    // --------------------
    // REST REQUESTS
    // --------------------
    private void getCurrentUserFromFirestore(){
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                modelCurrentUser = documentSnapshot.toObject(User.class);
            }
        });
    }

    // --------------------
    // PHONECALL MANAGEMENT
    // --------------------
    private void phoneRestaurant(){
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_phone_access), REQUEST_PHONE_CALL, PERMS);
            return;
        }
        if (restaurant_phone_http != null) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + restaurant_phone_http));
            startActivity(callIntent);
        } else Toast.makeText(this,getString(R.string.restaurant_no_phone),Toast.LENGTH_SHORT).show();
    }
}
