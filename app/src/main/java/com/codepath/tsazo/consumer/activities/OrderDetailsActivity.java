package com.codepath.tsazo.consumer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.models.Order;
import com.parse.ParseException;
import com.parse.ParseFile;

import org.parceler.Parcels;

public class OrderDetailsActivity extends AppCompatActivity {


    private static final String TAG = "OrderDetailsActivity";
    private String KEY_STORE_NAME = "storeName";
    private String KEY_STORE_ADDRESS = "address";

    // the Post to display
    private Order order;

    // the view objects
    private TextView textViewStoreName;
    private TextView textViewStoreAddress;
    private TextView textViewOrderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        textViewStoreName = findViewById(R.id.textViewStoreName);
        textViewStoreAddress = findViewById(R.id.textViewStoreAddress);
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);

        // Unwrap the movie passed in via intent, using its simple name as a key
        order = (Order) Parcels.unwrap(getIntent().getParcelableExtra(Order.class.getSimpleName()));

        setValues();
    }

    // Method to set the values into the views
    private void setValues() {

        try {
            Log.i(TAG, "Store name: "+ order.getStore().get(KEY_STORE_NAME));
            Log.i(TAG, "Store address: "+ order.getStore().get(KEY_STORE_ADDRESS));
            textViewStoreName.setText(order.getStore().fetchIfNeeded().getString(KEY_STORE_NAME));
            textViewStoreAddress.setText(order.getStore().fetchIfNeeded().getString(KEY_STORE_ADDRESS));
        } catch (Exception e){
            Log.e(TAG, "Cannot fetch store name or address", e);
        }

        textViewOrderNumber.setText(order.getOrderNumber());
    }
}