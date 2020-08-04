package com.codepath.tsazo.consumer.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.User;
import com.codepath.tsazo.consumer.activities.ShopperMainActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverSettingsFragment extends Fragment {

    public static final String TAG = "DriverSettingsFragment";
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private ImageView imageViewProfile;
    private ParseUser currentUser;
    private Button buttonChangePicture;
    private Button buttonChangeName;
    private Button buttonChangeEmail;
    private Button buttonChangePhone;
    private TextView textViewEarnings;
    private Button buttonCashOut;
    private Button buttonUser;
    private Button buttonLogout;
    private ProgressBar progressBar;

    private boolean hasActiveOrder;
    private static DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private static final String KEY_PROFILE_PIC = "profilePicture";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phoneNumber";
    private static final String KEY_IS_DRIVER = "isDriver";
    private static final String KEY_HAS_ORDER = "hasOrder";
    private static final String KEY_EARNINGS = "earnings";

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

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
        textViewEarnings = view.findViewById(R.id.textViewEarnings);
        buttonChangePicture = view.findViewById(R.id.buttonChangePicture);
        buttonChangeName = view.findViewById(R.id.buttonChangeName);
        buttonChangeEmail = view.findViewById(R.id.buttonChangeEmail);
        buttonChangePhone = view.findViewById(R.id.buttonChangePhone);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        buttonCashOut = view.findViewById(R.id.buttonCashOut);
        buttonUser = view.findViewById(R.id.buttonUser);
        progressBar = view.findViewById(R.id.pbLoading);

        // Gets the person who's logged in
        currentUser = ParseUser.getCurrentUser();

        hasActiveOrder = currentUser.getBoolean(KEY_HAS_ORDER);

        editTextPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // Set values
        setValues();

        // Update picture
        updatePicture();

        // Update name
        User.updateName(getContext(), buttonChangeName, editTextName, currentUser);

        // Update email
        User.updateEmail(getContext(), buttonChangeEmail, editTextEmail, currentUser);

        // Update phone
        User.updatePhone(getContext(), buttonChangePhone, editTextPhone, currentUser);

        // Cashout earnings
        cashOut();

        // Switch to user
        goUserHome();

        // Logout button listener
        logout();
    }

    private void setValues() {
        editTextName.setText(currentUser.getString(KEY_NAME));
        editTextEmail.setText(currentUser.getEmail());
        textViewEarnings.setText("$"+ decimalFormat.format(currentUser.getNumber(KEY_EARNINGS)));
        editTextPhone.setText(currentUser.getString(KEY_PHONE));

        ParseFile image = currentUser.getParseFile(KEY_PROFILE_PIC);

        if(image != null) {
            // Binds image to ViewHolder with rounded corners
            Glide.with(getContext())
                    .load(currentUser.getParseFile(KEY_PROFILE_PIC).getUrl())
                    .fitCenter()
                    .circleCrop()
                    .into(imageViewProfile);
        }

    }

    // Set listener to update picture
    private void updatePicture() {
        buttonChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "update picture button clicked.");
                progressBar.setVisibility(ProgressBar.VISIBLE);

                // Create intent for picking a photo from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                // So as long as the result is not null, it's safe to use the intent.
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Bring up gallery to select a photo
                    startActivityForResult(intent, PICK_PHOTO_CODE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = User.loadFromUri(photoUri, getContext());

            // Load the selected image into a preview
            imageViewProfile.setImageBitmap(selectedImage);

            //create and save file into Parse
            User.createImageFile(selectedImage, getContext(), currentUser, progressBar);
        }  else {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private void cashOut() {
        buttonCashOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                Toast.makeText(getContext(), "Processing the money to your bank.", Toast.LENGTH_SHORT).show();
                currentUser.put(KEY_EARNINGS, 0);
                textViewEarnings.setText("$0.00");
                currentUser.saveInBackground();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    // Switches the driver to a user
    private void goUserHome() {
        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                if(hasActiveOrder){
                    Toast.makeText(getContext(), "You have an active order!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }

                currentUser.put(KEY_IS_DRIVER, false);

                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null){
                            Log.e(TAG, "Error to switch value to user mode", e);
                        }
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                });

                Intent i = new Intent(getContext(), ShopperMainActivity.class);
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
                if(hasActiveOrder){
                    Toast.makeText(getContext(), "You have an active order!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "User logging out!");

                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

                User.goLoginActivity(getContext(), getActivity());
            }
        });
    }
}