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
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.controllers.activities.MainActivity;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class NotificationsService extends FirebaseMessagingService {

    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "GO4LUNCH", NETWORK = "NETWORK";
    String restaurant_adress, restaurant_name, chosenrestaurantId;
    private Disposable disposable;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("NETWORK","On Message Firebase Notification received");
        if (remoteMessage.getNotification() != null) {
            Log.i("NETWORK","Receive notification from Firebase");
            String message = remoteMessage.getNotification().getBody();
            getUserRestaurantId();
        }
    }

    private void sendVisualNotification(String restaurant_name, String restaurant_adress) {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Go4Lunch");
        inboxStyle.addLine("Your Lunch: "+restaurant_name);
        inboxStyle.addLine(restaurant_adress);

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

    private void getUserRestaurantId() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                User currentUser = documentSnapshot.toObject(User.class);
                chosenrestaurantId = currentUser.getLunch();
                if (!chosenrestaurantId.equals("XXX")){
                    getRestaurantDetail(chosenrestaurantId);
                }
            }
        });
    }

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
                sendVisualNotification(restaurant_name,restaurant_adress);
            }
        });
    }

    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }
}