package com.duboscq.nicolas.go4lunch.utils;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtility {

    private static Retrofit INSTANCE = null;

    public static Retrofit getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            }
            return INSTANCE;
        }
}
