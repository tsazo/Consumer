package com.codepath.tsazo.consumer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.fragments.DriverHomeFragment;
import com.codepath.tsazo.consumer.fragments.DriverOrderFragment;
import com.codepath.tsazo.consumer.fragments.DriverSettingsFragment;
import com.codepath.tsazo.consumer.fragments.UserComposeFragment;
import com.codepath.tsazo.consumer.fragments.UserHomeFragment;
import com.codepath.tsazo.consumer.fragments.UserSettingsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.json.JSONObject;
import org.parceler.Parcels;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class DriverMainActivity extends AppCompatActivity {

    public static final String TAG = "DriverMainActivity";
    private BottomNavigationView bottomNavigationViewDriver;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);


        bottomNavigationViewDriver = findViewById(R.id.bottom_navigation_driver);

        // Connects fragments to tab bar
        setBottomNavigationView(bottomNavigationViewDriver, fragmentManager);

        // Set default selection
        bottomNavigationViewDriver.setSelectedItemId(R.id.action_home);
    }

    public static void setBottomNavigationView(BottomNavigationView bottomNavigationView,
                                               final FragmentManager fragmentManager){
        // handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment;
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                fragment = new DriverHomeFragment();
                                break;
                            case R.id.action_order:
                                fragment = new DriverOrderFragment();
                                break;
                            case R.id.action_profile:
                            default:
                                fragment = new DriverSettingsFragment();
                                break;
                        }

                        fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
                        return true;
                    }
                });

    }
}