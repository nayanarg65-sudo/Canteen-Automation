package com.example.canteenautomation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.bumptech.glide.Glide;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<FoodModel> cartList;
    private final CartUpdateListener listener;

    // This is the interface your friend's CartActivity expects to "implement"
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
        // Ensure you have an item_cart.xml layout file in your res/layout folder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        FoodModel item = cartList.get(position);

        holder.txtName.setText(item.name);
        holder.txtQuantity.setText(String.valueOf(item.quantity));

        // Calculate item-specific price (Price * Quantity)
        try {
            String cleanPrice = item.price.replace("₹", "").replace("Rs.", "").trim();
            int basePrice = Integer.parseInt(cleanPrice);
            holder.txtPrice.setText("Rs. " + (basePrice * item.quantity));
        } catch (Exception e) {
            holder.txtPrice.setText("Rs. " + item.price);
        }

        // PLUS BUTTON
        holder.btnPlus.setOnClickListener(v -> {
            item.quantity++; // Directly update FoodModel
            notifyItemChanged(position); // Update the list UI
            if (listener != null) {
                listener.onCartUpdated(); // 🚩 This tells CartActivity to update the top Total
            }
        });

        // MINUS BUTTON
        holder.btnMinus.setOnClickListener(v -> {
            if (item.quantity > 1) {
                item.quantity--;
                notifyItemChanged(position);
            } else {
                // If quantity becomes 0, remove from list
                CartManager.getCartItems().remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
            }
            if (listener != null) {
                listener.onCartUpdated(); // 🚩 This tells CartActivity to update the top Total
            }
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