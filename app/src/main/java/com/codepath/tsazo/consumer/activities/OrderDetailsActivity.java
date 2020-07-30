package com.codepath.tsazo.consumer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.User;
import com.codepath.tsazo.consumer.models.Order;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseException;
import com.parse.ParseFile;

import org.parceler.Parcels;

import kotlin.collections.MapsKt;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class OrderDetailsActivity extends AppCompatActivity {


    private static final String TAG = "OrderDetailsActivity";
    private String KEY_STORE_NAME = "storeName";
    private String KEY_STORE_ADDRESS = "address";
    private String KEY_NAME = "name";

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
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
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

        if(order.getDriver() != null && !order.getIsDone()){
            hasActiveDriver = true;
            User.callPermission(OrderDetailsActivity.this, OrderDetailsActivity.this, REQUEST_CODE);
        }

        setValues();
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
                textViewDriver.setVisibility(View.VISIBLE);
                buttonCallDriver.setVisibility(View.VISIBLE);
                //mapFragment.setVisibility(View.VISIBLE);
                textViewDriver.setText(order.getDriver().fetchIfNeeded().getString(KEY_NAME));
                driverLocation = FirebaseDatabase.getInstance().getReference();

                updateDriverLocation();

            } catch (Exception e){
                Log.e(TAG, "Cannot fetch driver name", e);
            }
        } else {
            textViewDriver.setVisibility(View.GONE);
            buttonCallDriver.setVisibility(View.GONE);
        }

        textViewOrderNumber.setText("Order #: " + order.getOrderNumber());
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