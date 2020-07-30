package com.codepath.tsazo.consumer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.adapters.StoresAdapter;
import com.codepath.tsazo.consumer.fragments.UserComposeFragment;
import com.codepath.tsazo.consumer.models.Store;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class StoreActivity extends AppCompatActivity {
    public static final String TAG = "StoreActivity";

    private AsyncHttpClient client;
    private RecyclerView recyclerViewStores;
    private List<Store> stores;
    private static StoresAdapter adapter;
    private StoresAdapter.OnStoreSelectedListener listener;
    private ParseUser currentUser;
    private String userLocation;

    private String FIND_PLACE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
    private String FIND_PLACE_URL_END = "&radius=16000&type=clothing_store&key="; // a little less than 10 miles radius and clothing store

    private final static String KEY_ADDRESS_COORDS = "addressCoords";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        client = new AsyncHttpClient();

        currentUser = ParseUser.getCurrentUser();
        userLocation = currentUser.getString(KEY_ADDRESS_COORDS);

        FIND_PLACE_URL_END += getString(R.string.google_maps_api_key);

        // Find the RecyclerView
        recyclerViewStores = findViewById(R.id.recyclerViewStores);

        // Initialize the list of stores and adapter
        stores = new ArrayList<>();

        // implement the interface before you create the adapter. and pass it in the adapters constructor
        listener = new StoresAdapter.OnStoreSelectedListener() {
            @Override
            public void onStoreSelected(Store selectedStore) {
                // this is the code that will be executed once user selects the store
                Log.i(TAG, "Store: "+ selectedStore);

                try {
                    ((UserComposeFragment)(UserMainActivity.fragment)).setStore(selectedStore);
                } catch (Exception e){
                    Log.e(TAG, "Error... ", e);
                }
                finish();
            }
        };
        adapter = new StoresAdapter(this, stores, listener);

        // RecyclerView setup: layout manager and the adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewStores.setLayoutManager(layoutManager);
        recyclerViewStores.setAdapter(adapter);

        getStores();
    }

        // Populate the stores recyclerView
    private void getStores(){

        String placesUrl = FIND_PLACE_URL + userLocation + FIND_PLACE_URL_END;

        Log.i(TAG, "placesURL: " + placesUrl);

        client.get(placesUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess! " + json.toString());

                // onSuccess retrieved a json object, which enables us to create a JsonArray to iterate through
                JSONObject jsonObject = json.jsonObject;
                try {
                    adapter.clear();
                    adapter.addAll(Store.fromJsonArray(jsonObject.getJSONArray("results")));
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure! " + response, throwable);
            }
        });
    }

    public static StoresAdapter getAdapter(){
        return adapter;
    }
}