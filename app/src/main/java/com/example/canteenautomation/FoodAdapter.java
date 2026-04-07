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
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private final List<FoodModel> foodList;
    private final CartUpdateListener listener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel food = foodList.get(position);

        food.quantity = CartManager.getItemQuantity(food.id);

        holder.name.setText(food.name);
        holder.price.setText("₹" + food.price);

        if (food.imageUrl != null && !food.imageUrl.isEmpty()) {
            holder.foodImage.setVisibility(View.VISIBLE);
            Glide.with(holder.foodImage.getContext())
                    .load(food.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.foodImage);
        } else {
            holder.foodImage.setVisibility(View.GONE);
        }

        if (!food.available) {
            holder.addButton.setVisibility(View.VISIBLE);
            holder.addButton.setEnabled(false);
            holder.addButton.setText("Out of Stock");
            holder.addButton.setBackgroundColor(Color.GRAY);
            holder.quantityLayout.setVisibility(View.GONE);
        } else {
            holder.addButton.setEnabled(true);
            holder.addButton.setText("ADD");
            // UPDATED: Corrected hex code and matched to Forest Green
            int themeColor = androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryColor);

            holder.addButton.setBackgroundColor(themeColor);
            holder.quantityLayout.setBackgroundColor(themeColor);

            if (food.quantity > 0) {
                holder.addButton.setVisibility(View.GONE);
                holder.quantityLayout.setVisibility(View.VISIBLE);
                holder.qtyText.setText(String.valueOf(food.quantity));
            } else {
                holder.addButton.setVisibility(View.VISIBLE);
                holder.quantityLayout.setVisibility(View.GONE);
            }
        }

        holder.addButton.setOnClickListener(v -> {
            food.quantity = 1;
            CartManager.addItem(food);
            syncUI(holder, food);
        });

        holder.plusBtn.setOnClickListener(v -> {
            food.quantity++;
            CartManager.addItem(food);
            syncUI(holder, food);
        });

        holder.minusBtn.setOnClickListener(v -> {
            if (food.quantity > 0) {
                food.quantity--;
                if (food.quantity == 0) {
                    CartManager.removeItem(food);
                } else {
                    CartManager.addItem(food);
                }
                syncUI(holder, food);
            }
        });
    }

    private void syncUI(FoodViewHolder holder, FoodModel food) {
        if (food.quantity > 0) {
            holder.addButton.setVisibility(View.GONE);
            holder.quantityLayout.setVisibility(View.VISIBLE);
            holder.qtyText.setText(String.valueOf(food.quantity));
        } else {
            holder.addButton.setVisibility(View.VISIBLE);
            holder.quantityLayout.setVisibility(View.GONE);
        }

        if (listener != null) {
            listener.onCartUpdated();
        }
    }

    @Override
    public int getItemCount() { return foodList.size(); }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView name, price, qtyText, plusBtn, minusBtn;
        Button addButton;
        LinearLayout quantityLayout;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            name = itemView.findViewById(R.id.foodName);
            price = itemView.findViewById(R.id.foodPrice);
            addButton = itemView.findViewById(R.id.addButton);
            quantityLayout = itemView.findViewById(R.id.quantityLayout);
            qtyText = itemView.findViewById(R.id.qtyText);
            plusBtn = itemView.findViewById(R.id.plusBtn);
            minusBtn = itemView.findViewById(R.id.minusBtn);
        }
    }
}

