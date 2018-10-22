package com.duboscq.nicolas.go4lunch.views;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.restaurant_list_name_txt) TextView restaurant_name;
    @BindView(R.id.restaurant_list_adress_txt) TextView restaurant_address;
    @BindView(R.id.restaurant_list_people_txt) TextView restaurant_people_chosen;
    @BindView(R.id.restaurant_list_distance_txt) TextView restaurant_distance;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantInfo(Result restaurantPlaceResult,float distance){
        restaurant_name.setText(restaurantPlaceResult.getName());
        restaurant_address.setText(restaurantPlaceResult.getVicinity());
        restaurant_people_chosen.setText("(0)");
        restaurant_distance.setText(distance+" m");
    }
}
