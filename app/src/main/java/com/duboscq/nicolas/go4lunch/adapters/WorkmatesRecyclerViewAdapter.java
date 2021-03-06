package com.duboscq.nicolas.go4lunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.views.WorkmatesViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class WorkmatesRecyclerViewAdapter extends FirestoreRecyclerAdapter<User, WorkmatesViewHolder> {

    //FOR DATA
    private final RequestManager glide;
    private String idCurrentUser, todayDate;
    private Context context;

    //FOR COMMUNICATION
    private Listener callback;

    public WorkmatesRecyclerViewAdapter(@NonNull FirestoreRecyclerOptions<User> options, RequestManager glide, Listener callback, String idCurrentUser, Context context, String todayDate) {
        super(options);
        this.glide = glide;
        this.callback = callback;
        this.idCurrentUser = idCurrentUser;
        this.context = context;
        this.todayDate = todayDate;
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmatesViewHolder holder, int position, @NonNull User model) {
        holder.updateWorkmatesInfo(model,this.glide, this.context, this.todayDate);
    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkmatesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.workmates_list_view, parent, false));
    }

    public interface Listener {
        void onDataChanged();
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }
}
