package com.codepath.tsazo.consumer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.fragments.DriverHomeFragment;
import com.codepath.tsazo.consumer.fragments.DriverOrderFragment;
import com.codepath.tsazo.consumer.fragments.DriverSettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class DriverMainActivity extends AppCompatActivity {

    public static final String TAG = "DriverMainActivity";
    private BottomNavigationView bottomNavigationViewDriver;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    public static Fragment fragment;
    public static DriverHomeFragment driverHomeFragment;
    public static DriverOrderFragment driverOrderFragment;
    private static DriverSettingsFragment driverSettingsFragment;

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
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                if(driverHomeFragment == null)
                                    driverHomeFragment = new DriverHomeFragment();

                                fragment = driverHomeFragment;
                                break;
                            case R.id.action_order:
                                if(driverOrderFragment == null)
                                    driverOrderFragment = new DriverOrderFragment();

                                fragment = driverOrderFragment;
                                break;
                            case R.id.action_profile:
                            default:
                                if(driverSettingsFragment == null)
                                    driverSettingsFragment = new DriverSettingsFragment();

                                fragment = driverSettingsFragment;
                                break;
                        }

                        fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
                        return true;
                    }
                });

    }
}