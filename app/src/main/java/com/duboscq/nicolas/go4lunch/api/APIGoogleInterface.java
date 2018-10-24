package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIGoogleInterface {

    @GET("nearbysearch/json?type=restaurant")
    Observable<RestaurantPlace> getRestaurantList(@Query("radius") int radius,
                                                  @Query("key") String key,
                                                  @Query("location") String location);

    @GET("details/json")
    Observable<RestaurantDetail> getRestaurantDetail(@Query("placeid") String placeid,
                                                     @Query("key") String key);
}
