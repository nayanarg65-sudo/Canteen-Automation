package com.example.canteenautomation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private ArrayList<OrderHistoryModel> list;

    public OrderHistoryAdapter(ArrayList<OrderHistoryModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderHistoryModel order = list.get(position);

        if (order.orderId != null && !order.orderId.isEmpty()) {
            holder.tvId.setText("Order ID: " + order.orderId);
        } else {
            holder.tvId.setText("Order ID: ----");
        }

        // 2. Set Items and Total
        holder.tvItems.setText(order.items);
        holder.tvTotal.setText("₹" + order.total);

        // 3. Handle Status Badge
        holder.tvStatus.setText(order.status != null ? order.status.toUpperCase() : "PENDING");
        if ("Ready".equalsIgnoreCase(order.status) || "Delivered".equalsIgnoreCase(order.status)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Orange
        }

        // 4. Format Timestamp to readable Date
        if (order.timestamp != 0) {
            Date date = new Date(order.timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(date));
        } else {
            holder.tvDate.setText("Date not available");
        }

        // 5. Reorder Button Logic
        holder.btnReorder.setOnClickListener(v -> {
            if (order.items != null) {
                // Clear existing cart
                CartManager.clearCart();

                // Create a temporary model for the reorder
                FoodModel reorderItem = new FoodModel();
                reorderItem.name = order.items;
                reorderItem.price = order.total.toString();
                reorderItem.quantity = 1;

                // Add to your global CartManager
                CartManager.addItem(reorderItem);

                Toast.makeText(v.getContext(), "Opening Cart to reorder...", Toast.LENGTH_SHORT).show();

                // Redirect to CartActivity
                android.content.Intent intent = new android.content.Intent(v.getContext(), CartActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvItems, tvTotal, tvStatus, tvDate;
        android.widget.Button btnReorder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvHistoryOrderId);
            tvItems = itemView.findViewById(R.id.tvHistoryItems);
            tvTotal = itemView.findViewById(R.id.tvHistoryTotal);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            btnReorder = itemView.findViewById(R.id.btnReorder);
        }
    }
}