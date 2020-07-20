package com.codepath.tsazo.consumer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.models.Order;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverOrderFragment extends Fragment {

    public static final String TAG = "DriverOrderFragment";
    private ParseUser currentUser;
    private TextView textViewOrderHeader;
    private boolean hasActiveOrder;

    private final static String KEY_HAS_ORDER = "hasOrder";

    public DriverOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_order, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup any handles to view objects here
        textViewOrderHeader = view.findViewById(R.id.textViewOrderHeader);

        // Gets the person who's logged in
        currentUser = ParseUser.getCurrentUser();

        hasActiveOrder = currentUser.getBoolean(KEY_HAS_ORDER);

        if(hasActiveOrder){
            setValues();
        } else {
            textViewOrderHeader.setText("No Current Order");
        }

    }

    // Sets all the values for the current order if there is one.
    private void setValues() {
        textViewOrderHeader.setText("Current Order");

    }
}