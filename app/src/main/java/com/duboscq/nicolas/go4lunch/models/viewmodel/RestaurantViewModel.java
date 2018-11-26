package com.duboscq.nicolas.go4lunch.models.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantViewModel extends ViewModel {

    private MutableLiveData<List<RestaurantDetail>> restaurant_result;
    private Disposable disposable;
    private String NETWORK = "NETWORK";
    private String key,location;

    public RestaurantViewModel(String key,String location) {
        this.key = key;
        this.location = location;
    }

    public LiveData<List<RestaurantDetail>> getRestaurantResult() {
        if (restaurant_result == null) {
            restaurant_result= new MutableLiveData<>();
            loadRestaurantResult();
        }
        return restaurant_result;
    }

    public LiveData<List<RestaurantDetail>> setNewRestaurantResult(List<RestaurantDetail> new_result) {
        restaurant_result.setValue(new_result);
        return restaurant_result;
    }

    private void loadRestaurantResult() {

        disposable = APIStreams.getRestaurantListAndDetail(100,key,location).subscribeWith(new DisposableObserver<List<RestaurantDetail>>() {

            @Override
            public void onNext(List<RestaurantDetail> restaurantDetails) {
                restaurant_result.setValue(restaurantDetails);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.isDisposed();
    }
}