package com.duboscq.nicolas.go4lunch.controllers.fragments;


import android.arch.lifecycle.Observer;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.RestaurantListRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModel;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.utils.DateUtility;
import com.duboscq.nicolas.go4lunch.utils.DividerItemDecoration;
import com.duboscq.nicolas.go4lunch.utils.ItemClickSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantFragment extends Fragment {

    //FOR DESIGN
    @BindView(R.id.fragment_restaurant_recycler_view) RecyclerView restaurant_recyclerView;
    @BindView(R.id.fragment_restaurant_swipe_container) SwipeRefreshLayout restaurant_swipe_refresh;
    @BindView(R.id.fragment_restaurant_recycler_view_empty) TextView no_restaurant_txt;

    //FOR DATA
    RestaurantListRecyclerViewAdapter adapter;
    RestaurantViewModel mModel;
    List<RestaurantDetail> restaurant_list;
    public static double user_lat,user_lng;
    final static String ARG_LAT = "ARG_LAT",ARG_LNG = "ARG_LNG";
    String todayDate;

    public static RestaurantFragment newInstance(double user_lat, double user_lng) {
        RestaurantFragment fragment = new RestaurantFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, user_lat);
        args.putDouble(ARG_LNG, user_lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user_lat = getArguments().getDouble(ARG_LAT);
            user_lng = getArguments().getDouble(ARG_LNG);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        ButterKnife.bind(this, view);
        todayDate = DateUtility.getDateTime();
        mModel = ViewModelProviders.of(getActivity()).get(RestaurantViewModel.class);
        mModel.getRestaurantResult().observe(this, new Observer<List<RestaurantDetail>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantDetail> results) {
                if (!results.isEmpty()){
                    Log.i("APP","Restaurant List not empty");
                    restaurant_list = mModel.getRestaurantResult().getValue();
                    no_restaurant_txt.setVisibility(View.GONE);
                    restaurant_recyclerView.setVisibility(View.VISIBLE);
                    restaurant_swipe_refresh.setEnabled(true);
                    configureRecyclerView(restaurant_list);
                    configureOnClickRecyclerView();
                    configureSwipeRefreshLayout(restaurant_list);
                } else if (results.isEmpty()) {
                    Log.i("APP","Restaurant List empty");
                    restaurant_recyclerView.setVisibility(View.GONE);
                    no_restaurant_txt.setVisibility(View.VISIBLE);
                    restaurant_swipe_refresh.setEnabled(false);
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    // --------------------
    // UI
    // --------------------

    // RECYCLERVIEW CONFIGURATION

    public void configureRecyclerView(List<RestaurantDetail> restaurant_list) {
        this.adapter = new RestaurantListRecyclerViewAdapter(restaurant_list,user_lat,user_lng,Glide.with(this),todayDate,getContext());
        this.restaurant_recyclerView.setAdapter(this.adapter);
        this.restaurant_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(restaurant_recyclerView.getContext(), R.drawable.horizontal_divider);
        restaurant_recyclerView.addItemDecoration(mDividerItemDecoration);
        restaurant_swipe_refresh.setRefreshing(false);
    }

    //CLICK ON RECYCLERVIEW ITEM CONFIGURATION

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(restaurant_recyclerView, R.layout.fragment_restaurant)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent i = new Intent(getActivity(), RestaurantActivity.class);
                        i.putExtra("restaurant_id",restaurant_list.get(position).getResult().getPlaceId());
                        if (restaurant_list.get(position).getResult().getPhotos() != null) {
                            if (restaurant_list.get(position).getResult().getPhotos().get(0).getPhotoReference() != null){
                                String restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+
                                        restaurant_list.get(position).getResult().getPhotos().get(0).getPhotoReference()+
                                        "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                                i.putExtra("restaurant_image_url",restaurantPictureUrl);
                            }
                        }
                        startActivity(i);
                    }
                });
    }

    //SWIPE CONFIGURATION

    private void configureSwipeRefreshLayout(List<RestaurantDetail> restaurant_list) {
        restaurant_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                configureRecyclerView(restaurant_list);
            }
        });
    }
}