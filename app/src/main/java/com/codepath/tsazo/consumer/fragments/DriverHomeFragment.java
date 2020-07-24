package com.codepath.tsazo.consumer.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcel;
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
import com.codepath.tsazo.consumer.adapters.DriverOrdersAdapter;
import com.codepath.tsazo.consumer.models.Order;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * A simple {@link Fragment} subclass.
 */

@RuntimePermissions
public class DriverHomeFragment extends Fragment {

    public static final String TAG = "DriverHomeFragment";
    private RecyclerView recyclerViewDriverOrders;
    private ProgressBar progressBar;
    private TextView textViewOrdersHeader;
    private Button buttonOrder;
    public static DriverOrdersAdapter adapter;
    protected List<Order> allOrders;

    // Bottom Navigation fields
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationViewDriver;

    //Google Maps fields
//    private static final int REQUEST_LOCATION = 1;
    private ParseUser currentUser;
    private DriverHomeFragment fragment;
    private static Location mCurrentLocation;

    private final static String KEY_LOCATION = "location";
    private final static String KEY_HAS_ORDER = "hasOrder";

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
        // on some click or some loading we need to wait for...
        progressBar= view.findViewById(R.id.pbLoading);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        fragmentManager = getActivity().getSupportFragmentManager();
        bottomNavigationViewDriver = getActivity().findViewById(R.id.bottom_navigation_driver);

        recyclerViewDriverOrders = view.findViewById(R.id.recyclerViewDriverOrders);
        textViewOrdersHeader = view.findViewById(R.id.textViewOrdersHeader);
        buttonOrder = view.findViewById(R.id.buttonOrder);
        currentUser = ParseUser.getCurrentUser();
        fragment = this;

        DriverHomeFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(fragment);

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


        if(currentUser.getBoolean(KEY_HAS_ORDER)){
            Toast.makeText(getContext(), "Go to active order", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Reload available orders each time activity is loaded
    @Override
    public void onStart(){
        super.onStart();
        adapter.clear();

        if(!currentUser.getBoolean(KEY_HAS_ORDER))
            queryOrders();
        else {
            textViewOrdersHeader.setText("You Have an Active Order");
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    // Goes to order fragment
    private void goOrderFragment() {
        if(DriverMainActivity.driverOrderFragment == null)
            DriverMainActivity.fragment = new DriverOrderFragment();
        else
            DriverMainActivity.fragment = DriverMainActivity.driverOrderFragment;
        bottomNavigationViewDriver.setSelectedItemId(R.id.action_order);
        fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, DriverMainActivity.fragment).commit();
    }

    // Get all orders within 10 miles of driver
    // TODO: connect driver to geopoint/location
    protected void queryOrders() {
        Log.i(TAG, "Checking if location is null @query orders");

        // Specify which class to query
        ParseQuery<Order> query = ParseQuery.getQuery(Order.class);
        query.include(Order.KEY_USER);
        query.include(Order.KEY_STORE);
        query.include(Order.KEY_DRIVER);
        query.include(Order.KEY_DONE);

        query.whereEqualTo(Order.KEY_DRIVER, null);
        query.whereEqualTo(Order.KEY_DONE, false);

        query.findInBackground(new FindCallback<Order>() {
            @Override
            public void done(List<Order> orders, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue with getting orders", e);
                    return;
                }

                List<Order> closeOrders = new ArrayList<>();

                for(Order order: orders) {
                    Log.i(TAG, "Order: " + order.getOrderNumber() + ", user: " + order.getUser().getUsername());

                    Double orderLat = order.getStore().getLocation().getLatitude();
                    Double orderLong = order.getStore().getLocation().getLongitude();
                    Double userLat = currentUser.getParseGeoPoint(KEY_LOCATION).getLatitude();
                    Double userLong = currentUser.getParseGeoPoint(KEY_LOCATION).getLongitude();

                    if(calculateDistance(orderLat, orderLong, userLat, userLong) < 10){
                        closeOrders.add(order);
                    }

                }

                allOrders.addAll(closeOrders);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    // Calculate distances between two coordinate points in miles
    private double calculateDistance(double lat1, double long1, double lat2, double long2) {
        if ((lat1 == lat2) && (long1 == long2)) {
            return 0;
        }
        double theta = long1 - long2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;

        return (dist);
    }

    // Google Maps, retrieve location
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DriverHomeFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getMyLocation() {
        // Access users current location
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.i(TAG, "Location: " + location.toString());
                            mCurrentLocation = location;
                            ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

                            currentUser.put(KEY_LOCATION, geoPoint);
                            currentUser.saveInBackground();
                            //getLocationFromCoords(location.getLatitude(), location.getLongitude());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }
}