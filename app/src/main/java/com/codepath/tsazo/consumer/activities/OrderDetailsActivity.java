package com.codepath.tsazo.consumer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.User;
import com.codepath.tsazo.consumer.models.Order;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class OrderDetailsActivity extends AppCompatActivity {


    private static final String TAG = "OrderDetailsActivity";
    private String KEY_STORE_NAME = "storeName";
    private String KEY_STORE_ADDRESS = "address";
    private String KEY_NAME = "name";
    private String KEY_PHONE_NUMBER = "phoneNumber";

    // the Post to display
    private Order order;

    // the view objects
    private TextView textViewStoreName;
    private TextView textViewStoreAddress;
    private TextView textViewOrderNumber;
    private TextView textViewDriver;
    private Button buttonCallDriver;

    // live-tracking map
    private boolean hasActiveDriver;
    private GoogleMap trackingMap;
    private SupportMapFragment mapFragment;
    private DatabaseReference driverLocation;
    private static Marker marker;

    // Cal permission
    private static final int REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_details);

        textViewStoreName = findViewById(R.id.textViewStoreName);
        textViewStoreAddress = findViewById(R.id.textViewStoreAddress);
        textViewOrderNumber = findViewById(R.id.textViewPrice);
        textViewDriver = findViewById(R.id.textViewDriver);
        buttonCallDriver = findViewById(R.id.buttonCallDriver);

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        }

        // Unwrap the movie passed in via intent, using its simple name as a key
        order = (Order) Parcels.unwrap(getIntent().getParcelableExtra(Order.class.getSimpleName()));

//        if(order.getDriver() != null && !order.getIsDone()){
//            hasActiveDriver = true;
//            User.callPermission(OrderDetailsActivity.this, OrderDetailsActivity.this, REQUEST_CODE);
//        }

        //setValues();
    }

    @Override
    protected void onStart() {
        super.onStart();

        queryOrder();
    }

    protected void queryOrder() {
        // Specify which class to query
        ParseQuery<Order> query = ParseQuery.getQuery(Order.class);
        query.include(Order.KEY_DONE);
        query.include(Order.KEY_DRIVER);

        query.whereEqualTo(Order.KEY_DRIVER, order.getDriver());
        query.whereEqualTo(Order.KEY_DONE, !order.getIsDone());

        query.findInBackground(new FindCallback<Order>() {
            @Override
            public void done(List<Order> orders, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue with getting order", e);
                    return;
                }

                if(!orders.isEmpty()){
                    hasActiveDriver = true;
                    User.callPermission(OrderDetailsActivity.this, OrderDetailsActivity.this, REQUEST_CODE);
                }

                setValues();
            }
        });
    }

    // Method to set the values into the views
    private void setValues() {
        try {
            textViewStoreName.setText(order.getStore().fetchIfNeeded().getString(KEY_STORE_NAME));
            textViewStoreAddress.setText(order.getStore().fetchIfNeeded().getString(KEY_STORE_ADDRESS));
        } catch (Exception e){
            Log.e(TAG, "Cannot fetch store name or address", e);
        }

        if(hasActiveDriver){
            try {
                buttonCallDriver.setVisibility(View.VISIBLE);
                textViewDriver.setText(order.getDriver().fetchIfNeeded().getString(KEY_NAME));
                driverLocation = FirebaseDatabase.getInstance().getReference();

                callDriver();

                updateDriverLocation();

            } catch (Exception e){
                Log.e(TAG, "Cannot fetch driver name", e);
            }
        } else {
            textViewDriver.setText("No driver assigned to order");
            buttonCallDriver.setVisibility(View.GONE);
        }

        textViewOrderNumber.setText("Order #: " + order.getOrderNumber());
    }

    // Calls the driver
    private void callDriver() {
        buttonCallDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + order.getUser().getString(KEY_PHONE_NUMBER)));
                startActivity(intent);
            }
        });
    }

    // Updates the driver location when they are within the app
    private void updateDriverLocation() {
        driverLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Double lat = dataSnapshot.child("latitude").getValue(Double.class);
                Double lng = dataSnapshot.child("longitude").getValue(Double.class);
                Log.d(TAG, "Value is: " + lat + "," + lng);

                if(marker != null){
                    marker.remove();
                }

                marker = trackingMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(order.getDriver().getString(KEY_NAME)));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // Loads the Google map for the user to see the store and driver location.
    protected void loadMap(GoogleMap googleMap) {
        trackingMap = googleMap;
        if (trackingMap != null) {
            // Map is ready
            Log.i(TAG,"Map Fragment was loaded properly!");

            trackingMap.addMarker(new MarkerOptions()
                    .position(new LatLng(order.getStore().getLocation().getLatitude(), order.getStore().getLocation().getLongitude()))
                    .title(order.getStore().getName()));

        }
    }
}