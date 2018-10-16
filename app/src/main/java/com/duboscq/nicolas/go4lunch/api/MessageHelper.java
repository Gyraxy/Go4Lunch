package com.duboscq.nicolas.go4lunch.api;

import com.duboscq.nicolas.go4lunch.models.firebase.Message;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

/**
 * Created by Nicolas DUBOSCQ on 20/09/2018
 */
public class MessageHelper {

    private static final String COLLECTION_NAME = "messages";

    // --- GET ---

    public static Query getAllMessageForChat(String chat){
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .limit(50);
    }

    public static Task<DocumentReference> createMessageForChat(String textMessage, String chat, User userSender){

        Message message = new Message(textMessage, userSender);

        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }
}