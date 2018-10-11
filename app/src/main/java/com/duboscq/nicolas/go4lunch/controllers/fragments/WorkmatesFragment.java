package com.duboscq.nicolas.go4lunch.controllers.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.WorkmatesRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesFragment extends Fragment implements WorkmatesRecyclerViewAdapter.Listener{

    // FOR DESIGN
    @BindView(R.id.fragment_workmates_recycler_view) RecyclerView recyclerView;

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

        this.workmatesRecyclerViewAdapter = new WorkmatesRecyclerViewAdapter(generateOptionsForAdapter(UserHelper.getAllWorkmates()), Glide.with(this), this);
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

    @Override
    public void onDataChanged() {

    }
}
