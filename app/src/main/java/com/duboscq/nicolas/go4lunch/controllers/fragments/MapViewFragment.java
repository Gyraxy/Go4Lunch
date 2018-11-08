package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.Manifest;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.controllers.activities.ChatActivity;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModel;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class MapViewFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    //FOR DESIGN
    @BindView(R.id.fragment_map_view_my_location_floating_btn)
    FloatingActionButton my_location_btn;
    @BindView(R.id.fragment_map_view_message_floating_btn)
    FloatingActionButton message_btn;

    //FOR DATA
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient mFusedLocationClient;
    LatLng myLatLng;
    Location mLastLocation;
    String my_location, updated_location, restaurantPictureUrl;
    List<Result> restaurant_list, updated_restaurant_list;
    RestaurantViewModel mModel;
    private Disposable disposable;
    String key, NETWORK = "NETWORK";
    private HashMap<Marker, String> mHashMapId = new HashMap<>();
    private HashMap<Marker, String> mHashMapPhoto = new HashMap<>();


    public MapViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        restaurant_list = new ArrayList<>();
        key = getString(R.string.api_google_place_key);
        mModel = ViewModelProviders.of(getActivity()).get(RestaurantViewModel.class);
        mModel.getRestaurantResult().observe(this, new Observer<List<Result>>() {
            @Override
            public void onChanged(@Nullable List<Result> results) {
                if (results != null){
                    restaurant_list = mModel.getRestaurantResult().getValue();
                    generateMarkersOnMap();
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
        mMap.setOnMyLocationClickListener(this);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
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
                        updated_location = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                        RestaurantFragment.user_lng = mLastLocation.getLongitude();
                        RestaurantFragment.user_lat = mLastLocation.getLatitude();
                        loadNewRestaurantList(updated_location);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17));
                    }
                }
            });
        }
    }

    private void generateMarkersOnMap(){
        if (restaurant_list != null){
            mMap.clear();
            for (int i = 0; i<restaurant_list.size();i++) {
                Double lat = restaurant_list.get(i).getGeometry().getLocation().getLat();
                Double lng = restaurant_list.get(i).getGeometry().getLocation().getLng();
                LatLng restaurant_position = new LatLng(lat, lng);
                String restaurant_name = restaurant_list.get(i).getName();

                if (restaurant_list.get(i).getPhotos() != null) {
                    if (restaurant_list.get(i).getPhotos().get(0).getPhotoReference() != null){
                        restaurantPictureUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+
                                restaurant_list.get(i).getPhotos().get(0).getPhotoReference()+
                                "&key=AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
                    }
                } else restaurantPictureUrl = null;

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(restaurant_position);
                markerOptions.title(restaurant_name);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                Marker m = mMap.addMarker(markerOptions);

                mHashMapId.put(m, restaurant_list.get(i).getPlaceId());
                mHashMapPhoto.put(m, restaurantPictureUrl);
            }
        }
    }

    private void onClickMarker(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String restaurant_placeId = mHashMapId.get(marker);
                String restaurant_url = mHashMapPhoto.get(marker);

                Intent startRestaurantActivity = new Intent(getContext(), RestaurantActivity.class);
                startRestaurantActivity.putExtra("restaurant_id", restaurant_placeId);
                startRestaurantActivity.putExtra("restaurant_image_url",restaurant_url);
                startActivity(startRestaurantActivity);

                return false;
            }
        });
    }

    private void loadNewRestaurantList(String location) {
        disposable = APIStreams.getRestaurantList(20,key,location).subscribeWith(new DisposableObserver<RestaurantPlace>() {
            @Override
            public void onNext(RestaurantPlace restaurantPlace) {
                Log.i(NETWORK, "MapViewFragment Updated List: On Next");
                updated_restaurant_list = new ArrayList<>();
                updated_restaurant_list.addAll(restaurantPlace.getResults());
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "MapViewFragment Updated List: On Error" + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "MapViewFragment Updated List: On Complete !!");
                mModel.setNewRestaurantResult(updated_restaurant_list);
            }
        });
    }

    // ------------------------------
    // AUTO GENERATED METHOD NOT USED
    // ------------------------------

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }
}