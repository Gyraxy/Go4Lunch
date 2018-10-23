package com.duboscq.nicolas.go4lunch.views;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.restaurant_list_name_txt) TextView restaurant_name;
    @BindView(R.id.restaurant_list_adress_txt) TextView restaurant_address;
    @BindView(R.id.restaurant_list_people_txt) TextView restaurant_people_chosen;
    @BindView(R.id.restaurant_list_distance_txt) TextView restaurant_distance;
    @BindView(R.id.restaurant_list_opening_txt) TextView restaurant_openingTimes;
    @BindView(R.id.restaurant_list_photo_imv) ImageView restaurant_picture;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantInfo(Result restaurantPlaceResult,float distance,RequestManager glide) {
        restaurant_name.setText(restaurantPlaceResult.getName());
        restaurant_address.setText(restaurantPlaceResult.getVicinity());
        restaurant_people_chosen.setText("(0)");
        restaurant_distance.setText(distance + " m");

        try {
            boolean isOpenNow = restaurantPlaceResult.getOpeningHours().getOpenNow();
            if (isOpenNow) {
                restaurant_openingTimes.setText(R.string.restaurant_open);
            } else {
                restaurant_openingTimes.setText(R.string.restaurant_close);
            }
        } catch (NullPointerException e) {
            restaurant_openingTimes.setText(R.string.restaurant_no_opening_info);
        }
        if (restaurantPlaceResult.getPhotos() != null) {
            if (restaurantPlaceResult.getPhotos().get(0).getPhotoReference() != null){
                String restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+
                        restaurantPlaceResult.getPhotos().get(0).getPhotoReference()+
                        "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                Log.i("PHOTO", restaurantPictureUrl);
                glide.load(restaurantPictureUrl).into(restaurant_picture);
            }
        } else glide.load(R.drawable.no_camera).into(restaurant_picture);
    }
}
