package com.codepath.tsazo.consumer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.models.Store;

import java.util.List;

public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.ViewHolder> {

    public interface OnStoreSelectedListener {
        void onStoreSelected(Store selectedStore);
    }

    public static final String TAG = "StoresAdapter";

    private Context context;
    private List<Store> stores;
    private OnStoreSelectedListener storeListener;

    public StoresAdapter(Context context, List<Store> stores, OnStoreSelectedListener onStoreSelectedListener) {
        this.context = context;
        this.stores = stores;
        storeListener = onStoreSelectedListener ;// define a private member variable that is assigned in the constructor (can also use a setter to do this)
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view for the item — in this case a tweet
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);

        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        final Store store = stores.get(position);

        // Bind the store with view holder
        holder.bind(store);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeListener.onStoreSelected(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        stores.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Store> storeList) {
        stores.addAll(storeList);
        notifyDataSetChanged();
        Log.i(TAG, "add all ran");
    }

    // Define a ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        //ImageView imageViewMedia;
        TextView textViewName;
        TextView textViewLocation;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            //imageViewMedia = itemView.findViewById(R.id.imageViewMedia);
        }

        // Take each attribute of the tweet and use those values to bind them to the screen
        public void bind(Store store) {
            textViewName.setText(store.name);
            textViewLocation.setText(store.address);
            //textViewLocation.setText(store.lat + "," + store.lng);
        }
    }
}
