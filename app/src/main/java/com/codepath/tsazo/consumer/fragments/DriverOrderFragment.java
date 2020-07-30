package com.codepath.tsazo.consumer.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.codepath.tsazo.consumer.TrackingService;
import com.codepath.tsazo.consumer.activities.DriverMainActivity;
import com.codepath.tsazo.consumer.activities.UserMainActivity;
import com.codepath.tsazo.consumer.models.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions
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
    private static DecimalFormat decimalFormat = new DecimalFormat("0.00");

    // Bottom Navigation fields
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationViewDriver;

    // GPS Firebase
    private static final int PERMISSIONS_REQUEST = 100;
    private static final int REQUEST_CODE = 123;

    private final static String KEY_PHONE_NUMBER = "phoneNumber";
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

            // Call user
            callUserPermission();

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

        // Check whether GPS tracking is enabled
        enableTracking();

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
                textViewOrderNumber.setText("Order #: " + order.getOrderNumber());
                textViewUserName.setText(order.getUser().getString("name"));
                textViewUserAddress.setText(order.getDeliveryAddress());
            }
        });
    }

    // Enable tracking such that the user is able to see the driver's location
    private void enableTracking() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        //Check whether this app has access to the location permission//

        int permission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the location permission has been granted, then start the TrackerService

        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {

            //If the app doesn’t currently have access to the user’s location, then request access
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    //Start the TrackerService
    private void startTrackerService() {
        // Write a message to the database
        Intent intent = new Intent(getContext(), TrackingService.class);
        getActivity().startService(intent);

        Toast.makeText(getContext(), "GPS tracking enabled", Toast.LENGTH_SHORT).show();
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

    // Location tracking permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DriverOrderFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.CALL_PHONE})
    private void callUserPermission() {
        buttonCallUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED){
                    // when permission is not granted
                    if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)){
                        // Create AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Grant phone call permission");
                        builder.setMessage("Call User");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE},REQUEST_CODE);

                                callUser();
                            }
                        });

                        builder.setNegativeButton("Cancel", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE},REQUEST_CODE);
                    }
                } else {
                    callUser();
                }
            }
        });
    }

    private void callUser(){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + order.getUser().getString(KEY_PHONE_NUMBER)));
        startActivity(intent);
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
    private void completeOrder() {
        buttonCompleteOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser.put(KEY_HAS_ORDER, false);
                String totalEarnings = "" + (Double.valueOf(decimalFormat.format(currentUser.getNumber(KEY_EARNINGS))) + Double.valueOf(decimalFormat.format(order.getPrice())));
                Log.i(TAG, totalEarnings);
                currentUser.put(KEY_EARNINGS, Double.valueOf(totalEarnings));
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