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
import com.example.spatia.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private Context context;
    private List<Product> productList;
    private NumberFormat currencyFormatter;

    public HomeAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_card, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText("₹" + (product.getPrice()));
        holder.productCategory.setText(product.getCategory());

        Glide.with(context)
                .load(product.getImageUrl())
                .centerCrop()
                .into(holder.productImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle item click (can be implemented later)
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productCategory;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);
        }
    }
}