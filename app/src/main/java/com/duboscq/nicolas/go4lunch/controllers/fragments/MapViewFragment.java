package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.Manifest;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.controllers.activities.ChatActivity;
import com.duboscq.nicolas.go4lunch.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapViewFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

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


    public MapViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableMyLocation();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
                    }
                }
            });
        } else {
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("APP","MapFrag : OnResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("APP","MapFrag : OnPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("APP","MapFrag : OnStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("APP","MapFrag : OnDestroy");
    }
}
