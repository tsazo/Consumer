package com.codepath.tsazo.consumer.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.StoreActivity;
import com.codepath.tsazo.consumer.models.Order;
import com.codepath.tsazo.consumer.models.ParseStore;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.boltsinternal.Task;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserComposeFragment extends Fragment {

    public static final String TAG = "UserComposeFragment";
    private Button buttonChoose;
    private EditText editTextOrder;
    public TextView textViewStoreName;
    public TextView textViewStoreAddress;
    private TextView textViewPrice;
    private Button buttonPlaceOrder;
    private float price;
    private String storePlaceId;
    private Double storeLat;
    private Double storeLng;

    private final String KEY_ADDRESS = "address";

    public UserComposeFragment() {
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


        // TODO: FIX DEFAULT PRICE TO BE DEPENDENT ON HOW FAR THE STORE IS TO THE USER
        price = 3;
        textViewPrice.setText("$" + price + "0");

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

    // On place order, post order
    private void placeOrder() {
        buttonPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Place order button clicked");

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

                ParseUser currentUser = ParseUser.getCurrentUser();

                if(currentUser.getString(KEY_ADDRESS) == null){
                    Toast.makeText(getContext(), "You don't have a delivery address!", Toast.LENGTH_SHORT).show();
                    return;
                }


                // TODO: Add store chooser to completely finish a proper order request
                saveOrder(orderNumber, currentUser, store, price);
                //saveOrder(orderNumber, currentUser, price);
            }

        });
    }

    // Create ParseStore on save
    private ParseStore createStore(String storeName, String storeAddress) {
        ParseStore store = new ParseStore();

        //TODO: Change when I implement geocoding? (a.k.a. converting from coordinates to String addresses)
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(0,0);

        store.setName(storeName);
        store.setAddress(storePlaceId);
        //store.setAddress(storeAddress);
        store.setLocation(parseGeoPoint);

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

    // Sets the values of the stores once user selects a store in StoreActivity
    public void setStore(String name, String lat, String lng, String placeId){
        textViewStoreName.setText(name);
        textViewStoreAddress.setText(lat + "," + lng);
        storeLat = Double.valueOf(lat);
        storeLng = Double.valueOf(lng);
        storePlaceId = placeId;
    }

    // Save the order request to Parse
    private void saveOrder(String orderNumber, ParseUser currentUser, ParseStore store, float price) {
        Order order = new Order();

        order.setOrderNumber(orderNumber);
        order.setPrice(price);
        order.setUser(currentUser);
        order.setStore(store);
        order.setDeliveryAddress(currentUser.getString(KEY_ADDRESS));

        order.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                }

                Log.i(TAG, "Saving order was successful");
                editTextOrder.setText("");
                textViewStoreName.setText("");
                textViewStoreAddress.setText("");
                //textViewPrice.setText("");
            }
        });
    }
}
