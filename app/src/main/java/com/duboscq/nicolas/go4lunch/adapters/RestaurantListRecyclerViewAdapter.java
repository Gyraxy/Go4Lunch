package com.duboscq.nicolas.go4lunch.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.bumptech.glide.RequestManager;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.views.RestaurantViewHolder;

import java.util.List;

public class RestaurantListRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<Result> restaurant_place_result;
    private RequestManager glide;
    private double latB,lngB;
    private int nb_workmates;

    public RestaurantListRecyclerViewAdapter(List<Result> result, double latB, double lngB, RequestManager glide, int nb_workmates) {
        this.restaurant_place_result = result;
        this.latB = latB;
        this.lngB = lngB;
        this.glide = glide;
        this.nb_workmates = nb_workmates;
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
        holder.updateRestaurantInfo(this.restaurant_place_result.get(position),latB,lngB,this.glide,nb_workmates);
    }

    @Override
    public int getItemCount() {
        return restaurant_place_result.size();
    }
}
