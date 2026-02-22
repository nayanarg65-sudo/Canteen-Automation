package com.example.canteenautomation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodModel> foodList;
    private CartUpdateListener listener;

    // 🔥 Interface for updating cart bar
    public interface CartUpdateListener {
        void onCartUpdated();
    }

    public FoodAdapter(List<FoodModel> foodList, CartUpdateListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {

        FoodModel food = foodList.get(position);

        holder.foodName.setText(food.name);
        holder.foodPrice.setText("₹" + food.price);
        holder.foodImage.setImageResource(food.image);

        updateUI(holder, food);

        // ADD
        holder.addButton.setOnClickListener(v -> {
            food.quantity = 1;
            CartManager.updateCart(food);
            updateUI(holder, food);
            if (listener != null) listener.onCartUpdated();
        });

        // PLUS
        holder.plusBtn.setOnClickListener(v -> {
            food.quantity++;
            CartManager.updateCart(food);
            holder.qtyText.setText(String.valueOf(food.quantity));
            if (listener != null) listener.onCartUpdated();
        });

        // MINUS
        holder.minusBtn.setOnClickListener(v -> {

            if (food.quantity > 1) {
                food.quantity--;
            } else {
                food.quantity = 0;
            }

            CartManager.updateCart(food);
            updateUI(holder, food);
            if (listener != null) listener.onCartUpdated();
        });
    }

    private void updateUI(FoodViewHolder holder, FoodModel food) {
        if (food.quantity == 0) {
            holder.addButton.setVisibility(View.VISIBLE);
            holder.quantityLayout.setVisibility(View.GONE);
        } else {
            holder.addButton.setVisibility(View.GONE);
            holder.quantityLayout.setVisibility(View.VISIBLE);
            holder.qtyText.setText(String.valueOf(food.quantity));
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {

        ImageView foodImage;
        TextView foodName, foodPrice;
        Button addButton;
        LinearLayout quantityLayout;
        TextView plusBtn, minusBtn, qtyText;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);

            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);

            addButton = itemView.findViewById(R.id.addButton);
            quantityLayout = itemView.findViewById(R.id.quantityLayout);
            plusBtn = itemView.findViewById(R.id.plusBtn);
            minusBtn = itemView.findViewById(R.id.minusBtn);
            qtyText = itemView.findViewById(R.id.qtyText);
        }
    }
}