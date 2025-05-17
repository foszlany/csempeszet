package com.hu.mobilalk;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<ShopItem> mItems;
    private Context mContext;

    CartItemAdapter(Context context, ArrayList<ShopItem> items) {
        this.mItems = items;
        this.mContext = context;
    }

    @NonNull
    @Override
    public CartItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.cart_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemAdapter.ViewHolder holder, int position) {
        ShopItem current_item = mItems.get(position);

        holder.bindTo(current_item);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView title;
        private TextView price;
        private ImageView image;
        private TextView quantityText;
        private Button btnPlus, btnMinus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.cart_item_title);
            price = itemView.findViewById(R.id.cart_item_price);
            image = itemView.findViewById(R.id.cart_item_img);
            quantityText = itemView.findViewById(R.id.cart_item_quantity);
            btnPlus = itemView.findViewById(R.id.cart_item_btn_plus);
            btnMinus = itemView.findViewById(R.id.cart_item_btn_minus);
        }

        @SuppressLint("SetTextI18n")
        public void bindTo(ShopItem currentItem) {
            title.setText(currentItem.getName());
            price.setText(String.valueOf(currentItem.getPrice()) + " Ft");
            Glide.with(mContext).load(currentItem.getImage_resource()).into(image);

            btnPlus.setOnClickListener(v -> {
                int quantity = Integer.parseInt(quantityText.getText().toString());
                quantityText.setText(String.valueOf(quantity + 1));
            });

            btnMinus.setOnClickListener(v -> {
                int quantity = Integer.parseInt(quantityText.getText().toString());
                if(quantity > 1) {
                    quantityText.setText(String.valueOf(quantity - 1));
                }
            });
        }
    }
}