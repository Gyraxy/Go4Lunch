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

    public static Task<Void> createUser(String uid, String username, String urlPicture, String lunch_id, String lunch_name, String lunch_date) {
        User userToCreate = new User(uid, username, urlPicture, lunch_id, lunch_name, lunch_date);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    public static Query getAllWorkmates() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Query getAllRestaurantWorkmates(String restaurant_uid, String users_date) {
        return RestaurantHelper.getRestaurantCollection().document(restaurant_uid).collection(users_date);
    }

    // --- UPDATE ---

    public static Task<Void> updateUsername(String username, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("username", username);
    }

    public static Task<Void> updateUserLunchId(String username, String restaurant_uid) {
        return UserHelper.getUsersCollection().document(username).update("lunchId",restaurant_uid);
    }

    public static Task<Void> updateUserLunchName(String username, String restaurant_name) {
        return UserHelper.getUsersCollection().document(username).update("lunchName",restaurant_name);
    }

    public static Task<Void> updateUserLunchDate(String username, String date) {
        return UserHelper.getUsersCollection().document(username).update("lunchDate",date);
    }


    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }
}