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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.DriverMainActivity;
import com.codepath.tsazo.consumer.activities.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserSettingsFragment extends Fragment {

    public static final String TAG = "UserSettingsFragment";
    private EditText editTextUserName;
    private EditText editTextUserEmail;
    private EditText editTextAddress;
    private ImageView imageViewProfile;
    private ParseUser currentUser;
    private Button buttonChangePicture;
    private Button buttonChangeName;
    private Button buttonChangeEmail;
    private Button buttonChangeAddress;
    private Button buttonDriver;
    private Button buttonLogout;

    public static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_ADDRESS_COORDS = "addressCoords";
    private static final String KEY_PROFILE_PIC = "profilePicture";
    private static final String KEY_NAME = "name";
    private static final String KEY_IS_DRIVER = "isDriver";

    public UserSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_settings, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup any handles to view objects here
        // Need to use view.findViewById as Fragment class doesn't extend View, but rather fragment
        buttonLogout = view.findViewById(R.id.buttonLogout);
        editTextUserName = view.findViewById(R.id.editTextUserName);
        editTextUserEmail = view.findViewById(R.id.editTextUserEmail);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        buttonChangePicture = view.findViewById(R.id.buttonChangePicture);
        buttonChangeName = view.findViewById(R.id.buttonChangeName);
        buttonChangeEmail = view.findViewById(R.id.buttonChangeEmail);
        buttonChangeAddress = view.findViewById(R.id.buttonChangeAddress);
        buttonDriver = view.findViewById(R.id.buttonDriver);

        // Gets the person who's logged in
        currentUser = ParseUser.getCurrentUser();

        // Set values
        setValues();

        // Update picture
        updatePicture();

        // Update name
        updateName();

        // Update email
        updateEmail();

        // Update address
        updateEmail();

        // Update profile information
        updateAddress();

        // Switch to driver
        switchDriver();

        // Logout button listener
        logout();
    }

    private void setValues() {
        editTextUserName.setText(currentUser.getString(KEY_NAME));
        editTextUserEmail.setText(currentUser.getEmail());

        String address = currentUser.getString(KEY_ADDRESS);

        if(address != null || !address.isEmpty()){
            editTextAddress.setText(address);
        }

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

    // TODO: PICTURE INTENT
    // Set listener to update picture
    private void updatePicture() {
        buttonChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "update picture button clicked.");
            }
        });
    }

    // Set listener to update name
    private void updateName() {
        buttonChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextUserName.getText().toString() != null || !editTextUserName.getText().toString().isEmpty()){
                    currentUser.put(KEY_NAME, editTextUserName.getText().toString());
                    currentUser.saveInBackground();
                    Toast.makeText(getContext(),"Updated name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(),"Please do not leave your name blank.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set listener to update email
    private void updateEmail() {
        buttonChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextUserEmail.getText().toString() != null || !editTextUserEmail.getText().toString().isEmpty()){
                    currentUser.setUsername(editTextUserEmail.getText().toString());
                    currentUser.setEmail(editTextUserEmail.getText().toString());
                    currentUser.saveInBackground();
                    Toast.makeText(getContext(),"Updated email.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(),"Please do not leave your email blank.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set listener to update profile
    private void updateAddress() {
        buttonChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = editTextAddress.getText().toString();

                if(address != null || !address.isEmpty()){
                    geocodeAddress(address);
                }
                Toast.makeText(getContext(),"Please do not leave your address blank.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Converts address to geographical coordinates using the Geocoding API
    private void geocodeAddress(final String address) {
        AsyncHttpClient client = new AsyncHttpClient();

        String addressURL = GEOCODE_URL + address.replace(" ", "+") + "&key=" + getString(R.string.google_maps_api_key);

        client.get(addressURL, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess! " + json.toString());

                // onSuccess retrieved a json object, which enables us to create a JsonArray to iterate through
                JSONObject jsonObject = json.jsonObject;
                try {

                    Double lat = jsonObject.getJSONArray("results")
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .getDouble("lat");

                    Double lng = jsonObject.getJSONArray("results")
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .getDouble("lng");

                    currentUser.put(KEY_ADDRESS, address);

                    currentUser.put(KEY_ADDRESS_COORDS, lat + "," + lng);

                    currentUser.saveInBackground();
                    Log.i(TAG, address);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "Invalid Address", Toast.LENGTH_SHORT).show();
                    editTextAddress.setText(currentUser.getString(KEY_ADDRESS));
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure! " + response, throwable);
                Toast.makeText(getContext(), "Invalid address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TODO: Look at license number before proceeding - no user can be a driver without a license (week 4 task)
    // Switches the user to a driver
    private void switchDriver() {
        buttonDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser.put(KEY_IS_DRIVER, true);

                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null){
                            Log.e(TAG, "Error to switch value to driver mode", e);
                        }
                    }
                });

                Intent i = new Intent(getContext(), DriverMainActivity.class);
                startActivity(i);
                getActivity().finish();
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