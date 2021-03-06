package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.utils.DateUtility;
import com.duboscq.nicolas.go4lunch.utils.FirebaseUtils;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
    @BindView(R.id.activity_restaurant_opening_hours_txt) TextView activity_restaurant_opening_hours_txt;
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
    private Disposable disposable;
    String NETWORK = "NETWORK";
    String restaurant_id, restaurant_adress_http, restaurant_name_http, restaurant_phone_http, restaurant_website_http,todayDate,last_restaurant_chosen_id, restaurantPictureUrl,restaurant_hours;
    String restaurant_open;
    User modelCurrentUser;
    int nb_like;
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
        last_restaurant_chosen_id = SharedPreferencesUtility.getString(this,"LAST_RESTAURANT_CHOSEN");
        todayDate = DateUtility.getDateTime();
        checkRestaurantchosen();
        httpRequestRestaurantDetail();
        this.getCurrentUserFromFirestore();
        showRestaurantRating(restaurant_id);
        configureRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("APP", "RestauActivity : Resume");
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
    public void onClickLikeRestaurant() {
        RestaurantHelper.getRestaurantUserLikeList(restaurant_id,FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final User user_like = documentSnapshot.toObject(User.class);
                if (user_like == null){
                    RestaurantHelper.addUserLiketoRestaurant(restaurant_id,FirebaseUtils.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                showRestaurantRating(restaurant_id);
                                Toast.makeText(RestaurantActivity.this,getString(R.string.restaurant_like_action),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    AlertDialog.Builder like_diag = new AlertDialog.Builder(RestaurantActivity.this);
                    like_diag.setMessage(getString(R.string.diag_like))
                            .setPositiveButton(getString(R.string.dialog_btn_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    RestaurantHelper.deleteUserLikeInRestaurantList(restaurant_id,FirebaseUtils.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                showRestaurantRating(restaurant_id);
                                            }
                                        }
                                    });

                                }
                            })
                            .setNegativeButton(getString(R.string.dialog_btn_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    like_diag.show();
                }
            }
        });
    }

    @OnClick(R.id.activity_restaurant_selection_floating_btn)
    public void chooseRestaurant() {
        UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String user_uid = user.getUid();
                String restaurant_chosen = user.getLunchId();
                String date = user.getLunchDate();
                if (!date.equals(todayDate)){
                    userChooseRestaurant(user_uid,restaurant_chosen,user);
                } else if (date.equals(todayDate) && !restaurant_id.equals(restaurant_chosen)){
                    userChooseRestaurant(user_uid,restaurant_chosen,user);
                } else if (date.equals(todayDate) && restaurant_id.equals(restaurant_chosen)){
                    RestaurantHelper.deleteUserInRestaurantList(restaurant_chosen,"users"+todayDate,FirebaseUtils.getCurrentUser().getUid());
                    UserHelper.updateUserLunchDate(FirebaseUtils.getCurrentUser().getUid(),"XX-XX-XXXX");
                    UserHelper.updateUserLunchId(FirebaseUtils.getCurrentUser().getUid(),"XXX");
                    UserHelper.updateUserLunchName(FirebaseUtils.getCurrentUser().getUid(),"XXX");
                    restaurant_selection.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.WhiteColor)));
                }
            }
        });
    }

    public void disableButtonClick(){
        restaurant_call_btn.setClickable(false);
        restaurant_website_btn.setClickable(false);
    }

    // --------------------
    // RESTAURANT UI
    // --------------------

    public void updateRestaurantInfo(){
        restaurant_adress_txt.setText(restaurant_adress_http);
        restaurant_name_txt.setText(restaurant_name_http);
        if (restaurantPictureUrl != null){
            Glide.with(this).load(restaurantPictureUrl).into(restaurant_picture_imv);
        } else Glide.with(this).load(R.drawable.no_camera).into(restaurant_picture_imv);

        switch (restaurant_open){
            case "OPEN":
                String open_hours = getString(R.string.restaurant_open_hours)+restaurant_hours;
                activity_restaurant_opening_hours_txt.setText(open_hours);
                break;
            case "CLOSED":
                activity_restaurant_opening_hours_txt.setText(getString(R.string.restaurant_close));
                break;
            case "NO_INFO":
                activity_restaurant_opening_hours_txt.setText(R.string.restaurant_no_opening_info);
                break;
                default:
                    activity_restaurant_opening_hours_txt.setText(R.string.restaurant_no_opening_info);
                    break;
        }
    }

    private void showRestaurantRating(String restaurant_id) {
        Log.i("APP","Show Rating Restaurant");
        RestaurantHelper.getRestaurantLike(restaurant_id).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                nb_like = task.getResult().size();
                if (nb_like == 0) {
                    one_star_imv.setVisibility(View.INVISIBLE);
                    two_star_imv.setVisibility(View.INVISIBLE);
                    three_star_imv.setVisibility(View.INVISIBLE);
                }
                if (nb_like >= 1 && nb_like <= 5) {
                    one_star_imv.setVisibility(View.VISIBLE);
                    two_star_imv.setVisibility(View.INVISIBLE);
                    three_star_imv.setVisibility(View.INVISIBLE);
                }
                if (nb_like > 5 && nb_like <= 10) {
                    one_star_imv.setVisibility(View.VISIBLE);
                    two_star_imv.setVisibility(View.VISIBLE);
                    three_star_imv.setVisibility(View.INVISIBLE);
                }
                if (nb_like > 10) {
                    one_star_imv.setVisibility(View.VISIBLE);
                    two_star_imv.setVisibility(View.VISIBLE);
                    three_star_imv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void checkRestaurantchosen(){
        UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String restaurant_chosen = user.getLunchId();
                String date_lunch = user.getLunchDate();
                if (date_lunch.equals(todayDate) && restaurant_chosen.equals(restaurant_id)){
                    restaurant_selection.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }
            }
        });
    }

    // --------------------
    // UI
    // --------------------

    //RECYCLERVIEW CONFIGURATION
    private void configureRecyclerView(){

        this.workmatesRecyclerViewAdapter = new RestaurantWorkmatesRecyclerViewAdapter(generateOptionsForAdapter(UserHelper.getAllRestaurantWorkmates(restaurant_id,"users"+todayDate)), Glide.with(this), this, FirebaseUtils.getCurrentUser().getUid(), getString(R.string.workmate_joining));
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

    // --------------------------
    // RESTAURANT DETAIL HTTP GET
    // --------------------------

    public void httpRequestRestaurantDetail() {
        disposable = APIStreams.getRestaurantDetail(restaurant_id,getString(R.string.api_google_place_key)).subscribeWith(new DisposableObserver<RestaurantDetail>() {
            @Override
            public void onNext(RestaurantDetail restaurantDetail) {
                Log.i(NETWORK, "RestauActivity : On Next");
                Log.i(NETWORK,restaurantDetail.getResult().getPlaceId());
                restaurant_adress_http = restaurantDetail.getResult().getFormattedAddress();
                restaurant_name_http = restaurantDetail.getResult().getName();
                if (restaurantDetail.getResult().getPhotos() != null) {
                    if (restaurantDetail.getResult().getPhotos().get(0).getPhotoReference() != null) {
                        restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" +
                                restaurantDetail.getResult().getPhotos().get(0).getPhotoReference() +
                                "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                    }
                }
                if (restaurantDetail.getResult().getWebsite() != null){
                    restaurant_website_http=restaurantDetail.getResult().getWebsite();
                } else restaurant_website_http = null;
                if (restaurantDetail.getResult().getFormattedPhoneNumber() != null){
                    restaurant_phone_http=restaurantDetail.getResult().getFormattedPhoneNumber();
                } else restaurant_phone_http = null;
                try {
                    boolean isOpenNow = restaurantDetail.getResult().getOpeningHours().getOpenNow();
                    if (isOpenNow) {
                        restaurant_open = "OPEN";
                        restaurant_hours = DateUtility.formatWeekDayText(restaurantDetail.getResult().getOpeningHours().getWeekdayText());
                    } else {
                        restaurant_open = "CLOSED";
                    }
                } catch (NullPointerException e) {
                    Log.i(NETWORK,"No Opening Hours");
                    restaurant_open = "NO_INFO";
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "RestauActivity : On Error " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "RestauActivity : On Complete !!");
                updateRestaurantInfo();
                restaurant_call_btn.setClickable(true);
                restaurant_website_btn.setClickable(true);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    private void getCurrentUserFromFirestore(){
        UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                modelCurrentUser = documentSnapshot.toObject(User.class);
            }
        });
    }

    private void userChooseRestaurant(String user_uid, String restaurant_chosen, User user){
        UserHelper.updateUserLunchId(user_uid,restaurant_id);
        UserHelper.updateUserLunchName(user_uid,restaurant_name_http);
        UserHelper.updateUserLunchDate(user_uid,todayDate);
        restaurant_selection.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        if (!restaurant_chosen.equals(null)){
            RestaurantHelper.deleteUserInRestaurantList(restaurant_chosen,"users"+todayDate,FirebaseUtils.getCurrentUser().getUid());
        }
        RestaurantHelper.createUserforRestaurant(restaurant_id,user_uid,user.getUsername(),todayDate,user.getUrlPicture(),restaurant_id,restaurant_name_http,todayDate);
        SharedPreferencesUtility.putString(RestaurantActivity.this,"LAST_RESTAURANT_CHOSEN",restaurant_id);
        Toast.makeText(RestaurantActivity.this,getString(R.string.restaurant_chosen),Toast.LENGTH_SHORT).show();
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
