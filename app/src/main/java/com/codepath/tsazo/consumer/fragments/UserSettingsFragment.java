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
import com.codepath.tsazo.consumer.activities.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserSettingsFragment extends Fragment {

    public static final String TAG = "UserSettingsFragment";
    private Button buttonLogout;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextAddress;
    private ImageView imageViewProfile;
    private ParseUser currentUser;
    private Button buttonChangeProfile;

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_PROFILE_PIC = "profilePicture";
    private static final String KEY_NAME = "name";

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
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        buttonChangeProfile = view.findViewById(R.id.buttonChangeProfile);

        // Gets the person who's logged in
        currentUser = ParseUser.getCurrentUser();

        // Set values
        setValues();

        // Update profile information
        updateProfile();

        // Logout button listener
        logout();
    }

    private void setValues() {
        editTextName.setText(currentUser.getString(KEY_NAME));
        editTextEmail.setText(currentUser.getEmail());

        Log.i(TAG, currentUser.getString(KEY_ADDRESS));

        if(currentUser.getString(KEY_ADDRESS) != null)
            editTextAddress.setText(currentUser.getString(KEY_ADDRESS));

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

                if(editTextAddress.getText().toString() != null)
                    currentUser.put(KEY_ADDRESS, editTextAddress.getText().toString());


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