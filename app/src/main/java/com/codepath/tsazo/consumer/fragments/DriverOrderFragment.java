package com.codepath.tsazo.consumer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.DriverMainActivity;
import com.codepath.tsazo.consumer.activities.UserMainActivity;
import com.codepath.tsazo.consumer.models.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverOrderFragment extends Fragment {

    public static final String TAG = "DriverOrderFragment";
    private ParseUser currentUser;
    private TextView textViewOrderHeader;
    private TextView textViewStoreName;
    private TextView textViewStoreAddress;
    private TextView textViewOrderNumber;
    private Button buttonNavigateStore;
    private TextView textViewUserName;
    private TextView textViewUserAddress;
    private Button buttonCallUser;
    private Button buttonNavigateUser;
    private Button buttonPicture;
    private Button buttonCompleteOrder;
    private boolean hasActiveOrder;

    private Order order;

    // Bottom Navigation fields
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationViewDriver;

    private final static String KEY_HAS_ORDER = "hasOrder";
    private final static String KEY_EARNINGS = "earnings";

    public DriverOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_order, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup any handles to view objects here
        fragmentManager = getActivity().getSupportFragmentManager();
        bottomNavigationViewDriver = getActivity().findViewById(R.id.bottom_navigation_driver);

        textViewOrderHeader = view.findViewById(R.id.textViewOrderHeader);
        textViewStoreName = view.findViewById(R.id.textViewStoreName);
        textViewStoreAddress = view.findViewById(R.id.textViewStoreAddress);
        textViewOrderNumber = view.findViewById(R.id.textViewOrderNumber);
        buttonNavigateStore = view.findViewById(R.id.buttonNavigateStore);
        textViewUserName = view.findViewById(R.id.textViewUserName);
        textViewUserAddress = view.findViewById(R.id.textViewUserAddress);
        buttonCallUser = view.findViewById(R.id.buttonCallUser);
        buttonNavigateUser = view.findViewById(R.id.buttonNavigateUser);
        buttonPicture = view.findViewById(R.id.buttonPicture);
        buttonCompleteOrder = view.findViewById(R.id.buttonCompleteOrder);

        // Gets the person who's logged in
        currentUser = ParseUser.getCurrentUser();

        hasActiveOrder = currentUser.getBoolean(KEY_HAS_ORDER);

        if(hasActiveOrder){
            setValues(view);

            // Navigate to store
            navigateStore();

            // Navigate to user
            navigateUser();

            // On complete-order button press
            completeOrder();
        } else {
            noOrderValues();
        }
    }

    private void noOrderValues() {
        textViewOrderHeader.setText("No Current Order");
        textViewStoreName.setVisibility(View.GONE);
        textViewStoreAddress.setVisibility(View.GONE);
        textViewOrderNumber.setVisibility(View.GONE);
        buttonNavigateStore.setVisibility(View.GONE);
        textViewUserName.setVisibility(View.GONE);
        textViewUserAddress.setVisibility(View.GONE);
        buttonCallUser.setVisibility(View.GONE);
        buttonNavigateUser.setVisibility(View.GONE);
        buttonPicture.setVisibility(View.GONE);
        buttonCompleteOrder.setVisibility(View.GONE);
    }

    // Sets all the values for the current order if there is one.
    private void setValues(View view) {
        textViewOrderHeader.setText("Current Order");
        textViewStoreName.setVisibility(View.VISIBLE);
        textViewStoreAddress.setVisibility(View.VISIBLE);
        textViewOrderNumber.setVisibility(View.VISIBLE);
        buttonNavigateStore.setVisibility(View.VISIBLE);
        textViewUserName.setVisibility(View.VISIBLE);
        textViewUserAddress.setVisibility(View.VISIBLE);
        buttonCallUser.setVisibility(View.VISIBLE);
        buttonNavigateUser.setVisibility(View.VISIBLE);
        buttonPicture.setVisibility(View.VISIBLE);
        buttonCompleteOrder.setVisibility(View.VISIBLE);

        // Specify which class to query
        final ParseQuery<Order> query = ParseQuery.getQuery(Order.class);
        query.include(Order.KEY_USER);
        query.include(Order.KEY_STORE);
        query.include(Order.KEY_DRIVER);

        query.whereEqualTo(Order.KEY_DRIVER, currentUser);
        query.whereEqualTo(Order.KEY_DONE, false);

        query.findInBackground(new FindCallback<Order>() {
            @Override
            public void done(List<Order> orders, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue with getting orders", e);
                    return;
                }
                order = orders.get(0);

                textViewStoreName.setText(order.getStore().getName());
                textViewStoreAddress.setText(order.getStore().getAddress());
                textViewOrderNumber.setText(order.getOrderNumber());
                textViewUserName.setText(order.getUser().getString("name"));
                textViewUserAddress.setText(order.getDeliveryAddress());
            }
        });
    }

    // Button to start intent for Google Maps to navigate to the store
    private void navigateStore() {
        buttonNavigateStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+textViewStoreAddress.getText()+"&mode=d");
                Log.i(TAG, ""+ gmmIntentUri);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }

    private void navigateUser() {
        buttonNavigateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+textViewUserAddress.getText()+"&mode=d");
                Log.i(TAG, ""+ gmmIntentUri);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }

    // Updates the driver's boolean hasOrder as well as updates order boolean value isDone
    // TODO: Add earnings value for driver
    private void completeOrder() {
        buttonCompleteOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: DO a check to see if Driver is at/near delivery address before completing order
                currentUser.put(KEY_HAS_ORDER, false);
                Number totalEarnings = (float) currentUser.getNumber(KEY_EARNINGS) + (float) order.getPrice();
                currentUser.put(KEY_EARNINGS, totalEarnings);
                order.setIsDone(true);

                currentUser.saveInBackground();
                order.saveInBackground();

                Toast.makeText(getContext(), "Completed order! Thank you.", Toast.LENGTH_SHORT).show();

                // Goes to home fragment
                DriverMainActivity.fragment = DriverMainActivity.driverHomeFragment;
                bottomNavigationViewDriver.setSelectedItemId(R.id.action_home);
                fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, DriverMainActivity.fragment).commit();
            }
        });
    }

}