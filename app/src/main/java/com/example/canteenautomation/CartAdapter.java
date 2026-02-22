package com.example.canteenautomation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    List<FoodModel> cartList;
    CartUpdateListener listener;

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(List<FoodModel> cartList, CartUpdateListener listener) {
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

        FoodModel food = cartList.get(position);

        holder.name.setText(food.name);
        holder.price.setText("₹" + (food.price * food.quantity));
        holder.qty.setText(String.valueOf(food.quantity));
        holder.image.setImageResource(food.image);

        holder.plus.setOnClickListener(v -> {
            food.quantity++;
            notifyDataSetChanged();
            listener.onCartUpdated();
        });

        holder.minus.setOnClickListener(v -> {
            if (food.quantity > 1) {
                food.quantity--;
            } else {
                cartList.remove(position);
            }
            notifyDataSetChanged();
            listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name, price, qty, plus, minus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.cartFoodImage);
            name = itemView.findViewById(R.id.cartFoodName);
            price = itemView.findViewById(R.id.cartFoodPrice);
            qty = itemView.findViewById(R.id.cartQty);
            plus = itemView.findViewById(R.id.cartPlus);
            minus = itemView.findViewById(R.id.cartMinus);
        }
    }
}