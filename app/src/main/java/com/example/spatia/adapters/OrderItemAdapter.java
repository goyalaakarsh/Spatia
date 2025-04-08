package com.example.spatia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spatia.R;
import com.example.spatia.model.OrderItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private Context context;
    private List<OrderItem> orderItems;
    private NumberFormat currencyFormatter;

    public OrderItemAdapter(Context context, List<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        
        holder.orderItemName.setText(item.getProductName());
        holder.orderItemQuantity.setText("Qty: " + item.getQuantity());
        double totalPrice = item.getProductPrice() * item.getQuantity();
        holder.orderItemPrice.setText(currencyFormatter.format(totalPrice));
        
        // Load product image
        if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
            Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.orderItemImage);
        } else {
            holder.orderItemImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView orderItemImage;
        TextView orderItemName;
        TextView orderItemQuantity;
        TextView orderItemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderItemImage = itemView.findViewById(R.id.orderItemImage);
            orderItemName = itemView.findViewById(R.id.orderItemName);
            orderItemQuantity = itemView.findViewById(R.id.orderItemQuantity);
            orderItemPrice = itemView.findViewById(R.id.orderItemPrice);
        }
    }
}
