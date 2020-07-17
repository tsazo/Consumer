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
@RuntimePermissions
public class UserHomeFragment extends Fragment {

    public static final String TAG = "UserHomeFragment";
    private RecyclerView recyclerViewOrders;
    private TextView textViewUserAddress;
    protected OrdersAdapter adapter;
    protected List<Order> allOrders;
    private final String KEY_ADDRESS = "address";

    //Google Maps fields
    private ParseUser currentUser;
    private UserHomeFragment fragment;
    private Location mCurrentLocation;
    //private Location location;
    private final static String KEY_LOCATION = "location";

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
        textViewUserAddress = view.findViewById(R.id.textViewUserAddress);
        allOrders = new ArrayList<>();
        adapter = new OrdersAdapter(getContext(), allOrders);

        currentUser = ParseUser.getCurrentUser();
        fragment = this;

        UserHomeFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(fragment);

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
            }
        });
    }

    // Google Maps, retrieve location
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UserHomeFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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