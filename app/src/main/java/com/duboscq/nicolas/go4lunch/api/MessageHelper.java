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

    //Get all the message created
    public static Query getMessageQuery(){
        return getCollectionChatCollection()
                .orderBy("dateCreated")
                .limit(50);
    }

    // --- CREATE ---

    //Create a message without any image
    public static Task<DocumentReference> createMessage(String textMessage, User userSender){

        Message message = new Message(textMessage, userSender);

        return getCollectionChatCollection()
                .add(message);
    }

    //Create a message with image
    public static Task<DocumentReference> createMessageWithImageForChat(String urlImage, String textMessage, User userSender){

        Message message = new Message(textMessage, urlImage, userSender);

        return getCollectionChatCollection()
                .add(message);
    }
}