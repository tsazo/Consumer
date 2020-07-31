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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.StoreActivity;
import com.codepath.tsazo.consumer.models.Order;
import com.codepath.tsazo.consumer.models.ParseStore;

import com.codepath.tsazo.consumer.models.Store;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.DecimalFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopperComposeFragment extends Fragment {

    public static final String TAG = "ShopperComposeFragment";
    private Button buttonChoose;
    private EditText editTextOrder;
    public TextView textViewStoreName;
    public TextView textViewStoreAddress;
    private TextView textViewPrice;
    private Button buttonPlaceOrder;
    private ProgressBar pb;

    ParseUser currentUser;
    private float price;
    private String storePlaceId;
    private Double storeLat;
    private Double storeLng;

    private static DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private final String KEY_ADDRESS = "address";
    private final String KEY_ADDRESS_COORDS = "addressCoords";

    public ShopperComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_compose, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup any handles to view objects here
        // Need to use view.findViewById as Fragment class doesn't extend View, but rather fragment
        buttonChoose = view.findViewById(R.id.buttonChoose);
        editTextOrder = view.findViewById(R.id.editTextOrder);
        textViewStoreName = view.findViewById(R.id.textViewStoreName);
        textViewStoreAddress = view.findViewById(R.id.textViewStoreAddress);
        textViewPrice = view.findViewById(R.id.textViewPrice);
        buttonPlaceOrder = view.findViewById(R.id.buttonPlaceOrder);
        pb = view.findViewById(R.id.pbLoading);

        currentUser = ParseUser.getCurrentUser();

        price = 0;
        textViewPrice.setText("$" + decimalFormat.format(price));

        // Choose store
        chooseStore();

        // Place order
        placeOrder();
    }

    // On choose store, open StoreActivity to retrieve store information
    private void chooseStore() {
        // On choose store, open StoreActivity to retrieve store information
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StoreActivity.class);

                startActivity(intent);
            }
        });
    }

    // Sets the values of the stores once user selects a store in StoreActivity
    public void setStore(Store store){
        textViewStoreName.setText(store.name);
        storeLat = Double.valueOf(store.lat);
        storeLng = Double.valueOf(store.lng);
        storePlaceId = store.placeId;

        setPrice();

        textViewStoreAddress.setText(store.address);
    }

    private void setPrice() {
        String[] deliveryCoords = currentUser.getString(KEY_ADDRESS_COORDS).split(",");

        double distance = calculateDistance(storeLat, storeLng, Double.valueOf(deliveryCoords[0]), Double.valueOf(deliveryCoords[1]));

        price = 3 + (float) (distance * 0.75);
        textViewPrice.setText("$" + decimalFormat.format(price));
    }

    // TODO: consolidate method in it's own java file
    // Calculate distances between two coordinate points in miles
    private double calculateDistance(double lat1, double long1, double lat2, double long2) {
        if ((lat1 == lat2) && (long1 == long2)) {
            return 0;
        }
        double theta = long1 - long2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;

        return (dist);
    }

    // On place order, post order
    private void placeOrder() {
        buttonPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb.setVisibility(ProgressBar.VISIBLE);

                String orderNumber = editTextOrder.getText().toString();
                if(orderNumber.isEmpty()){
                    Toast.makeText(getContext(), "Order number cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String storeName = textViewStoreName.getText().toString();
                String storeAddress = textViewStoreAddress.getText().toString();

                if(storeName.isEmpty() || storeAddress.isEmpty()){
                    Toast.makeText(getContext(), "You must choose your store", Toast.LENGTH_SHORT).show();
                    return;
                }

                ParseStore store = createStore(storeName, storeAddress);

                if(currentUser.getString(KEY_ADDRESS) == null){
                    Toast.makeText(getContext(), "You don't have a delivery address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveOrder(orderNumber, store);
            }

        });
    }

    // Create ParseStore on save
    private ParseStore createStore(String storeName, String storeAddress) {
        ParseStore store = new ParseStore();

        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(storeLat,storeLng);

        store.setName(storeName);
        store.setAddress(storeAddress);
        store.setLocation(parseGeoPoint);
        store.setPlaceId(storePlaceId);

        store.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving store", Toast.LENGTH_SHORT).show();
                }

                Log.i(TAG, "Saving store was successful");
            }
        });

        return store;
    }

    // Save the order request to Parse
    private void saveOrder(String orderNumber, ParseStore store) {
        Order order = new Order();

        order.setOrderNumber(orderNumber);
        order.setPrice(price);
        order.setUser(currentUser);
        order.setStore(store);
        order.setDeliveryAddress(currentUser.getString(KEY_ADDRESS));

        order.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                pb.setVisibility(ProgressBar.INVISIBLE);

                if(e != null){
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(getContext(), "Order created!" , Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Saving order was successful");
                editTextOrder.setText("");
                textViewStoreName.setText("");
                textViewStoreAddress.setText("");

                price = 0;
                textViewPrice.setText("$" + decimalFormat.format(price));
            }
        });
    }
}
