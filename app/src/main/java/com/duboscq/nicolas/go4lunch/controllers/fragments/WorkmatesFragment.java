package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.WorkmatesRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.utils.ItemClickSupport;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesFragment extends Fragment implements WorkmatesRecyclerViewAdapter.Listener{

    // FOR DESIGN
    @BindView(R.id.fragment_workmates_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_workmates_recycler_view_empty) TextView textViewRecyclerViewEmpty;

    // FOR DATA
    WorkmatesRecyclerViewAdapter workmatesRecyclerViewAdapter;

    public WorkmatesFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this, view);
        configureRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    // --------------------
    // UI
    // --------------------

    //RECYCLERVIEW CONFIGURATION
    private void configureRecyclerView(){

        this.workmatesRecyclerViewAdapter = new WorkmatesRecyclerViewAdapter(generateOptionsForAdapter(UserHelper.getAllWorkmates()), Glide.with(this), this, this.getCurrentUser().getUid(),getContext());
        workmatesRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(workmatesRecyclerViewAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(this.workmatesRecyclerViewAdapter);
    }

    // 6 - Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_restaurant)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent i = new Intent(getActivity(), RestaurantActivity.class);
                        startActivity(i);
                    }
                });
    }

    // --------------------
    // CALLBACK
    // --------------------

    @Override
    public void onDataChanged() {
        textViewRecyclerViewEmpty.setVisibility(this.workmatesRecyclerViewAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }
}
