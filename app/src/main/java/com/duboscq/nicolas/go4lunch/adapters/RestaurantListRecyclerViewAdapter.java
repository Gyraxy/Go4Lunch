package com.duboscq.nicolas.go4lunch.adapters;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.views.RestaurantViewHolder;

import java.util.List;

public class RestaurantListRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<Result> restaurant_place_result;
    private float distance;

    public RestaurantListRecyclerViewAdapter(List<Result> result, float distance) {
        this.restaurant_place_result = result;
        this.distance = distance;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.restaurant_list_view, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.updateRestaurantInfo(this.restaurant_place_result.get(position),distance);
    }

    @Override
    public int getItemCount() {
        return restaurant_place_result.size();
    }
}
