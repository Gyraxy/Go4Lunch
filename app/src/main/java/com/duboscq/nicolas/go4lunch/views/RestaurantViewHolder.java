package com.duboscq.nicolas.go4lunch.views;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;

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

    //FOR DATA
    private Location restaurant_location,my_location;
    private String distance,restaurant_distance_stg;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantInfo(Result restaurantPlaceResult,double latB,double lngB,RequestManager glide) {
        restaurant_name_txt.setText(restaurantPlaceResult.getName());
        restaurant_address_txt.setText(restaurantPlaceResult.getVicinity());
        restaurant_people_chosen_txt.setText("(0)");

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


        restaurant_location = new Location("point A");
        restaurant_location.setLatitude(restaurantPlaceResult.getGeometry().getLocation().getLat());
        restaurant_location.setLongitude(restaurantPlaceResult.getGeometry().getLocation().getLng());

        my_location = new Location ("point B");
        my_location.setLatitude(latB);
        my_location.setLongitude(lngB);

        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        distance = formatDistance(restaurant_location.distanceTo(my_location));
        restaurant_distance_stg= distance + " m";
        restaurant_distance_txt.setText(restaurant_distance_stg);
    }

    private String formatDistance(double distance){
        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(distance);
    }
}
