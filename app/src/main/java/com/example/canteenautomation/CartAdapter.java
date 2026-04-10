package com.example.canteenautomation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final ArrayList<FoodModel> cartList;
    private final OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onQuantityChanged();
    }

    public CartAdapter(ArrayList<FoodModel> cartList, OnCartChangeListener listener) {
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        FoodModel item = cartList.get(position);

        holder.txtName.setText(item.name);

        // ✅ UPDATED: Calculate subtotal (Price * Quantity)
        // Convert the String price to an Integer before multiplying
        int subtotal = Integer.parseInt(item.price) * item.quantity;
        holder.txtPrice.setText("₹" + subtotal);

        holder.txtQuantity.setText(String.valueOf(item.quantity));

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .into(holder.imgFood);
        }

        // ✅ PLUS BUTTON
        holder.btnPlus.setOnClickListener(v -> {
            item.quantity++;
            // Sync with CartManager
            CartManager.addItem(item);
            notifyItemChanged(position);
            if (listener != null) listener.onQuantityChanged();
        });

        // ✅ MINUS BUTTON
        holder.btnMinus.setOnClickListener(v -> {
            if (item.quantity > 1) {
                item.quantity--;
                // Sync with CartManager
                CartManager.addItem(item);
                notifyItemChanged(position);
            } else {
                // Completely remove from both local list and CartManager
                CartManager.removeItem(item);
                cartList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
            }
            if (listener != null) listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPrice, txtQuantity;
        Button btnPlus, btnMinus;
        ImageView imgFood;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}