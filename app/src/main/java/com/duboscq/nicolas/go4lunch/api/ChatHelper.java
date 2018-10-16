package com.duboscq.nicolas.go4lunch.api;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by Nicolas DUBOSCQ on 20/09/2018
 */
public class ChatHelper {

    private static final String COLLECTION_NAME = "chats";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getChatCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }
}