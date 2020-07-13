package com.codepath.tsazo.consumer;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Order")
public class Order extends ParseObject {
    public static final String KEY_ORDER = "orderNumber";
    public static final String KEY_PRICE = "price";
    public static final String KEY_USER = "user";
    //public static final String KEY_CREATED_AT = "createdAt";

    public String getOrderNumber(){
        return getString(KEY_ORDER);
    }

    public void setOrderNumber(String orderNumber){
        put(KEY_ORDER, orderNumber);
    }

    public String getPrice(){
        return getString(KEY_PRICE);
    }

    public void setPrice(float price){
        put(KEY_PRICE, price);
    }

//    public ParseFile getImage(){
//        return getParseFile(KEY_IMAGE);
//    }

//    public void setImage(ParseFile parseFile){
//        put(KEY_IMAGE, parseFile);
//    }

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser){
        put(KEY_USER, parseUser);
    }
}
