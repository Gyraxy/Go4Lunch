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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.duboscq.nicolas.go4lunch.R;
import com.duboscq.nicolas.go4lunch.adapters.WorkmatesRecyclerViewAdapter;
import com.duboscq.nicolas.go4lunch.api.UserHelper;
import com.duboscq.nicolas.go4lunch.controllers.activities.RestaurantActivity;
import com.duboscq.nicolas.go4lunch.models.firebase.User;
import com.duboscq.nicolas.go4lunch.utils.DateUtility;
import com.duboscq.nicolas.go4lunch.utils.DividerItemDecoration;
import com.duboscq.nicolas.go4lunch.utils.FirebaseUtils;
import com.duboscq.nicolas.go4lunch.utils.ItemClickSupport;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesFragment extends Fragment implements WorkmatesRecyclerViewAdapter.Listener{

    // FOR DESIGN
    @BindView(R.id.fragment_workmates_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_workmates_recycler_view_empty) TextView textViewRecyclerViewEmpty;

    // FOR DATA
    WorkmatesRecyclerViewAdapter workmatesRecyclerViewAdapter;
    String todayDate;

    public WorkmatesFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        todayDate = DateUtility.getDateTime();
        ButterKnife.bind(this, view);
        configureRecyclerView();
        configureOnClickRecyclerView();
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

        this.workmatesRecyclerViewAdapter = new WorkmatesRecyclerViewAdapter(generateOptionsForAdapter(UserHelper.getAllWorkmates()), Glide.with(this), this, FirebaseUtils.getCurrentUser().getUid(),getContext(), todayDate);
        workmatesRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(workmatesRecyclerViewAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), R.drawable.horizontal_divider);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(mDividerItemDecoration);
        recyclerView.setAdapter(this.workmatesRecyclerViewAdapter);
    }

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
                        String workmates_choice = workmatesRecyclerViewAdapter.getItem(position).getLunchDate();
                        if (workmates_choice.equals(todayDate)){
                            Intent i = new Intent(getActivity(),RestaurantActivity.class);
                            i.putExtra("restaurant_id",workmatesRecyclerViewAdapter.getItem(position).getLunchId());
                            i.putExtra("restaurant_image_url",workmatesRecyclerViewAdapter.getItem(position).getLunchUrl());
                            startActivity(i);
                        } else {
                            Toast.makeText(getContext(),workmatesRecyclerViewAdapter.getItem(position).getUsername()+getString(R.string.workmates_not_decided),Toast.LENGTH_SHORT).show();
                        }
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
}
