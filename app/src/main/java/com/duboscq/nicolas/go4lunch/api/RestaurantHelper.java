package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.firebase.Restaurant;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
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

    public static Task<DocumentSnapshot> getRestaurantUserLikeList(String restaurant_id, String user_id){
        return getRestaurantCollection().document(restaurant_id).collection("like").document(user_id).get();
    }

    // --- CREATE ---

    public static Task<Void> createUserforRestaurant(String restaurant_uid, String uid, String username, String date, String urlPicture, String lunch, String lunch_date,String lunch_url){
        User user = new User(uid, username, urlPicture, lunch, lunch_date, lunch_url);
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection("users"+date).document(uid).set(user);
    }

    public static Task<Void> addUserLiketoRestaurant(String restaurant_uid, String uid) {
        User user = new User(uid, "XXX", "XXX", "XXX", "XX-XX-XXXX", "XXX");
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection("like").document(uid).set(user);
    }

    public static Task<Void> addLikeRestaurant(String restaurant_uid, int like) {
        Restaurant restaurant_toCreate = new Restaurant(like);
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).set(restaurant_toCreate);
    }

    // --- DELETE ---
    public static Task<Void> deleteUserInRestaurantList(String restaurant_uid, String users_date, String user_uid) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection(users_date).document(user_uid).delete();
    }

    public static Task<Void> deleteUserLikeInRestaurantList(String restaurant_uid, String user_uid) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection("like").document(user_uid).delete();
    }
}
