package com.codepath.tsazo.consumer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.codepath.tsazo.consumer.activities.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User class is to reduce redundancy in code between the Driver and the Shopper
 */
public class User {

    private static final String KEY_PROFILE_PIC = "profilePicture";
    private static final String KEY_NAME = "name";

    public static void callPermission(Context context, final Activity activity, final int REQUEST_CODE){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            // when permission is not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE)){
                // Create AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Grant phone call permission");
                builder.setMessage("Call User");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE},REQUEST_CODE);
                    }
                });

                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE},REQUEST_CODE);
            }
        }
    }

    // Loads image from camera roll
    public static Bitmap loadFromUri(Uri photoUri, Context context) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // Creates file to save to Parse
    public static void createImageFile(Bitmap selectedImage, Context context, ParseUser currentUser, ProgressBar  progressBar) {
        File f = new File(context.getCacheDir(), "new.jpg");
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
            saveImageFile(f, context, currentUser, progressBar);
    }

    // Saves new image into Parse
    private static void saveImageFile(File f, final Context context, final ParseUser currentUser, final ProgressBar  progressBar) {
        final ParseFile parseImage = new ParseFile(f);

        // Call below signals to save the parseImage in the background, however the default image is still being used
        parseImage.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                // If successful add file to user and signUpInBackground
                if(e != null){
                    return;
                }

                currentUser.put(KEY_PROFILE_PIC, parseImage);
                currentUser.saveInBackground();
                Toast.makeText(context,"Updated picture.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    // Set listener to update name
    public static void updateName(final Context context, Button buttonChangeName, final EditText editTextUserName, final ParseUser currentUser) {
        buttonChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextUserName.getText().toString() != null || !editTextUserName.getText().toString().isEmpty()){
                    currentUser.put(KEY_NAME, editTextUserName.getText().toString());
                    currentUser.saveInBackground();
                    Toast.makeText(context,"Updated name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(context,"Please do not leave your name blank.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set listener to update email
    public static void updateEmail(final Context context, Button buttonChangeEmail, final EditText editTextEmail, final ParseUser currentUser) {
        buttonChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextEmail.getText().toString() != null || !editTextEmail.getText().toString().isEmpty()){
                    currentUser.setUsername(editTextEmail.getText().toString());
                    currentUser.setEmail(editTextEmail.getText().toString());
                    currentUser.saveInBackground();
                    Toast.makeText(context,"Updated email.", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(context,"Please do not leave your email blank.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Goes to LoginActivity on click
    public static void goLoginActivity(Context context, Activity activity) {
        Intent i = new Intent(context, LoginActivity.class);
        activity.startActivity(i);
        activity.finish();
    }

}
