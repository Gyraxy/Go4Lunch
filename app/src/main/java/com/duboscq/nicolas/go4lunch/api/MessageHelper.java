package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.firebase.Message;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Created by Nicolas DUBOSCQ on 20/09/2018
 */
public class MessageHelper {

    private static final String COLLECTION_NAME = "messages";

    // --- GET ---

    public static CollectionReference getCollectionChatCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Query getMessageQuery(){
        return FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .limit(50);
    }

    public static Task<DocumentReference> createMessage(String textMessage, User userSender){

        Message message = new Message(textMessage, userSender);

        return FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .add(message);
    }

    public static Task<DocumentReference> createMessageWithImageForChat(String urlImage, String textMessage, User userSender){

        Message message = new Message(textMessage, urlImage, userSender);

        return FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .add(message);
    }
}