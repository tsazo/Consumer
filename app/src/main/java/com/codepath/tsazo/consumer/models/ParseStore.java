package com.codepath.tsazo.consumer.models;

import com.parse.ParseClassName;
        import com.parse.ParseGeoPoint;
        import com.parse.ParseObject;

@ParseClassName("Store")
public class ParseStore extends ParseObject {
    public static final String KEY_NAME = "storeName";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_PLACE_ID = "placeId";
    //public static final String KEY_CREATED_AT = "createdAt";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getAddress() {
        return getString(KEY_ADDRESS);
    }

    public void setAddress(String address) {
        put(KEY_ADDRESS, address);
    }

    public String getPlaceId() {
        return getString(KEY_PLACE_ID);
    }

    public void setPlaceId(String placeId) {
        put(KEY_PLACE_ID, placeId);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }

    public void setLocation(ParseGeoPoint parseGeoPoint) {
        put(KEY_LOCATION, parseGeoPoint);

    }
}
