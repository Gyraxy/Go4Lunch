package com.duboscq.nicolas.go4lunch.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.views.WorkmatesViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RestaurantWorkmatesRecyclerViewAdapter extends FirestoreRecyclerAdapter<User, WorkmatesViewHolder> {

    //FOR DATA
    private final RequestManager glide;
    private final String answer,idCurrentUser;

    //FOR COMMUNICATION
    private RestaurantWorkmatesRecyclerViewAdapter.Listener callback;

    public RestaurantWorkmatesRecyclerViewAdapter(@NonNull FirestoreRecyclerOptions<User> options, RequestManager glide, RestaurantWorkmatesRecyclerViewAdapter.Listener callback, String idCurrentUser, String answer) {
        super(options);
        this.glide = glide;
        this.callback = callback;
        this.idCurrentUser = idCurrentUser;
        this.answer = answer;
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmatesViewHolder holder, int position, @NonNull User model) {
        holder.updateRestaurantChosenWorkmatesInfo(model,this.glide, this.answer);
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
