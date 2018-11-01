package com.duboscq.nicolas.go4lunch.views;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.RestaurantHelper;
import com.duboscq.nicolas.go4lunch.models.firebase.Restaurant;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    //FOR DESIGN
    @BindView(R.id.restaurant_list_name_txt) TextView restaurant_name_txt;
    @BindView(R.id.restaurant_list_adress_txt) TextView restaurant_address_txt;
    @BindView(R.id.restaurant_list_people_txt) TextView restaurant_people_chosen_txt;
    @BindView(R.id.restaurant_list_distance_txt) TextView restaurant_distance_txt;
    @BindView(R.id.restaurant_list_opening_txt) TextView restaurant_openingTimes_txt;
    @BindView(R.id.restaurant_list_photo_imv) ImageView restaurant_picture_imv;
    @BindView(R.id.restaurant_list_one_star_imv) ImageView restaurant_one_star_imv;
    @BindView(R.id.restaurant_list_two_star_imv) ImageView restaurant_two_star_imv;
    @BindView(R.id.restaurant_list_three_star_imv) ImageView restaurant_three_star_imv;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantInfo(Result restaurantPlaceResult,double latB,double lngB,RequestManager glide,int nb_workmates_joining) {
        restaurant_name_txt.setText(restaurantPlaceResult.getName());
        restaurant_address_txt.setText(restaurantPlaceResult.getVicinity());
        restaurant_people_chosen_txt.setText("("+nb_workmates_joining+")");

        try {
            boolean isOpenNow = restaurantPlaceResult.getOpeningHours().getOpenNow();
            if (isOpenNow) {
                restaurant_openingTimes_txt.setText(R.string.restaurant_open);
            } else {
                restaurant_openingTimes_txt.setText(R.string.restaurant_close);
            }
        } catch (NullPointerException e) {
            restaurant_openingTimes_txt.setText(R.string.restaurant_no_opening_info);
        }
        if (restaurantPlaceResult.getPhotos() != null) {
            if (restaurantPlaceResult.getPhotos().get(0).getPhotoReference() != null){
                String restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+
                        restaurantPlaceResult.getPhotos().get(0).getPhotoReference()+
                        "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                glide.load(restaurantPictureUrl).into(restaurant_picture_imv);
            }
        } else glide.load(R.drawable.no_camera).into(restaurant_picture_imv);


        //FOR DATA
        Location restaurant_location = new Location("point A");
        restaurant_location.setLatitude(restaurantPlaceResult.getGeometry().getLocation().getLat());
        restaurant_location.setLongitude(restaurantPlaceResult.getGeometry().getLocation().getLng());

        Location my_location = new Location("point B");
        my_location.setLatitude(latB);
        my_location.setLongitude(lngB);

        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        String distance = formatDistance(restaurant_location.distanceTo(my_location));
        String restaurant_distance_stg = distance + " m";
        restaurant_distance_txt.setText(restaurant_distance_stg);

        showRestaurantRating(restaurantPlaceResult.getPlaceId());
    }

    private String formatDistance(double distance){
        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(distance);
    }

    private void showRestaurantRating(String restaurant_id){
        RestaurantHelper.getRestaurant(restaurant_id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                if (restaurant != null){
                    int nb_likes = restaurant.getLikes();
                    if (1 < nb_likes && nb_likes<6){
                        restaurant_one_star_imv.setVisibility(View.VISIBLE);
                        restaurant_two_star_imv.setVisibility(View.INVISIBLE);
                        restaurant_three_star_imv.setVisibility(View.INVISIBLE);
                    } else if (5 < nb_likes && nb_likes<10){
                        restaurant_one_star_imv.setVisibility(View.VISIBLE);
                        restaurant_two_star_imv.setVisibility(View.VISIBLE);
                        restaurant_three_star_imv.setVisibility(View.INVISIBLE);
                    } else if (10 < nb_likes) {
                        restaurant_one_star_imv.setVisibility(View.VISIBLE);
                        restaurant_two_star_imv.setVisibility(View.VISIBLE);
                        restaurant_three_star_imv.setVisibility(View.VISIBLE);
                    }
                } else {
                    restaurant_one_star_imv.setVisibility(View.INVISIBLE);
                    restaurant_two_star_imv.setVisibility(View.INVISIBLE);
                    restaurant_three_star_imv.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
