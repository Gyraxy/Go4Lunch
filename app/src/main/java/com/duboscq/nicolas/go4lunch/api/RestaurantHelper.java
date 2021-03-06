package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.firebase.Restaurant;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

    public static Task<QuerySnapshot> getWorkmatesJoining(String restaurant_id, String todayDate){
        return getRestaurantCollection().document(restaurant_id).collection("users" + todayDate).get();
    }

    public static Task<QuerySnapshot> getRestaurantLike(String restaurant_id){
        return RestaurantHelper.getRestaurantCollection().document(restaurant_id).collection("like").get();
    }

    // --- CREATE ---

    public static Task<Void> createUserforRestaurant(String restaurant_uid, String uid, String username, String date, String urlPicture, String lunch_id, String lunch_name, String lunch_date){
        User user = new User(uid, username, urlPicture, lunch_id, lunch_name, lunch_date);
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection("users"+date).document(uid).set(user);
    }

    public static Task<Void> addUserLiketoRestaurant(String restaurant_uid, String uid) {
        User user = new User(uid, "XXX", "XXX", "XXX", "XXX", "XX-XX-XXXX");
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection("like").document(uid).set(user);
    }

    public static Task<Void> addLikeRestaurant(String restaurant_uid, int like) {
        Restaurant restaurant_toCreate = new Restaurant(like);
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).set(restaurant_toCreate);
    }

    // --- DELETE ---

    //Delete user in Restaurant List when user cancel his choice or choose another restaurant
    public static Task<Void> deleteUserInRestaurantList(String restaurant_uid, String users_date, String user_uid) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection(users_date).document(user_uid).delete();
    }

    //Delete user like in restaurant list
    public static Task<Void> deleteUserLikeInRestaurantList(String restaurant_uid, String user_uid) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection("like").document(user_uid).delete();
    }
}
