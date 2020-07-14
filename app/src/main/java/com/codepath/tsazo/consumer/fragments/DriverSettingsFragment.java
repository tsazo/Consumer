package com.codepath.tsazo.consumer.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.DriverMainActivity;
import com.codepath.tsazo.consumer.activities.LoginActivity;
import com.codepath.tsazo.consumer.activities.SignupActivity;
import com.codepath.tsazo.consumer.activities.UserMainActivity;
import com.codepath.tsazo.consumer.models.Order;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverSettingsFragment extends Fragment {

    public static final String TAG = "DriverSettingsFragment";
    private EditText editTextName;
    private EditText editTextEmail;
    private ImageView imageViewProfile;
    private ParseUser currentUser;
    private Button buttonChangeProfile;
    private Button buttonUser;
    private Button buttonLogout;

    private static final String KEY_PROFILE_PIC = "profilePicture";
    private static final String KEY_NAME = "name";
    private static final String KEY_IS_DRIVER = "isDriver";

    public DriverSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_settings, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup any handles to view objects here
        // Need to use view.findViewById as Fragment class doesn't extend View, but rather fragment
        buttonLogout = view.findViewById(R.id.buttonLogout);
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        buttonChangeProfile = view.findViewById(R.id.buttonChangeProfile);
        buttonUser = view.findViewById(R.id.buttonUser);

        // Gets the person who's logged in
        currentUser = ParseUser.getCurrentUser();

        // Set values
        setValues();

        // Update profile information
        updateProfile();

        // Switch to user
        switchUser();

        // Logout button listener
        logout();
    }

    private void switchUser() {
        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<Order> query = ParseQuery.getQuery(Order.class);
                query.include(Order.KEY_DRIVER);

                query.whereEqualTo(Order.KEY_DRIVER, ParseUser.getCurrentUser());

                query.findInBackground(new FindCallback<Order>() {
                    @Override
                    public void done(List<Order> orders, ParseException e) {
                        if(e != null){
                            Log.e(TAG, "Issue with getting orders", e);
                            return;
                        }

                        if(!orders.isEmpty()){
                            Toast.makeText(getContext(), "You have an active order!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        goUserHome();
                    }
                });
            }
        });
    }

    // Go to user activity
    private void goUserHome() {
        currentUser.put(KEY_IS_DRIVER, false);

        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.e(TAG, "Error to switch value to user mode", e);
                }
            }
        });

        Intent i = new Intent(getContext(), UserMainActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    private void setValues() {
        editTextName.setText(currentUser.getString(KEY_NAME));
        editTextEmail.setText(currentUser.getEmail());

        ParseFile image = currentUser.getParseFile(KEY_PROFILE_PIC);

        if(image != null) {
            // Binds image to ViewHolder with rounded corners
            Log.i(TAG, String.valueOf(currentUser.getParseFile(KEY_PROFILE_PIC).getUrl()));

            Glide.with(getContext())
                    .load(currentUser.getParseFile(KEY_PROFILE_PIC).getUrl())
                    .fitCenter()
                    .circleCrop()
                    .into(imageViewProfile);
        }
    }

    // Set listener to update profile
    private void updateProfile() {
        buttonChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.setUsername(editTextName.getText().toString());
                currentUser.setEmail(editTextEmail.getText().toString());

                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null){
                            Log.e(TAG, "Error while saving", e);
                            Toast.makeText(getContext(), "Error updating profile!", Toast.LENGTH_SHORT).show();
                        }
                        Log.i(TAG, "update profile save was successful!");
                        Toast.makeText(getContext(), "Updated profile!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Logout button listener
    private void logout() {
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "User logging out!");

                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

                goLoginActivity();
            }
        });
    }

    // Goes to LoginActivity on click
    private void goLoginActivity() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}