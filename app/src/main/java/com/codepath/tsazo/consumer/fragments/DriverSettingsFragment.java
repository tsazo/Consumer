package com.codepath.tsazo.consumer.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
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
import com.codepath.tsazo.consumer.activities.LoginActivity;
import com.codepath.tsazo.consumer.activities.UserMainActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverSettingsFragment extends Fragment {

    public static final String TAG = "DriverSettingsFragment";
    private EditText editTextName;
    private EditText editTextEmail;
    private ImageView imageViewProfile;
    private ParseUser currentUser;
    private Button buttonChangePicture;
    private Button buttonChangeName;
    private Button buttonChangeEmail;
    private TextView textViewEarnings;
    private Button buttonCashOut;
    private Button buttonUser;
    private Button buttonLogout;
    private ProgressBar progressBar;

    private boolean hasActiveOrder;
    private static DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private static final String KEY_PROFILE_PIC = "profilePicture";
    private static final String KEY_NAME = "name";
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
        buttonCashOut = view.findViewById(R.id.buttonCashOut);
        buttonUser = view.findViewById(R.id.buttonUser);
        progressBar= view.findViewById(R.id.pbLoading);


        // Gets the person who's logged in
        currentUser = ParseUser.getCurrentUser();

        hasActiveOrder = currentUser.getBoolean(KEY_HAS_ORDER);

        // Set values
        setValues();

        // Update picture
        updatePicture();

        // Update name
        updateName();

        // Update email
        updateEmail();

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

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // TODO: Break method down into mutiple methods
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            imageViewProfile.setImageBitmap(selectedImage);

            //create and save file into Parse
            createImageFile(selectedImage);
        }
    }

    private void createImageFile(Bitmap selectedImage) {
        File f = new File(getContext().getCacheDir(), "new.jpg");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(fos != null)
            saveImageFile(f);
    }

    // Saves new image into Parse
    private void saveImageFile(File f) {
        final ParseFile parseImage = new ParseFile(f);

        // Call below signals to save the parseImage in the background, however the default image is still being used
        parseImage.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // If successful add file to user and signUpInBackground
                    if(e != null){
                        Log.e(TAG, "Error saving image to Parse", e);
                    }
                    currentUser.put(KEY_PROFILE_PIC, parseImage);
                    currentUser.saveInBackground();
                    Toast.makeText(getContext(),"Updated picture.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
        });
    }

    // Set listener to update name
    private void updateName() {
        buttonChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                if(editTextName.getText().toString() != null || !editTextName.getText().toString().isEmpty()){
                    currentUser.put(KEY_NAME, editTextName.getText().toString());
                    currentUser.saveInBackground();
                    Toast.makeText(getContext(),"Updated name.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }
                Toast.makeText(getContext(),"Please do not leave your name blank.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    // Set listener to update email
    private void updateEmail() {
        buttonChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                if(editTextEmail.getText().toString() != null || !editTextEmail.getText().toString().isEmpty()){
                    currentUser.setUsername(editTextEmail.getText().toString());
                    currentUser.setEmail(editTextEmail.getText().toString());
                    currentUser.saveInBackground();
                    Toast.makeText(getContext(),"Updated email.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }
                Toast.makeText(getContext(),"Please do not leave your email blank.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
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
                if(hasActiveOrder){
                    Toast.makeText(getContext(), "You have an active order!", Toast.LENGTH_SHORT).show();
                    return;
                }

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