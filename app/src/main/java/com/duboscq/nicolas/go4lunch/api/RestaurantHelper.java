package com.duboscq.nicolas.go4lunch.api;


import com.duboscq.nicolas.go4lunch.models.firebase.Restaurant;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RestaurantHelper {

    private static final String COLLECTION_NAME = "restaurant";

    // --- GET ---

    public static CollectionReference getRestaurantCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Task<DocumentSnapshot> getRestaurant(String restaurant_name){
        return  RestaurantHelper.getRestaurantCollection().document(restaurant_name).get();
    }

    // --- CREATE ---

    public static Task<Void> createRestaurant(String restaurant_name, int likes) {
        Restaurant restaurantToCreate = new Restaurant(likes);
        return RestaurantHelper.getRestaurantCollection().document(restaurant_name).set(restaurantToCreate);
    }

    public static Task<Void> createUserforRestaurant(String restaurant_uid, String uid, String username, String date, String urlPicture, String lunch, String lunch_date,String lunch_url){

        User user = new User(uid, username, urlPicture, lunch, lunch_date, lunch_url);

        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection("users"+date).document(uid).set(user);
    }

    // --- UPDATE ---

    public static Task<Void> updateRestaurantLikes(String restaurant_name, int likes) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_name).update("likes",likes);
    }
}
