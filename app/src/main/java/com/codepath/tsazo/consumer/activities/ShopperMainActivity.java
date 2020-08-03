package com.codepath.tsazo.consumer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.fragments.ShopperComposeFragment;
import com.codepath.tsazo.consumer.fragments.ShopperHomeFragment;
import com.codepath.tsazo.consumer.fragments.ShopperSettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import me.ibrahimsn.lib.SmoothBottomBar;

import static androidx.navigation.Navigation.findNavController;
import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;

public class ShopperMainActivity extends AppCompatActivity {

    public static final String TAG = "ShopperMainActivity";
    private BottomNavigationView bottomNavigationViewUser;
    public final FragmentManager fragmentManager = getSupportFragmentManager();
    public static Fragment fragment;
    public static Activity activity;
    public static ShopperHomeFragment shopperHomeFragment;
    private static ShopperComposeFragment shopperComposeFragment;
    public static ShopperSettingsFragment shopperSettingsFragment;
    private SmoothBottomBar smoothBottomBar;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopper_main);
        activity = this;

        bottomNavigationViewUser = findViewById(R.id.bottom_navigation_user);

        // Connects fragments to tab bar
        setBottomNavigationView(bottomNavigationViewUser, fragmentManager);

        // Set default selection
        bottomNavigationViewUser.setSelectedItemId(R.id.action_home);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_smooth_nav_shopper, menu);
//        //smoothBottomBar.setupWithNavController(menu, navController);
//        return true;
//    }

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