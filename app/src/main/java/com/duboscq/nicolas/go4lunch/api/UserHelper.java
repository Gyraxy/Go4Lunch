package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserHelper {

    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture, String lunch, String lunch_date,String lunch_url) {
        User userToCreate = new User(uid, username, urlPicture, lunch, lunch_date, lunch_url);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    public static Query getAllWorkmates() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Query getAllRestaurantWorkmates(String collection_name, String restaurant_uid, String users_date) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection(users_date);
    }

    // --- UPDATE ---

    public static Task<Void> updateUsername(String username, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("username", username);
    }

    public static Task<Void> updateUserLunch(String username, String restaurant_uid) {
        return UserHelper.getUsersCollection().document(username).update("lunch",restaurant_uid);
    }

    public static Task<Void> updateUserLunchDate(String username, String date) {
        return UserHelper.getUsersCollection().document(username).update("lunchDate",date);
    }

    public static Task<Void> updateUserLunchUrl(String username, String restaurant_url) {
        return UserHelper.getUsersCollection().document(username).update("lunchUrl",restaurant_url);
    }


    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }

    public static Task<Void> deleteUserInRestaurantList(String restaurant_uid, String users_date, String user_uid) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection(users_date).document(user_uid).delete();
    }
}