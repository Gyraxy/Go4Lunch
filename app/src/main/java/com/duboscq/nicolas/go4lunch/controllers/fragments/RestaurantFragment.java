package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.RestaurantListRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantFragment extends Fragment {

    //FOR DESIGN
    @BindView(R.id.fragment_restaurant_recycler_view) RecyclerView restaurant_recyclerView;

    //FOR DATA
    float distance = 100;
    List<Result> restaurant_list;
    RestaurantListRecyclerViewAdapter adapter;
    private Disposable disposable;
    String NETWORK = "NETWORK";
    public RestaurantFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        ButterKnife.bind(this, view);
        configureRecyclerView();
        configureAndShowRestaurantList();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void configureRecyclerView() {
        this.restaurant_list = new ArrayList<>();
        this.adapter = new RestaurantListRecyclerViewAdapter(this.restaurant_list,distance);
        this.restaurant_recyclerView.setAdapter(this.adapter);
        this.restaurant_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public void configureAndShowRestaurantList() {
        disposable = APIStreams.getRestaurantList(20,getString(R.string.api_google_place_key)).subscribeWith(new DisposableObserver<RestaurantPlace>() {
            @Override
            public void onNext(RestaurantPlace restaurantPlace) {
                Log.i(NETWORK, "RestauFragment : On Next");
                restaurant_list.addAll(restaurantPlace.getResults());
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "RestauFragment : On Error " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "RestauFragment : On Complete !!");
                adapter.notifyDataSetChanged();
            }
        });
    }
}
