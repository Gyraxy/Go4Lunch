package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.utils.RetrofitUtility;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class APIStreams {
    public static Observable<RestaurantPlace> getRestaurantList(int radius,String key){
        APIGoogleInterface apiInterface = RetrofitUtility.getInstance().create(APIGoogleInterface.class);
        return apiInterface.getRestaurantList(radius,key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
