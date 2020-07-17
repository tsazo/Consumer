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

// TODO: See if code actually needs this STORE class

//@ParseClassName("Store")
@Parcel(analyze={Store.class})
//public class Store extends ParseObject {
public class Store {
    public String name;
    public String lat;
    public String lng;

    public static final String KEY_NAME = "storeName";
    public static final String KEY_ADDRESS = "address";
    //public static final String KEY_CREATED_AT = "createdAt";

    // empty constructor needed by the Parceler library
    public Store() {}

//    public String getName() {
//        return getString(KEY_NAME);
//    }
//
//    public void setName(String name) {
//        put(KEY_NAME, name);
//    }
//
//    public String getAddress() {
//        return getString(KEY_ADDRESS);
//    }
//
//    public void setAddress(ParseGeoPoint address) {
//        put(KEY_ADDRESS, address);
//    }

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

//    public ParseFile getImage(){
//        return getParseFile(KEY_IMAGE);
//    }

//    public void setImage(ParseFile parseFile){
//        put(KEY_IMAGE, parseFile);
//    }
}
