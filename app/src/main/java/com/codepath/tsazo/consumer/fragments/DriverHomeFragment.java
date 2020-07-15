package com.codepath.tsazo.consumer.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.DriverMainActivity;
import com.codepath.tsazo.consumer.adapters.DriverOrdersAdapter;
import com.codepath.tsazo.consumer.models.Order;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverHomeFragment extends Fragment {

    public static final String TAG = "DriverHomeFragment";
    private RecyclerView recyclerViewDriverOrders;
    private Button buttonOrder;
    protected DriverOrdersAdapter adapter;
    protected List<Order> allOrders;

    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationViewDriver;

    public DriverHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_home, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup any handles to view objects here
        // Need to use view.findViewById as Fragment class doesn't extend View, but rather fragment
        fragmentManager = getActivity().getSupportFragmentManager();
        bottomNavigationViewDriver = getActivity().findViewById(R.id.bottom_navigation_driver);

        recyclerViewDriverOrders = view.findViewById(R.id.recyclerViewDriverOrders);
        buttonOrder = view.findViewById(R.id.buttonOrder);
        allOrders = new ArrayList<>();
        adapter = new DriverOrdersAdapter(getContext(), allOrders);

        // RecyclerView setup: layout manager and the adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewDriverOrders.setLayoutManager(layoutManager);
        recyclerViewDriverOrders.setAdapter(adapter);

        buttonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOrderFragment();
            }
        });

        queryOrders();
    }

    // Goes to order fragment
    private void goOrderFragment() {
        Fragment fragment = new DriverOrderFragment();
        bottomNavigationViewDriver.setSelectedItemId(R.id.action_order);
        fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
    }

    protected void queryOrders() {
        // Specify which class to query
        ParseQuery<Order> query = ParseQuery.getQuery(Order.class);
        query.include(Order.KEY_USER);
        query.include(Order.KEY_STORE);
        query.include(Order.KEY_DRIVER);

        query.whereEqualTo(Order.KEY_DRIVER, null);

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