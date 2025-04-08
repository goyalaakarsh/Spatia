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

import java.util.List;

public class OrderPreviewItemAdapter extends RecyclerView.Adapter<OrderPreviewItemAdapter.ViewHolder> {
    
    private Context context;
    private List<OrderItem> orderItems;
    private int maxItems;
    
    public OrderPreviewItemAdapter(Context context, List<OrderItem> orderItems, int maxItems) {
        this.context = context;
        this.orderItems = orderItems;
        this.maxItems = maxItems;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_preview_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < orderItems.size() && position < maxItems) {
            OrderItem item = orderItems.get(position);
            
            // Set quantity badge
            holder.quantityText.setText("×" + item.getQuantity());
            
            // Load image
            if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
                Glide.with(context)
                    .load(item.getProductImage())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return Math.min(orderItems.size(), maxItems);
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView quantityText;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.previewItemImage);
            quantityText = itemView.findViewById(R.id.previewItemQuantity);
        }
    }
}
