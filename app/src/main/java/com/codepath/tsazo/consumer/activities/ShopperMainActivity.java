package com.codepath.tsazo.consumer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.fragments.ShopperComposeFragment;
import com.codepath.tsazo.consumer.fragments.ShopperHomeFragment;
import com.codepath.tsazo.consumer.fragments.ShopperSettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ShopperMainActivity extends AppCompatActivity {

    public static final String TAG = "ShopperMainActivity";
    private BottomNavigationView bottomNavigationViewUser;
    public final FragmentManager fragmentManager = getSupportFragmentManager();
    public static Fragment fragment;
    public static Activity activity;
    public static ShopperHomeFragment shopperHomeFragment;
    private static ShopperComposeFragment shopperComposeFragment;
    public static ShopperSettingsFragment shopperSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        activity = this;

        bottomNavigationViewUser = findViewById(R.id.bottom_navigation_user);

        // Connects fragments to tab bar
        setBottomNavigationView(bottomNavigationViewUser, fragmentManager);

        // Set default selection
        bottomNavigationViewUser.setSelectedItemId(R.id.action_home);
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
                                if(shopperHomeFragment == null)
                                    shopperHomeFragment = new ShopperHomeFragment();

                                fragment = shopperHomeFragment;
                                break;
                            case R.id.action_create:
                                if(shopperComposeFragment == null)
                                    shopperComposeFragment = new ShopperComposeFragment();

                                fragment = shopperComposeFragment;
                                break;
                            case R.id.action_profile:
                            default:
                                if(shopperSettingsFragment == null)
                                    shopperSettingsFragment = new ShopperSettingsFragment();

                                fragment = shopperSettingsFragment;
                                break;
                        }

                        fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
                        return true;
                    }
                });

    }
}