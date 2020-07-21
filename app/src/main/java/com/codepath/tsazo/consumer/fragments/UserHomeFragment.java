package com.codepath.tsazo.consumer.fragments;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.tsazo.consumer.adapters.OrdersAdapter;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.models.Order;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * A simple {@link Fragment} subclass.
 */

public class UserHomeFragment extends Fragment {

    public static final String TAG = "UserHomeFragment";
    private ProgressBar progressBar;
    private RecyclerView recyclerViewOrders;
    private TextView textViewUserAddress;
    protected OrdersAdapter adapter;
    protected List<Order> allOrders;
    private final String KEY_ADDRESS = "address";

    private ParseUser currentUser;
    private UserHomeFragment fragment;

    public UserHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_home, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup any handles to view objects here
        // on some click or some loading we need to wait for...
        progressBar= view.findViewById(R.id.pbLoading);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Need to use view.findViewById as Fragment class doesn't extend View, but rather fragment
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        textViewUserAddress = view.findViewById(R.id.textViewUserAddress);
        allOrders = new ArrayList<>();
        adapter = new OrdersAdapter(getContext(), allOrders);

        currentUser = ParseUser.getCurrentUser();
        fragment = this;

        if(currentUser.getString(KEY_ADDRESS) != null)
            textViewUserAddress.setText(currentUser.getString(KEY_ADDRESS));

        // RecyclerView setup: layout manager and the adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewOrders.setLayoutManager(layoutManager);
        recyclerViewOrders.setAdapter(adapter);

        queryOrders();
    }

    protected void queryOrders() {
        // Specify which class to query
        ParseQuery<Order> query = ParseQuery.getQuery(Order.class);
        query.include(Order.KEY_USER);
        query.include(Order.KEY_STORE);
        query.include(Order.KEY_DRIVER);

        query.whereEqualTo(Order.KEY_USER, ParseUser.getCurrentUser());

        query.addDescendingOrder(Order.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Order>() {
            @Override
            public void done(List<Order> orders, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue with getting orders", e);
                    return;
                }

                for(Order order: orders) {
                    Log.i(TAG, "Order: " + order.getOrderNumber() + ", user: " + order.getUser().getUsername());
                }

                allOrders.addAll(orders);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }
}