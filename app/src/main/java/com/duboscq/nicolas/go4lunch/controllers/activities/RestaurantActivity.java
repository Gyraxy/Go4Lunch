package com.duboscq.nicolas.go4lunch.controllers.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.api.RestaurantHelper;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantActivity extends AppCompatActivity {

    //FOR DESIGN
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_restaurant_image_imv)
    ImageView restaurant_picture_imv;
    @BindView(R.id.activity_restaurant_name_txt)
    TextView restaurant_name_txt;
    @BindView(R.id.activity_restaurant_adress_txt)
    TextView restaurant_adress_txt;
    @BindView(R.id.activity_restaurant_call_btn)
    Button restaurant_call_btn;
    @BindView(R.id.activity_restaurant_like_btn)
    Button restaurant_like_btn;
    @BindView(R.id.activity_restaurant_website_btn)
    Button restaurant_website_btn;
    @BindView(R.id.activity_restaurant_selection_floating_btn)
    FloatingActionButton restaurant_selection;


    //FOR DATA
    String restaurant_id,restaurant_image_url;
    private Disposable disposable;
    String NETWORK = "NETWORK";
    String restaurant_adress_http, restaurant_name_http, restaurant_phone_http, restaurant_website_http;
    User modelCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        ButterKnife.bind(this);
        configureToolBar();
        disableButtonClick();
        restaurant_id = getIntent().getExtras().getString("restaurant_id",null);
        restaurant_image_url = getIntent().getExtras().getString("restaurant_image_url",null);
        configureAndShowRestaurantList();
        this.getCurrentUserFromFirestore();
        showPictureRestaurant();
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
        } else Toast.makeText(this, "No Website", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.activity_restaurant_call_btn)
    public void callRestaurant() {
        if (restaurant_phone_http != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + restaurant_phone_http));
                startActivity(callIntent);
            } else Toast.makeText(this,"Phone Permission not accepted",Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this,"No PhoneNumber",Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.activity_restaurant_like_btn)
    public void addLikeRestaurant() {
        Toast.makeText(this,getString(R.string.restaurant_like_action),Toast.LENGTH_SHORT).show();
        RestaurantHelper.createRestaurant(restaurant_id,+1);
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

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

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
}
