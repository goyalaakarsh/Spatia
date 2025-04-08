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
import com.example.spatia.model.CartItem;
import com.example.spatia.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> items;
    private Map<Integer, Product> productMap;
    private NumberFormat currencyFormatter;

    public CheckoutItemAdapter(Context context, List<CartItem> items, Map<Integer, Product> productMap) {
        this.context = context;
        this.items = items;
        this.productMap = productMap;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkout_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        Product product = productMap.get(item.getProductId());
        
        if (product != null) {
            holder.productName.setText(product.getName());
            holder.quantity.setText("Qty: " + item.getQuantity());
            double itemTotal = product.getPrice() * item.getQuantity();
            holder.price.setText(currencyFormatter.format(itemTotal));
            
            // Load product image using Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(holder.productImage);
            } else {
                holder.productImage.setImageResource(R.drawable.placeholder_image);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView quantity;
        TextView price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.checkoutItemImage);
            productName = itemView.findViewById(R.id.checkoutItemName);
            quantity = itemView.findViewById(R.id.checkoutItemQuantity);
            price = itemView.findViewById(R.id.checkoutItemPrice);
        }
    }
}
