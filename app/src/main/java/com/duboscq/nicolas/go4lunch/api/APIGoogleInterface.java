package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIGoogleInterface {

    @GET("json?location=48.8647,2.3490&type=restaurant")
    Observable<RestaurantPlace> getRestaurantList(@Query("radius") int radius,
                                                  @Query("key") String key);
}
