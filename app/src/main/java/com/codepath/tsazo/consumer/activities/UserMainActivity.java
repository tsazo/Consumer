package com.codepath.tsazo.consumer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.fragments.UserComposeFragment;
import com.codepath.tsazo.consumer.fragments.UserHomeFragment;
import com.codepath.tsazo.consumer.fragments.UserSettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserMainActivity extends AppCompatActivity {

    public static final String TAG = "UserMainActivity";
    private BottomNavigationView bottomNavigationViewUser;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

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
                        Fragment fragment;
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                fragment = new UserHomeFragment();
                                break;
                            case R.id.action_create:
                                fragment = new UserComposeFragment();
                                break;
                            case R.id.action_profile:
                            default:
                                fragment = new UserSettingsFragment();
                                break;
                        }

                        fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
                        return true;
                    }
                });

    }
}