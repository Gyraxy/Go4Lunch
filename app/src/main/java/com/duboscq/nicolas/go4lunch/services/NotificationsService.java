package com.duboscq.nicolas.go4lunch.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.api.RestaurantHelper;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.controllers.activities.MainActivity;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.utils.DateUtility;
import com.duboscq.nicolas.go4lunch.utils.FirebaseUtils;
import com.duboscq.nicolas.go4lunch.utils.SharedPreferencesUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class NotificationsService extends FirebaseMessagingService {

    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "GO4LUNCH", NETWORK = "NETWORK";
    String restaurant_adress, restaurant_name, chosenrestaurantId, notification, lunch_date;
    String answerWorkmatesJoining;
    private Disposable disposable;
    List<DocumentSnapshot> wormatesJoining;


    // MESSAGE RECEIVED FROM FIREBASE
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("NETWORK","On Message Firebase Notification received");
        notification = SharedPreferencesUtility.getString(this,"NOTIFICATION");
        if (remoteMessage.getNotification() != null && notification.equals("ON")) {
            UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User current_user = documentSnapshot.toObject(User.class);
                    lunch_date = current_user.getLunchDate();
                    if (lunch_date.equals(DateUtility.getDateTime())){
                        Log.i("NETWORK","Receive notification from Firebase");
                        getUserRestaurantId();
                    }
                }
            });

        }
    }

    // VISUAL NOTIFICATION CONFIGURATION AND SHOW TO DEVICE
    private void sendVisualNotification(String restaurant_name, String restaurant_adress) {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Go4Lunch");
        inboxStyle.addLine(getString(R.string.notification_lunch)+restaurant_name);
        inboxStyle.addLine(restaurant_adress);
        inboxStyle.addLine(answerWorkmatesJoining);

        String channelId = "my channel";

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.home_icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Go4Lunch Notification")
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Message provenant de Firebase";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

    // GET USER RESTAURANT ID CHOICE IF THE DATE OF LUNCH IS TODAY
    private void getUserRestaurantId() {
        UserHelper.getUser(FirebaseUtils.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                User currentUser = documentSnapshot.toObject(User.class);
                chosenrestaurantId = currentUser.getLunchId();
                getRestaurantDetail(chosenrestaurantId);
            }
        });
    }

    // GET RESTAURANT DETAIL
    private void getRestaurantDetail(final String restaurant_id) {
        disposable = APIStreams.getRestaurantDetail(restaurant_id,getString(R.string.api_google_place_key)).subscribeWith(new DisposableObserver<RestaurantDetail>() {
            @Override
            public void onNext(RestaurantDetail restaurantDetail) {
                Log.i(NETWORK, "Notification : On Next");
                restaurant_name = restaurantDetail.getResult().getName();
                restaurant_adress = restaurantDetail.getResult().getFormattedAddress();
            }

            @Override
            public void onError(Throwable e) {
                Log.i(NETWORK, "Notification : On Error " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.i(NETWORK, "Notification : On Complete !!");
                RestaurantHelper.getWorkmatesJoining(restaurant_id, DateUtility.getDateTime())
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    wormatesJoining = task.getResult().getDocuments();
                                    getWorkmatesJoining(wormatesJoining);
                                    sendVisualNotification(restaurant_name, restaurant_adress);
                                }
                            }
                        });
            }
        });
    }

    // GET THE LIST OF WORKMATES JOINING IN STRING
    private void getWorkmatesJoining(List<DocumentSnapshot> list){
        int nbWorkmatesJoining = list.size()-1;
        if (nbWorkmatesJoining > 0){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i<list.size();i++){
                if (!list.get(i).toObject(User.class).getUid().equals(FirebaseUtils.getCurrentUser().getUid())){
                    sb.append(list.get(i).toObject(User.class).getUsername()+"\n");
                }
            } answerWorkmatesJoining = getString(R.string.notification_workmates_joining) + "\n" + sb.toString();
        } else answerWorkmatesJoining = getString(R.string.notification_no_workmates);
    }
}