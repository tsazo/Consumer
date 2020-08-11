package com.codepath.tsazo.consumer.activities;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.fragments.DriverHomeFragment;
import com.codepath.tsazo.consumer.models.Order;
import com.codepath.tsazo.consumer.models.ParseStore;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseUser;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.DecimalFormat;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class DriverOrderDetailsActivity extends AppCompatActivity {

    private static final String TAG = "DriverOrderDetails";

    private TextView textViewPrice;
    private TextView textViewStoreName;
    private TextView textViewLocation;
    private Button buttonAcceptOrder;
    private Order order;
    private SupportMapFragment mapFragment;
    private GoogleMap driverMap;
    private Location mCurrentLocation;
    public static JSONObject location;

    private static DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private final static String KEY_LOCATION = "location";
    private final static String KEY_HAS_ORDER = "hasOrder";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_order_details);

        setValues();

        // Set map values
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driverMap));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        }

        acceptOrder();
    }

    // Accepts the order for the driver to complete
    private void acceptOrder() {
        buttonAcceptOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order.setDriver(ParseUser.getCurrentUser());
                order.saveInBackground();
                Log.i(TAG, "driver!!: " + order.getDriver().getUsername());
                ParseUser.getCurrentUser().put(KEY_HAS_ORDER, true);
                ParseUser.getCurrentUser().saveInBackground();

                finish();
            }
        });
    }

    // Set values for the textViews and buttons
    private void setValues() {
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewStoreName = findViewById(R.id.textViewStoreName);
        textViewLocation = findViewById(R.id.textViewLocation);
        buttonAcceptOrder = findViewById(R.id.buttonAcceptOrder);

        order = Parcels.unwrap(getIntent().getParcelableExtra(Order.class.getSimpleName()));

        textViewPrice.setText("$" + decimalFormat.format(order.getPrice()));
        textViewStoreName.setText("Store: "+ order.getStore().getName());
        textViewLocation.setText("Address: " + order.getStore().getAddress());
    }

    protected void loadMap(GoogleMap googleMap) {
        driverMap = googleMap;
        if (driverMap != null) {
            // Map is ready

            Marker marker = driverMap.addMarker(new MarkerOptions()
                    .position(new LatLng(order.getStore().getLocation().getLatitude(), order.getStore().getLocation().getLongitude()))
                    .title(order.getStore().getName()));

            dropPinEffect(marker);

            DriverOrderDetailsActivityPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
        }
    }

    // Animation for marker to look as if it drops a pin
    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DriverOrderDetailsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getMyLocation() {
        driverMap.setMyLocationEnabled(true);
        driverMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Access users current location
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.i(TAG, "Location: " + location.toString());
                            mCurrentLocation = location;
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