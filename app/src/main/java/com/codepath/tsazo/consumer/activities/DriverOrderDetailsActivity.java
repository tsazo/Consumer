package com.codepath.tsazo.consumer.activities;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;
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
    private String FIND_PLACE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=";
    private GoogleMap driverMap;
    private Location mCurrentLocation;
    public static JSONObject location;
    private String address;

    private final static String KEY_STORE_NAME = "storeName";
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
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
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
                ParseUser.getCurrentUser().put(KEY_HAS_ORDER, true);
                ParseUser.getCurrentUser().saveInBackground();
                Toast.makeText(DriverOrderDetailsActivity.this, "Accepted Order!", Toast.LENGTH_SHORT).show();
                DriverHomeFragment.adapter.notifyDataSetChanged();
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

        textViewPrice.setText("$" + order.getPrice());
        textViewStoreName.setText(order.getStore().getName());
        textViewLocation.setText(order.getStore().getAddress());
    }

    protected void loadMap(GoogleMap googleMap) {
        driverMap = googleMap;
        if (driverMap != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            DriverOrderDetailsActivityPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            //DriverOrderDetailsActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DriverOrderDetailsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getMyLocation() {
        // Access users current location
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.i(TAG, "Location: " + location.toString());
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


//    private void getLocationFromCoords(double x, double y) {
//        AsyncHttpClient client = new AsyncHttpClient();
//        String geoUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + x + "," + y + "&key=" + getString(R.string.google_maps_api_key);
//        Log.i(TAG, geoUrl);
//        client.get(geoUrl, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Headers headers, JSON json) {
//                Log.i(TAG, getString(R.string.google_maps_api_key));
//                Log.i(TAG, "Response" + " " + json.toString());
//                location = json.jsonObject;
//                try {
//                    address = ((JSONObject) location.getJSONArray("results").get(0)).getString("formatted_address");
//                    Log.i(TAG, address);
//                    //TODO: Assign address to store or user or driver
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
//
//            }
//        });
//    }
}