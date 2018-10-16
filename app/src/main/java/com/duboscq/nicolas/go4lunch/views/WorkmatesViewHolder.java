package com.duboscq.nicolas.go4lunch.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.firebase.User;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder {

    private ImageView workmates_profile_imv;
    private TextView workmates_answer_txt;

    public WorkmatesViewHolder(View itemView) {
        super(itemView);
        workmates_profile_imv = itemView.findViewById(R.id.workmates_profile_imv);
        workmates_answer_txt = itemView.findViewById(R.id.workmates_answer_txt);
    }

    public void updateWorkmatesInfo(User user, RequestManager glide) {
        glide.load(user.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(workmates_profile_imv);
        String answer = user.getUsername()+" is eating";
        workmates_answer_txt.setText(answer);
    }
}
