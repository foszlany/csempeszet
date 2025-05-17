package com.hu.mobilalk;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<ShopItem> mItems;
    private ArrayList<ShopItem> mItemsAll;
    private Context mContext;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    ShopItemAdapter(Context context, ArrayList<ShopItem> items) {
        this.mItems = items;
        this.mItemsAll = items;
        this.mContext = context;

        this.prefs = context.getSharedPreferences("cart", MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    @NonNull
    @Override
    public ShopItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShopItemAdapter.ViewHolder holder, int position) {
        ShopItem current_item = mItems.get(position);

        holder.bindTo(current_item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public Filter getFilter() {
        return shopFilter;
    }

    private Filter shopFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<ShopItem> filtered_list = new ArrayList<>();
            FilterResults res = new FilterResults();

            if(constraint == null || constraint.length() == 0) {
                res.count = mItemsAll.size();
                res.values = mItemsAll;
            }
            else {
                String filter_pattern = constraint.toString().toLowerCase().trim();

                for(ShopItem i : mItemsAll) {
                    if(i.getName().toLowerCase().contains(filter_pattern)) {
                        filtered_list.add(i);
                    }
                }

                res.count = filtered_list.size();
                res.values = filtered_list;
            }

            return res;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mItems = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    };

    // ADD TO CART
    public class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView title;
        private TextView price;
        private TextView desc;
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.cart_item_title);
            price = itemView.findViewById(R.id.cart_item_price);
            desc = itemView.findViewById(R.id.shop_item_desc);
            image = itemView.findViewById(R.id.cart_item_img);

            itemView.findViewById(R.id.shop_item_add_to_cart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String json = prefs.getString("cart", "[]");
                        JSONArray jsonArray = new JSONArray(json);

                        boolean itemExists = false;
                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            if(obj.getString("name").equals(title.getText().toString())) {
                                int quantity = obj.getInt("quantity");
                                obj.put("quantity", quantity + 1);
                                itemExists = true;
                                break;
                            }
                        }

                        if(!itemExists) {
                            JSONObject newItem = new JSONObject();
                            newItem.put("name", title.getText().toString());
                            newItem.put("quantity", 1);
                            jsonArray.put(newItem);
                        }

                        editor.putString("cart", jsonArray.toString());
                        editor.apply();
                    }
                    catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bindTo(ShopItem currentItem) {
            title.setText(currentItem.getName());
            price.setText(String.valueOf(currentItem.getPrice()) + " Ft");
            desc.setText(currentItem.getDesc());
            Glide.with(mContext).load(currentItem.getImage_resource()).into(image);
        }
    }
}