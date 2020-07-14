package com.codepath.tsazo.consumer.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Store")
public class Store extends ParseObject {
    public static final String KEY_NAME = "storeName";
    public static final String KEY_ADDRESS = "address";
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

//    public ParseFile getImage(){
//        return getParseFile(KEY_IMAGE);
//    }

//    public void setImage(ParseFile parseFile){
//        put(KEY_IMAGE, parseFile);
//    }
}
