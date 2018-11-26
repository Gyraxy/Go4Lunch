package com.duboscq.nicolas.go4lunch.views;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.RestaurantHelper;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.utils.DateUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    //FOR DESIGN
    @BindView(R.id.restaurant_list_name_txt)
    TextView restaurant_name_txt;
    @BindView(R.id.restaurant_list_adress_txt)
    TextView restaurant_address_txt;
    @BindView(R.id.restaurant_list_people_txt)
    TextView restaurant_people_chosen_txt;
    @BindView(R.id.restaurant_list_distance_txt)
    TextView restaurant_distance_txt;
    @BindView(R.id.restaurant_list_opening_txt)
    TextView restaurant_openingTimes_txt;
    @BindView(R.id.restaurant_list_photo_imv)
    ImageView restaurant_picture_imv;
    @BindView(R.id.restaurant_list_one_star_imv)
    ImageView restaurant_one_star_imv;
    @BindView(R.id.restaurant_list_two_star_imv)
    ImageView restaurant_two_star_imv;
    @BindView(R.id.restaurant_list_three_star_imv)
    ImageView restaurant_three_star_imv;

    // FOR DATA
    int nb_wormates_joining, nb_like;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantInfo(RestaurantDetail restaurantDetail, double latB, double lngB, RequestManager glide, String todayDate, Context context) {

        //Display Restaurant Name & Address
        restaurant_name_txt.setText(restaurantDetail.getResult().getName());
        restaurant_address_txt.setText(restaurantDetail.getResult().getVicinity());

        //Get Number of Workmates joining to the restaurant
        getNumberOfWorkmatesJoining(restaurantDetail,todayDate);

        //Get Opening Information if available
        try {
            boolean isOpenNow = restaurantDetail.getResult().getOpeningHours().getOpenNow();
            if (isOpenNow) {
                try {
                    String opening_hour = DateUtility.formatWeekDayText(restaurantDetail.getResult().getOpeningHours().getWeekdayText());
                    if (opening_hour != null) {
                        String restaurant_open = context.getString(R.string.restaurant_open) + " : " + opening_hour;
                        Log.i("APP",restaurant_open);
                        restaurant_openingTimes_txt.setText(restaurant_open);
                    } else {
                        restaurant_openingTimes_txt.setText(context.getString(R.string.restaurant_open));
                    }
                }catch (NullPointerException e) {
                        restaurant_openingTimes_txt.setText(context.getString(R.string.restaurant_no_opening_info));
                    }
            } else {
                restaurant_openingTimes_txt.setText(context.getString(R.string.restaurant_close));
            }
        } catch (NullPointerException e) {
            restaurant_openingTimes_txt.setText(context.getString(R.string.restaurant_no_opening_info));
        }

        //Display Photo if available
        if (restaurantDetail.getResult().getPhotos() != null) {
            if (restaurantDetail.getResult().getPhotos().get(0).getPhotoReference() != null) {
                String restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" +
                        restaurantDetail.getResult().getPhotos().get(0).getPhotoReference() +
                        "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                glide.load(restaurantPictureUrl).into(restaurant_picture_imv);
            }
        } else glide.load(R.drawable.no_camera).into(restaurant_picture_imv);

        //Calculate Distance between User and Restaurant in meter
        Location restaurant_location = new Location("point A");
        restaurant_location.setLatitude(restaurantDetail.getResult().getGeometry().getLocation().getLat());
        restaurant_location.setLongitude(restaurantDetail.getResult().getGeometry().getLocation().getLng());

        Location my_location = new Location("point B");
        my_location.setLatitude(latB);
        my_location.setLongitude(lngB);

        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        String distance = formatDistance(restaurant_location.distanceTo(my_location));
        String restaurant_distance_stg = distance + " m";
        restaurant_distance_txt.setText(restaurant_distance_stg);

        //Show Restaurant Rating
        showRestaurantRating(restaurantDetail.getResult().getPlaceId());
    }

    private String formatDistance(double distance) {
        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(distance);
    }

    private void showRestaurantRating(String restaurant_id) {
        RestaurantHelper.getRestaurantLike(restaurant_id).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                nb_like = task.getResult().size();
                if (nb_like == 0) {
                    restaurant_one_star_imv.setVisibility(View.INVISIBLE);
                    restaurant_two_star_imv.setVisibility(View.INVISIBLE);
                    restaurant_three_star_imv.setVisibility(View.INVISIBLE);
                }
                if (nb_like >= 1 && nb_like <= 5) {
                    restaurant_one_star_imv.setVisibility(View.VISIBLE);
                    restaurant_two_star_imv.setVisibility(View.INVISIBLE);
                    restaurant_three_star_imv.setVisibility(View.INVISIBLE);
                }
                if (nb_like > 5 && nb_like <= 10) {
                    restaurant_one_star_imv.setVisibility(View.VISIBLE);
                    restaurant_two_star_imv.setVisibility(View.VISIBLE);
                    restaurant_three_star_imv.setVisibility(View.INVISIBLE);
                }
                if (nb_like > 10) {
                    restaurant_one_star_imv.setVisibility(View.VISIBLE);
                    restaurant_two_star_imv.setVisibility(View.VISIBLE);
                    restaurant_three_star_imv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getNumberOfWorkmatesJoining(RestaurantDetail restaurantDetail, String todayDate) {
        RestaurantHelper.getWorkmatesJoining(restaurantDetail.getResult().getPlaceId(),todayDate).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
               nb_wormates_joining = task.getResult().size();
               String nb_wormates_joining_txt = "(" + nb_wormates_joining + ")";
               restaurant_people_chosen_txt.setText(nb_wormates_joining_txt);
            }
        });
    }
}