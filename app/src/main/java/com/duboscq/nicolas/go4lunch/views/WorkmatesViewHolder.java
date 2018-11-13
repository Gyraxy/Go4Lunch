package com.duboscq.nicolas.go4lunch.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder {

    private ImageView workmates_profile_imv;
    final private TextView workmates_answer_txt;
    private String answer;

    public WorkmatesViewHolder(View itemView) {
        super(itemView);
        workmates_profile_imv = itemView.findViewById(R.id.workmates_profile_imv);
        workmates_answer_txt = itemView.findViewById(R.id.workmates_answer_txt);
    }

    public void updateWorkmatesInfo(User user, RequestManager glide, final Context context,final String todayDate) {

        //Upload the image into ImageView
        glide.load(user.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(workmates_profile_imv);

        //Show TextView - Workmate has chosen or not
        final String workmate_name = user.getUsername();
        UserHelper.getUser(user.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User workmates = documentSnapshot.toObject(User.class);
                String workmates_lunch_date = workmates.getLunchDate();
                String workmates_lunch_name = workmates.getLunchName();
                if (!workmates_lunch_date.equals(todayDate)){
                    answer = workmate_name + context.getString(R.string.workmates_not_decided);
                    workmates_answer_txt.setText(answer);
                    workmates_answer_txt.setTypeface(null, Typeface.ITALIC);
                } else if (workmates_lunch_date.equals(todayDate)){
                    answer = workmate_name + context.getString(R.string.workmates_is_eating) + workmates_lunch_name + ".";
                    workmates_answer_txt.setText(answer);
                    workmates_answer_txt.setTypeface(null, Typeface.NORMAL);
                }
            }
        });
    }

    public void updateRestaurantChosenWorkmatesInfo(User user, RequestManager glide, final String workmates_joining) {

        //Upload the image into ImageView
        glide.load(user.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(workmates_profile_imv);

        UserHelper.getUser(user.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User workmates = documentSnapshot.toObject(User.class);
                String workmates_username = workmates.getUsername();
                String answer = workmates_username + workmates_joining;
                workmates_answer_txt.setText(answer);
            }
        });

    }
}
