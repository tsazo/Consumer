package com.codepath.tsazo.consumer.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.tsazo.consumer.R;
import com.codepath.tsazo.consumer.activities.OrderDetailsActivity;
import com.codepath.tsazo.consumer.models.Order;
import com.parse.ParseException;

import org.parceler.Parcels;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    private static final String TAG = "OrdersAdapter";
    private Context context;
    private List<Order> orders;
    private String KEY_STORE_NAME = "storeName";
    private String KEY_STORE_ADDRESS = "address";

    public OrdersAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    // Method to connect the item_post to the PostsFragment ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    // Bind the item_post to the RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewStoreName;
        private TextView textViewStoreAddress;
        private TextView textViewOrderNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewStoreName = itemView.findViewById(R.id.textViewStoreName);
            textViewStoreAddress = itemView.findViewById(R.id.textViewStoreAddress);
            textViewOrderNumber = itemView.findViewById(R.id.textViewOrderNumber);

            itemView.setOnClickListener(this);
        }

        public void bind(final Order order) {
            // Bind the post data to the view elements

            try {
                order.fetch();
                textViewStoreName.setText(order.getStore().getString(KEY_STORE_NAME));
                textViewStoreAddress.setText(order.getStore().getString(KEY_STORE_ADDRESS));

                Log.i(TAG, "Store name: "+ order.getStore().get(KEY_STORE_NAME));
                Log.i(TAG, "Store address: "+ order.getStore().get(KEY_STORE_ADDRESS));
            } catch (Exception e) {
                Log.e(TAG, "Cannot fetch store", e);
            }

            textViewOrderNumber.setText(order.getOrderNumber());

            // TODO: Add image of store given my Google Maps API ??
//            String profileImage = order.getUser().getParseFile(KEY_PROFILE_PIC).getUrl();
//            if(profileImage != null) {
//                Glide.with(context).load(profileImage)
//                        .fitCenter()
//                        .circleCrop()
//                        .into(imageViewProfile);
//            }
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "Post clicked");
            // Gets item position
            int position = getAdapterPosition();

            // Make sure the position is valid i.e actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // Get the post at the position, this won't work if the class is static
                Order order = orders.get(position);

                // Create intent for the new activity
                Intent intent = new Intent(context, OrderDetailsActivity.class);

                // Serialize the post using the parceler, use its short name as a key
                intent.putExtra(Order.class.getSimpleName(), Parcels.wrap(order));

                // Show the activity
                context.startActivity(intent);
            }
        }
    }
}
