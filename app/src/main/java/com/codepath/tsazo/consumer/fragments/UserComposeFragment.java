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
import com.codepath.tsazo.consumer.activities.MainActivity;
import com.codepath.tsazo.consumer.activities.StoreActivity;
import com.codepath.tsazo.consumer.adapters.StoresAdapter;
import com.codepath.tsazo.consumer.models.Order;
import com.codepath.tsazo.consumer.models.Store;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import static android.app.Activity.RESULT_OK;

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
                Log.i(TAG, "Choose store button clicked");

                // TODO: Add intent similar to launchCamera intent in Parstagram to choose a store
                // Or add something like a parcel?
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

//                Store store = new Store();
//                store.setName("PLACEHOLDER NAME");
//                store.setAddress("PLACEHOLDER ADDRESS");

                String storeName = textViewStoreName.getText().toString();
                String storeAddress = textViewStoreAddress.getText().toString();

                if(storeName == null || storeAddress == null){
                    Toast.makeText(getContext(), "You must choose your store", Toast.LENGTH_SHORT).show();
                    return;
                }

                ParseUser currentUser = ParseUser.getCurrentUser();

                if(currentUser.getString(KEY_ADDRESS) == null){
                    Toast.makeText(getContext(), "You don't have a delivery address!", Toast.LENGTH_SHORT).show();
                    return;
                }


                // TODO: Add store chooser to completely finish a proper order request
                //savePost(orderNumber, currentUser, store, price);
                savePost(orderNumber, currentUser, price);
            }

        });
    }

    // Handle the result of the sub-activity

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onstart!!!!!!");

        // implement the interface before you create the adapter. and pass it in the adapters constructor
        // getActivity().getIntent().getExtras()
        if (getArguments() != null) {
            Log.i(TAG, "getArguments...:" + getArguments().getParcelable("STORE_KEY_STRING"));
            Store store = (Store) Parcels.unwrap(getActivity().getIntent().getExtras().getParcelable("STORE_KEY_STRING"));
            textViewStoreName.setText(store.name);
            textViewStoreAddress.setText(store.lat + ", " + store.lng);
        }

//        if(getArguments() != null){
//            Store store = (Store) Parcels.unwrap(getArguments().getParcelable("STORE_KEY_STRING"));
//            Log.i(TAG, "store: " + store);
//
//            textViewStoreName.setText(store.name);
//            textViewStoreAddress.setText(store.lat + ", " + store.lng);
//        }
//        Store store = (Store) Parcels.unwrap(getArguments().getParcelable("STORE_KEY_STRING"));
//        Log.i(TAG, "store: " + store);
//        if (store != null){
//            textViewStoreName.setText(store.name);
//            textViewStoreAddress.setText(store.lat + ", " + store.lng);
//        }

    }

    public void setStore(String name, String lat, String lng){
        textViewStoreName.setText(name);
        textViewStoreAddress.setText(lat + ", " + lng);
    }

//    @Override
//    public void onResume(int requestCode, int resultCode, @Nullable Intent data) {
//        Log.i(TAG, "Returned to userComposeFragment from Store details view");
//        super.onResume(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            Store store = Parcels.unwrap(data.getParcelableExtra(Store.class.getSimpleName()));
//            textViewStoreName.setText(store.name);
//            textViewStoreAddress.setText(store.lat + ", " + store.lng);
//        }
//    }

    // Save the order request to Parse
    private void savePost(String orderNumber, ParseUser currentUser, float price) {
        Order order = new Order();

        order.setOrderNumber(orderNumber);
        order.setPrice(price);
        order.setUser(currentUser);

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
                textViewPrice.setText("");
            }
        });
    }
}
