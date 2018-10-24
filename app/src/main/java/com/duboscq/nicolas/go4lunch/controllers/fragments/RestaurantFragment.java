package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.RestaurantListRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.utils.DividerItemDecoration;
import com.duboscq.nicolas.go4lunch.utils.ItemClickSupport;
import com.duboscq.nicolas.go4lunch.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantFragment extends Fragment {

    //FOR DESIGN
    @BindView(R.id.fragment_restaurant_recycler_view) RecyclerView restaurant_recyclerView;
    @BindView(R.id.fragment_restaurant_swipe_container) SwipeRefreshLayout swipeRefreshLayout;

    //FOR DATA
    List<Result> restaurant_list;
    RestaurantListRecyclerViewAdapter adapter;
    private Disposable disposable;
    String NETWORK = "NETWORK";
    Location mLastLocation;
    String my_location;
    FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    public RestaurantFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        ButterKnife.bind(this, view);
        getMyLocation();
        configureOnClickRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void configureRecyclerView() {
        this.restaurant_list = new ArrayList<>();
        this.adapter = new RestaurantListRecyclerViewAdapter(this.restaurant_list,mLastLocation.getLatitude(),mLastLocation.getLongitude(),Glide.with(this));
        this.restaurant_recyclerView.setAdapter(this.adapter);
        this.restaurant_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Add custom divider between Recycler's views
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

    public void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyLocation();
            }
        });
    }

    public void getMyLocation(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        my_location = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                        configureRecyclerView();
                        configureAndShowRestaurantList();
                        configureSwipeRefreshLayout();
                    }
                }
            });
        } else {
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    public void configureAndShowRestaurantList() {
        disposable = APIStreams.getRestaurantList(100,getString(R.string.api_google_place_key),my_location).subscribeWith(new DisposableObserver<RestaurantPlace>() {
            @Override
            public void onNext(RestaurantPlace restaurantPlace) {
                Log.i(NETWORK, "RestauFragment : On Next");
                if (!restaurant_list.isEmpty()){
                    restaurant_list.clear();
                }
                restaurant_list.addAll(restaurantPlace.getResults());
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "RestauFragment : On Error " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "RestauFragment : On Complete !!");
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
