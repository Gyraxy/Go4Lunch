package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.Manifest;
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
import com.duboscq.nicolas.go4lunch.api.RestaurantHelper;
import com.duboscq.nicolas.go4lunch.controllers.activities.ChatActivity;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.viewmodel.RestaurantViewModel;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.utils.DateUtility;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

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
    String my_location, updated_location;
    List<RestaurantDetail> restaurant_list, updated_restaurant_list;
    RestaurantViewModel mModel;
    private Disposable disposable;
    String key, NETWORK = "NETWORK";
    private HashMap<Marker, String> mHashMapId = new HashMap<>();


    public MapViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        restaurant_list = new ArrayList<>();
        key = getString(R.string.api_google_place_key);
        mModel = ViewModelProviders.of(getActivity()).get(RestaurantViewModel.class);
        mModel.getRestaurantResult().observe(this, new Observer<List<RestaurantDetail>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantDetail> results) {
                Log.i("MAP",restaurant_list.size()+"");
                if (!results.isEmpty()){
                    restaurant_list = mModel.getRestaurantResult().getValue();
                    getListWorkmatesJoining(restaurant_list);
                    onClickMarker();
                } else if (results.isEmpty()){
                    if (mMap != null) {
                        mMap.clear();
                        Log.i("MAP", "Marker Map Clear");
                    }
                    Toast.makeText(getContext(),getString(R.string.no_restaurant_found),Toast.LENGTH_SHORT).show();
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

    // -------
    // ACTIONS
    // -------

    // Center on User Location + Restaurant list updated
    @OnClick(R.id.fragment_map_view_my_location_floating_btn)
    public void goOnMyLocation() {
        centerMyLocation();
    }

    // Chat Activity
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

    public void generateMarkersOnMap(RestaurantDetail restaurantDetail, boolean isEmpty){
                Double lat = restaurantDetail.getResult().getGeometry().getLocation().getLat();
                Double lng = restaurantDetail.getResult().getGeometry().getLocation().getLng();
                LatLng restaurant_position = new LatLng(lat, lng);
                String restaurant_name = restaurantDetail.getResult().getName();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(restaurant_position);
                markerOptions.title(restaurant_name);
                if (isEmpty){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    Log.i("MAP","Markers RED");
                } else if (!isEmpty){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Log.i("MAP","Markers GREEN");
                }
                Marker m = mMap.addMarker(markerOptions);
                mHashMapId.put(m, restaurantDetail.getResult().getPlaceId());
            }

    private void onClickMarker(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String restaurant_placeId = mHashMapId.get(marker);

                Intent startRestaurantActivity = new Intent(getContext(), RestaurantActivity.class);
                startRestaurantActivity.putExtra("restaurant_id", restaurant_placeId);
                startActivity(startRestaurantActivity);

                return false;
            }
        });
    }

    private void loadNewRestaurantList(String location) {
        disposable = APIStreams.getRestaurantListAndDetail(100,key,location).subscribeWith(new DisposableObserver<List<RestaurantDetail>>() {
            @Override
            public void onNext(List<RestaurantDetail> restaurantDetails) {
                Log.i(NETWORK, "MapViewFragment Updated List: On Next");
                updated_restaurant_list = new ArrayList<>();
                updated_restaurant_list.addAll(restaurantDetails);
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

    public void getListWorkmatesJoining(List<RestaurantDetail> restaurant_details){
        if (restaurant_details!= null) {
            if (mMap != null) {
                mMap.clear();
                Log.i("MAP", "Marker Map Clear");
            }
            for (int i = 0; i < restaurant_details.size(); i++) {
                int finalI = i;
                RestaurantHelper.getWorkmatesJoining(restaurant_details.get(i).getResult().getPlaceId(), DateUtility.getDateTime())
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().getDocuments().isEmpty()) {
                                        generateMarkersOnMap(restaurant_list.get(finalI),true);
                                    } else {
                                        generateMarkersOnMap(restaurant_list.get(finalI),false);
                                    }

                                }
                            }
                        });
            }
        }
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