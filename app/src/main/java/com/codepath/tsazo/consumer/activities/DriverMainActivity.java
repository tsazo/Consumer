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
import com.codepath.tsazo.consumer.fragments.UserComposeFragment;
import com.codepath.tsazo.consumer.fragments.UserHomeFragment;
import com.codepath.tsazo.consumer.fragments.UserSettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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