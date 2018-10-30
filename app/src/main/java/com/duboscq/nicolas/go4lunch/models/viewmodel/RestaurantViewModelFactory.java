package com.duboscq.nicolas.go4lunch.models.viewmodel;


import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;


public class RestaurantViewModelFactory implements ViewModelProvider.Factory {

    private final String key,location;

    public RestaurantViewModelFactory(String key, String location) {
        this.key = key;
        this.location = location;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RestaurantViewModel(key,location);
    }
}
