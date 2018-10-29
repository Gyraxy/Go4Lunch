package com.duboscq.nicolas.go4lunch.controllers.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.RestaurantListRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.controllers.activities.MainActivity;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.RestaurantViewModel;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.utils.DividerItemDecoration;
import com.duboscq.nicolas.go4lunch.utils.ItemClickSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantFragment extends Fragment {

    //FOR DESIGN
    @BindView(R.id.fragment_restaurant_recycler_view) RecyclerView restaurant_recyclerView;

    //FOR DATA
    RestaurantListRecyclerViewAdapter adapter;
    RestaurantViewModel mModel;
    List<Result> restaurant_list;


    public RestaurantFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        ButterKnife.bind(this, view);
        mModel = ViewModelProviders.of(getActivity()).get(RestaurantViewModel.class);
        mModel.getRestaurantResult().observe(this, new android.arch.lifecycle.Observer<List<Result>>() {
            @Override
            public void onChanged(@Nullable List<Result> results) {
                if (results != null){
                    restaurant_list = mModel.getRestaurantResult().getValue();
                    configureRecyclerView();
                    configureOnClickRecyclerView();
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void configureRecyclerView() {
        this.adapter = new RestaurantListRecyclerViewAdapter(restaurant_list,48.8646983,2.349,Glide.with(this));
        this.restaurant_recyclerView.setAdapter(this.adapter);
        this.restaurant_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(restaurant_recyclerView.getContext(), R.drawable.horizontal_divider);
        restaurant_recyclerView.addItemDecoration(mDividerItemDecoration);
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(restaurant_recyclerView, R.layout.fragment_restaurant)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent i = new Intent(getActivity(), RestaurantActivity.class);
                        i.putExtra("restaurant_id",restaurant_list.get(position).getPlaceId());
                        if (restaurant_list.get(position).getPhotos() != null) {
                            if (restaurant_list.get(position).getPhotos().get(0).getPhotoReference() != null){
                                String restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+
                                        restaurant_list.get(position).getPhotos().get(0).getPhotoReference()+
                                        "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                                i.putExtra("restaurant_image_url",restaurantPictureUrl);
                            }
                        }
                        startActivity(i);
                    }
                });
    }
}