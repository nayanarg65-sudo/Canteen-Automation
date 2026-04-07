package com.example.canteenautomation;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HomeFoodAdapter extends RecyclerView.Adapter<HomeFoodAdapter.ViewHolder> {

    List<FoodModel> list;

    public HomeFoodAdapter(List<FoodModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodModel model = list.get(position);

        // Set Basic Data
        holder.foodName.setText(model.name);
        holder.foodPrice.setText("₹" + model.price);

        // Image Loading
        if (model.imageUrl != null && !model.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(model.imageUrl)
                    .placeholder(R.drawable.fullfood)
                    .into(holder.foodImage);
        } else {
            holder.foodImage.setImageResource(R.drawable.fullfood);
        }

        // Logic for Availability and Cart State
        updateButtonUI(holder, model);

        // Click Listeners
        holder.btnAdd.setOnClickListener(v -> {
            // Only allow adding if it is available
            if (model.available) {
                model.quantity = 1;
                CartManager.addItem(model);
                notifyItemChanged(position);
            }
        });

        holder.plusBtn.setOnClickListener(v -> {
            model.quantity++;
            CartManager.addItem(model);
            notifyItemChanged(position);
        });

        holder.minusBtn.setOnClickListener(v -> {
            if (model.quantity > 0) {
                model.quantity--;
                if (model.quantity == 0) {
                    CartManager.removeItem(model);
                } else {
                    CartManager.addItem(model);
                }
                notifyItemChanged(position);
            }
        });
    }

    private void updateButtonUI(ViewHolder holder, FoodModel model) {
        // Get the quantity from CartManager
        int quantity = CartManager.getItemQuantity(model.id);

        if (!model.available) {
            // 1. OUT OF STOCK STATE (Matches FoodAdapter logic)
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setText("Out of Stock");
            holder.btnAdd.setBackgroundColor(Color.GRAY);
            holder.quantityLayout.setVisibility(View.GONE);
        } else {
            // 2. AVAILABLE STATE
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setText("ADD");

            // Set your primary theme color back
            int themeColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryColor);
            holder.btnAdd.setBackgroundColor(themeColor);

            if (quantity > 0) {
                // Item is in cart
                holder.btnAdd.setVisibility(View.GONE);
                holder.quantityLayout.setVisibility(View.VISIBLE);
                holder.qtyText.setText(String.valueOf(quantity));
            } else {
                // Item is not in cart
                holder.btnAdd.setVisibility(View.VISIBLE);
                holder.quantityLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName, foodPrice, qtyText, plusBtn, minusBtn;
        Button btnAdd;
        LinearLayout quantityLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            qtyText = itemView.findViewById(R.id.qtyText);
            plusBtn = itemView.findViewById(R.id.plusBtn);
            minusBtn = itemView.findViewById(R.id.minusBtn);
            btnAdd = itemView.findViewById(R.id.addButton);
            quantityLayout = itemView.findViewById(R.id.quantityLayout);
        }
    }
}