package com.codepath.tsazo.consumer.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.tsazo.consumer.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.File;
import java.io.IOException;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonUserSignup;
    private ImageView imageViewProfile;
    private final String KEY_PICTURE = "profilePicture";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextPassword = findViewById(R.id.editTextPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonUserSignup = findViewById(R.id.buttonUserSignup);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        // On take picture, open camera (or even camera roll)
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Access external media from camera roll");

                launchCamera();
            }
        });

        buttonUserSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Signup button clicked");
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                signupUser(email, password);
            }
        });
    }

    // Launches the camera roll with an Intent
    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileprovider.consumer", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                // RESIZE BITMAP, see section below

                // Load the taken image into a preview
                //imageViewProfile.setImageBitmap(takenImage);
                Glide.with(this).load(takenImage).fitCenter().circleCrop().into(imageViewProfile);

            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    // Checks if the user has entered in signup credentials, if so, user is taken to the MainActivity
    private void signupUser(String email, String password) {
        Log.i(TAG, "Attempting to signup user: " + email);

        // Create the ParseUser
        final ParseUser user = new ParseUser();
        // TODO: Fix bug
        final ParseFile parseImage = new ParseFile(photoFile);

        // Set core properties
        if(email == "" || password == "") {
            Log.e(TAG, "Not all required fields have been filled out");
            Toast.makeText(SignupActivity.this, "Not all required fields have been filled out", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setUsername(email);
        user.setPassword(password);
        user.setEmail(email);

        // Set custom properties

        // Call below signals to save the parseImage in the background, however the default image is still being used
        // TODO: Fix this bug such that the user can upload a taken photo properly
//        parseImage.saveInBackground(new SaveCallback() {
//            public void done(ParseException e) {
//                // If successful add file to user and signUpInBackground
//                if(null == e){
//                    Log.i(TAG, "Saved image to parse");
//                    Log.i(TAG, "The value of parseImage is: "+ parseImage);
//                }
//            }
//        });
//
//        if(parseImage != null){
//            user.put(KEY_PICTURE, parseImage);
//        }


        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with signup", e);
                    Toast.makeText(SignupActivity.this, "Issue with signup!", Toast.LENGTH_SHORT).show();
                    return;
                }

                goLoginActivity();
                Toast.makeText(SignupActivity.this, "Successfully signed up!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    // Creates a flow (using Intents) to the MainActivity
    private void goLoginActivity() {
        // Intent(this context, activity I want to navigate to)
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}