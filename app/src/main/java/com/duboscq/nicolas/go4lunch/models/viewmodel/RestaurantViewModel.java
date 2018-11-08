package com.duboscq.nicolas.go4lunch.models.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantViewModel extends ViewModel {

    private MutableLiveData<List<Result>> restaurant_result;
    private Disposable disposable;
    private String NETWORK = "NETWORK";
    private String key,location;

    public RestaurantViewModel(String key,String location) {
        this.key = key;
        this.location = location;
    }

    public LiveData<List<Result>> getRestaurantResult() {
        if (restaurant_result == null) {
            restaurant_result= new MutableLiveData<>();
            loadRestaurantResult();
        }
        return restaurant_result;
    }

    public LiveData<List<Result>> setNewRestaurantResult(List<Result> new_result) {
        restaurant_result.setValue(new_result);
        return restaurant_result;
    }

    private void loadRestaurantResult() {
        disposable = APIStreams.getRestaurantList(20,key,location).subscribeWith(new DisposableObserver<RestaurantPlace>() {
            @Override
            public void onNext(RestaurantPlace restaurantPlace) {
                Log.i(NETWORK, "ViewModel: On Next");
                restaurant_result.setValue(restaurantPlace.getResults());
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "ViewModel : On Error " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "ViewModel : On Complete !!");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.isDisposed();
    }
}