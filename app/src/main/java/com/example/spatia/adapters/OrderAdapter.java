package com.example.spatia.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spatia.R;
import com.example.spatia.model.Order;
import com.example.spatia.model.OrderItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final String TAG = "OrderAdapter";
    private Context context;
    private List<Order> orders;
    private NumberFormat currencyFormatter;
    private OrderClickListener listener;
    private FirebaseFirestore db;
    private static final int MAX_PREVIEW_ITEMS = 3;  // Maximum items to show in the preview

    public interface OrderClickListener {
        void onOrderClicked(Order order);
    }

    public OrderAdapter(Context context, List<Order> orders, OrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        try {
            Order order = orders.get(position);
            
            String orderId = order.getOrderId();
            if (orderId != null && orderId.length() > 8) {
                holder.orderIdText.setText("Order #" + orderId.substring(0, 8));
            } else {
                holder.orderIdText.setText("Order #" + (orderId != null ? orderId : ""));
            }
            
            holder.orderDateText.setText(order.getFormattedOrderDate());
            holder.orderStatusText.setText(order.getOrderStatus());
            holder.orderAmountText.setText(currencyFormatter.format(order.getTotalAmount()));
            
            // Set status color based on status
            int statusColor;
            String status = order.getOrderStatus();
            if (status == null) status = "";
            
            switch (status) {
                case "Delivered":
                    statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                    break;
                case "Cancelled":
                    statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
                    break;
                case "Shipped":
                    statusColor = context.getResources().getColor(R.color.pri_yellow);
                    break;
                default:
                    statusColor = context.getResources().getColor(R.color.primary_dark);
                    break;
            }
            holder.orderStatusText.setTextColor(statusColor);
            
            // Fetch and load order items if not already loaded
            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                loadOrderItems(order, holder);
            } else {
                setupOrderItemsPreview(holder, order.getOrderItems());
            }
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClicked(order);
                }
            });
            
            // Set up click listener for the "View Details" text
            holder.viewDetailsText.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClicked(order);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error binding order at position " + position, e);
        }
    }

    private void loadOrderItems(Order order, OrderViewHolder holder) {
        db.collection("orders").document(order.getOrderId())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                List<Map<String, Object>> items = (List<Map<String, Object>>) documentSnapshot.get("items");
                if (items != null && !items.isEmpty()) {
                    List<OrderItem> orderItems = new ArrayList<>();
                    
                    for (Map<String, Object> item : items) {
                        try {
                            int productId = 0;
                            int quantity = 1;
                            
                            if (item.get("productId") instanceof Long) {
                                productId = ((Long) item.get("productId")).intValue();
                            }
                            
                            if (item.get("quantity") instanceof Long) {
                                quantity = ((Long) item.get("quantity")).intValue();
                            }
                            
                            String productName = (String) item.get("productName");
                            double productPrice = 0;
                            if (item.get("productPrice") instanceof Double) {
                                productPrice = (double) item.get("productPrice");
                            }
                            String productImage = (String) item.get("productImage");
                            
                            OrderItem orderItem = new OrderItem(productId, quantity);
                            orderItem.setProductName(productName != null ? productName : "Product");
                            orderItem.setProductPrice(productPrice);
                            orderItem.setProductImage(productImage);
                            
                            orderItems.add(orderItem);
                        } catch (Exception e) {
                            // Skip this item if there's an error
                        }
                    }
                    
                    order.setOrderItems(orderItems);
                    setupOrderItemsPreview(holder, orderItems);
                }
            });
    }
    
    private void setupOrderItemsPreview(OrderViewHolder holder, List<OrderItem> orderItems) {
        if (orderItems != null && !orderItems.isEmpty()) {
            // Set up horizontal RecyclerView for preview items
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false);
            holder.orderItemsPreviewRecyclerView.setLayoutManager(layoutManager);
            
            // Create and set the adapter
            OrderPreviewItemAdapter previewAdapter = new OrderPreviewItemAdapter(
                    context, orderItems, MAX_PREVIEW_ITEMS);
            holder.orderItemsPreviewRecyclerView.setAdapter(previewAdapter);
            
            // Show "more items" text if needed
            if (orderItems.size() > MAX_PREVIEW_ITEMS) {
                holder.moreItemsText.setVisibility(View.VISIBLE);
                holder.moreItemsText.setText("+" + (orderItems.size() - MAX_PREVIEW_ITEMS) + " more items");
            } else {
                holder.moreItemsText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText;
        TextView orderDateText;
        TextView orderStatusText;
        TextView orderAmountText;
        TextView moreItemsText;
        TextView viewDetailsText;
        RecyclerView orderItemsPreviewRecyclerView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            orderDateText = itemView.findViewById(R.id.orderDateText);
            orderStatusText = itemView.findViewById(R.id.orderStatusText);
            orderAmountText = itemView.findViewById(R.id.orderAmountText);
            moreItemsText = itemView.findViewById(R.id.moreItemsText);
            viewDetailsText = itemView.findViewById(R.id.viewDetailsText);
            orderItemsPreviewRecyclerView = itemView.findViewById(R.id.orderItemsPreviewRecyclerView);
        }
    }
}
