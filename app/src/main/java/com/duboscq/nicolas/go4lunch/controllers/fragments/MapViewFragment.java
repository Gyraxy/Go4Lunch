package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.controllers.activities.ChatActivity;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModel;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapViewFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerClickListener {

    //FOR DESIGN
    @BindView(R.id.fragment_map_view_my_location_floating_btn)
    FloatingActionButton my_location_btn;
    @BindView(R.id.fragment_map_view_message_floating_btn)
    FloatingActionButton message_btn;

    //FOR DATA
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient mFusedLocationClient;
    LatLng myLatLng;
    Location mLastLocation;
    String my_location;
    List<Result> restaurant_list;
    RestaurantViewModel mModel;


    public MapViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableMyLocation();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        restaurant_list = new ArrayList<>();
        mModel = ViewModelProviders.of(getActivity()).get(RestaurantViewModel.class);
        mModel.getRestaurantResult().observe(this, new android.arch.lifecycle.Observer<List<Result>>() {
            @Override
            public void onChanged(@Nullable List<Result> results) {
                if (results != null){
                    generateMarkersOnMap(results);
                    onClickMarker();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        ButterKnife.bind(this, view);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //ACTIONS
    @OnClick(R.id.fragment_map_view_my_location_floating_btn)
    public void goOnMyLocation() {
        centerMyLocation();
    }

    @OnClick(R.id.fragment_map_view_message_floating_btn)
    public void goChat() {
        Intent chat = new Intent(getActivity(),ChatActivity.class);
        startActivity(chat);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        enableMyLocation();
        mMap.setOnMyLocationClickListener(this);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        myLatLng = new LatLng(mLastLocation.getLatitude(),
                                mLastLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17));
                        my_location = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                    }
                }
            });
        } else {
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    //PERMISSIONS
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    public void centerMyLocation(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                        myLatLng = new LatLng(mLastLocation.getLatitude(),
                                mLastLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17));
                    }
                }
            });
        } else {
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    private void generateMarkersOnMap(List<Result> restaurant_result){
        for (int i = 0; i<restaurant_result.size();i++){
            Double lat = restaurant_result.get(i).getGeometry().getLocation().getLat();
            Double lng = restaurant_result.get(i).getGeometry().getLocation().getLng();
            LatLng restaurant = new LatLng(lat,lng);
            String restaurant_name = restaurant_result.get(i).getName();
            mMap.addMarker(new MarkerOptions()
                    .position(restaurant)
                    .title(restaurant_name));
        }
    }

    private void onClickMarker(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (int i = 0; i<restaurant_list.size();i++){
                    if (marker.getTitle().equals(restaurant_list.get(i).getName())){
                        Intent go_restaurant = new Intent(getContext(),RestaurantActivity.class);
                        go_restaurant.putExtra("restaurant_id",restaurant_list.get(i).getPlaceId());
                        if (restaurant_list.get(i).getPhotos() != null) {
                            if (restaurant_list.get(i).getPhotos().get(0).getPhotoReference() != null){
                                String restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+
                                        restaurant_list.get(i).getPhotos().get(0).getPhotoReference()+
                                        "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                                go_restaurant.putExtra("restaurant_image_url",restaurantPictureUrl);
                            }
                        }
                        startActivity(go_restaurant);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}