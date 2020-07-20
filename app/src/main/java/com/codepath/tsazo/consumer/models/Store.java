package com.codepath.tsazo.consumer.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel(analyze={Store.class})
public class Store {
    public String name;
    public String lat;
    public String lng;

    // empty constructor needed by the Parceler library
    public Store() {}

    // Create Store model from JSONObject parameter
    public static Store fromJson(JSONObject jsonObject) throws JSONException {
        Store store = new Store();
        store.name = jsonObject.getString("name");

        JSONObject locationCoordinates = jsonObject.getJSONObject("geometry").getJSONObject("location");
        double lat = locationCoordinates.getDouble("lat");
        double lng = locationCoordinates.getDouble("lng");

        store.lat = "" + locationCoordinates.getDouble("lat");
        store.lng = "" + locationCoordinates.getDouble("lng");

        return store;
    }

    // Build the list of Stores that will appear in the StoresActivity
    public static List<Store> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Store> stores = new ArrayList<>();

        for(int i = 0; i < jsonArray.length(); i++){
            stores.add(fromJson(jsonArray.getJSONObject(i)));
        }

        return stores;
    }
}
