package com.codepath.tsazo.consumer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.tsazo.consumer.adapters.OrdersAdapter;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.models.Order;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserHomeFragment extends Fragment {

    public static final String TAG = "UserHomeFragment";
    private RecyclerView recyclerViewOrders;
    protected OrdersAdapter adapter;
    protected List<Order> allOrders;

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
        // Need to use view.findViewById as Fragment class doesn't extend View, but rather fragment
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        allOrders = new ArrayList<>();
        adapter = new OrdersAdapter(getContext(), allOrders);


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
            }
        });
    }
}