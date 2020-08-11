package com.codepath.tsazo.consumer.models;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.tsazo.consumer.activities.MainActivity;
import com.codepath.tsazo.consumer.activities.StoreActivity;
import com.codepath.tsazo.consumer.adapters.StoresAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

@Parcel(analyze={Store.class})
public class Store {

    public interface OnStoreAddressSetListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onAddressSet(String address);
    }

    public static final String TAG = "Store";

    public String name;
    public String lat;
    public String lng;
    public String address;
    public String placeId;

    private static final String PLACES_DETAILS = "https://maps.googleapis.com/maps/api/place/details/json?place_id=";

    // empty constructor needed by the Parceler library
    public Store() {}

    // Create Store model from JSONObject parameter
    public static Store fromJson(JSONObject jsonObject, OnStoreAddressSetListener listener) throws JSONException {
        final Store store = new Store();
        store.name = jsonObject.getString("name");

        JSONObject locationCoordinates = jsonObject.getJSONObject("geometry").getJSONObject("location");
        double lat = locationCoordinates.getDouble("lat");
        double lng = locationCoordinates.getDouble("lng");

        store.lat = "" + locationCoordinates.getDouble("lat");
        store.lng = "" + locationCoordinates.getDouble("lng");

        store.placeId = jsonObject.getString("place_id");

        store.getAddress(listener);

        //Log.i(TAG, "Address: " + store.address);

        return store;
    }

    // Build the list of Stores that will appear in the StoresActivity
    public static List<Store> fromJsonArray(final JSONArray jsonArray) throws JSONException {
        List<Store> tempStores = new ArrayList<>();
        final List<Store> stores = new ArrayList<>();

        for(int i = 0; i < jsonArray.length(); i++){

            stores.add(fromJson(jsonArray.getJSONObject(i), new OnStoreAddressSetListener() {
                @Override
                public void onAddressSet(String address) {
                    Log.i(TAG, "onAddressSet: " + address);
                }
            }));

        }

        return stores;
    }

    // Access the Geocoding Api to convert coordinates to addresses
    public void getAddress(final OnStoreAddressSetListener listener) {
        AsyncHttpClient client = new AsyncHttpClient();

        String placesDetails = PLACES_DETAILS + placeId + "&key=AIzaSyB33o9qfsYo0BoA_oBOVAxN4XmQaamWIv4";

        client.get(placesDetails, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // onSuccess retrieved a json object, which enables us to create a JsonArray to iterate through
                JSONObject jsonObject = json.jsonObject;
                try {
                    address = jsonObject.getJSONObject("result").getString("formatted_address");
                    listener.onAddressSet(address);

                    StoreActivity.getAdapter().notifyDataSetChanged();
                    Log.i(TAG, address);
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
}
