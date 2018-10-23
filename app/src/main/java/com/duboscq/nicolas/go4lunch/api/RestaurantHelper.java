package com.duboscq.nicolas.go4lunch.api;


import com.duboscq.nicolas.go4lunch.models.firebase.Restaurant;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RestaurantHelper {

    private static final String COLLECTION_NAME = "restaurant";

    // --- GET ---

    public static CollectionReference getCollectionRestaurantCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createRestaurant(String restaurant_name, int numberOfLikes) {
        Restaurant restaurantToCreate = new Restaurant(restaurant_name, numberOfLikes);
        return RestaurantHelper.getCollectionRestaurantCollection().document(restaurant_name).set(restaurantToCreate);
    }
}
