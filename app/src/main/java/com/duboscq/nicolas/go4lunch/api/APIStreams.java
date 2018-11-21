package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.utils.RetrofitUtility;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class APIStreams {
    public static Observable<RestaurantPlace> getRestaurantList(double radius, String key, String location) {
        APIGoogleInterface apiInterface = RetrofitUtility.getInstance().create(APIGoogleInterface.class);
        return apiInterface.getRestaurantList(radius, key, location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<RestaurantDetail> getRestaurantDetail(String placeid, String key) {
        APIGoogleInterface apiInterface = RetrofitUtility.getInstance().create(APIGoogleInterface.class);
        return apiInterface.getRestaurantDetail(placeid, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
