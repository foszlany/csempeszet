package com.hu.mobilalk;

import static android.content.Context.MODE_PRIVATE;

// import static com.hu.mobilalk.CartActivity.mItemList;
import static com.hu.mobilalk.CartActivity.updateTotalUIParam;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<ShopItem> mItems;
    private static Context mContext;
    static SharedPreferences prefs;
    static SharedPreferences.Editor editor;
    private CartActivity cartActivity;

    CartItemAdapter(Context context, ArrayList<ShopItem> items, CartActivity cartActivity) {
        this.mItems = items;
        mContext = context;

        prefs = context.getSharedPreferences("cart", MODE_PRIVATE);
        editor = prefs.edit();

        this.cartActivity = cartActivity;
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
        return mItems.size();
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
            quantityText.setText(String.valueOf(currentItem.getQuantity()));

            // ADD TO QUANTITY
            btnPlus.setOnClickListener(v -> {
                int quantity = currentItem.getQuantity() + 1;
                currentItem.setQuantity(quantity);
                quantityText.setText(String.valueOf(quantity));
                updateCartQuantity(currentItem.getName(), quantity);
                updateTotalUIParam(currentItem.getPrice());
            });

            // REMOVE FROM QUANTITY
            btnMinus.setOnClickListener(v -> {
                int newQuantity = currentItem.getQuantity() - 1;

                if(newQuantity >= 1) {
                    currentItem.setQuantity(newQuantity);
                    quantityText.setText(String.valueOf(newQuantity));
                }
                else {
                    int pos = getAdapterPosition();
                    cartActivity.removeElement(pos);
                    notifyItemRemoved(pos);
                }

                updateTotalUIParam(currentItem.getPrice() * -1);
                updateCartQuantity(currentItem.getName(), newQuantity);
            });
        }

        private void updateCartQuantity(String itemName, int newQuantity) {
            String json = prefs.getString("cart", "[]");
            try {
                JSONArray jsonArray = new JSONArray(json);

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    if(obj.getString("name").equals(itemName)) {
                        if(newQuantity >= 1) {
                            obj.put("quantity", newQuantity);
                        }
                        else {
                            jsonArray.remove(i);
                        }
                        editor.putString("cart", jsonArray.toString());
                        editor.apply();
                        break;
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}