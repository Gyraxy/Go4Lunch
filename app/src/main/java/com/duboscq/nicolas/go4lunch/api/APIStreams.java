package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;
import com.duboscq.nicolas.go4lunch.models.restaurant.Result;
import com.duboscq.nicolas.go4lunch.utils.RetrofitUtility;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class APIStreams {

    //Stream to get the List of Restaurant around the user with a radius of radius meter
    public static Observable<RestaurantPlace> getRestaurantList(double radius, String key, String location) {
        APIGoogleInterface apiInterface = RetrofitUtility.getInstance().create(APIGoogleInterface.class);
        return apiInterface.getRestaurantList(radius, key, location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    //Stream to get the detail of a Restaurant
    public static Observable<RestaurantDetail> getRestaurantDetail(String placeid, String key) {
        APIGoogleInterface apiInterface = RetrofitUtility.getInstance().create(APIGoogleInterface.class);
        return apiInterface.getRestaurantDetail(placeid, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    //Stream to get the List of Restaurant around the user and the detail for each restaurant found. Return a list of Detail Restaurant
    public static Observable<List<RestaurantDetail>> getRestaurantListAndDetail(double radius, String key, String location) {
        return getRestaurantList(radius, key, location)
                .map(RestaurantPlace::getResults)
                .concatMap((Function<List<Result>, Observable<List<RestaurantDetail>>>) results -> Observable.fromIterable(results)
                        .concatMap((Function<Result, Observable<RestaurantDetail>>) result -> getRestaurantDetail(result.getPlaceId(),key))
                        .toList()
                        //Include data from firebase
                        .toObservable());
    }
}
