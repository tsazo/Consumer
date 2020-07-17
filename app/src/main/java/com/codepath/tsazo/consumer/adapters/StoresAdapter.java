package com.codepath.tsazo.consumer.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.StoreActivity;
import com.codepath.tsazo.consumer.activities.UserMainActivity;
import com.codepath.tsazo.consumer.fragments.UserComposeFragment;
import com.codepath.tsazo.consumer.models.Store;

import org.parceler.Parcels;

import java.util.List;

public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.ViewHolder> {

    public static final String TAG = "StoresAdapter";

    Context context;
    List<Store> stores;

    public StoresAdapter(Context context, List<Store> stores){
        this.context = context;
        this.stores = stores;
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
        Store store = stores.get(position);

        // Bind the tweet with view holder
        holder.bind(store);
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }


    /* Within the RecyclerView.Adapter class to implement the pull-to-refresh action */
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
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //ImageView imageViewMedia;
        TextView textViewName;
        TextView textViewLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            //imageViewMedia = itemView.findViewById(R.id.imageViewMedia);

            itemView.setOnClickListener(this);
        }

        // Take each attribute of the tweet and use those values to bind them to the screen
        public void bind(Store store) {
            textViewName.setText(store.name);
            textViewLocation.setText(store.lat + "," + store.lng);

//            if(store.media.size() > 0){
//                imageViewMedia.setVisibility(View.VISIBLE);
//                String embeddedImageURL = tweet.media.get(0).baseURL;
//                Log.i("TweetsAdapter", "baseURl: " + embeddedImageURL);
//                Glide.with(context).load(embeddedImageURL)
//                        .fitCenter()
//                        .transform(new RoundedCornersTransformation(10, 10))
//                        .into(imageViewMedia);
//            } else {
//                imageViewMedia.setVisibility(View.GONE);
//            }
        }

        // when user clicks on a row, send the store information to the UserComposeFragment
        @Override
        public void onClick(View view) {
            // Gets the item position
            int position = getAdapterPosition();

            Log.i(TAG, "Position of store: " + String.valueOf(position));

            // make sure the position is valid, i.e. exists in the view
            if (position != RecyclerView.NO_POSITION){

                // get the movie at the position, this won't work if the class is static
                Store store = stores.get(position);

                Log.i(TAG, "Store: "+ store);

                // create intent for the new activity
                Bundle bundle = new Bundle();
                bundle.putString("name", store.name);
                bundle.putString("lng", store.lng);
                bundle.putString("lat", store.lat);

                UserComposeFragment userComposeFragment = new UserComposeFragment();
                userComposeFragment.setArguments(bundle);

                //Log.i(TAG, "intent: "+intent);

                // serialize the movie using parceler, use its short nasme as a key
                // First string is the name, second string is the value
                //intent.putExtra(Store.class.getSimpleName(), Parcels.wrap(store));

                // show the activity
                //context.startActivity(intent);
            }
        }

    }
}
