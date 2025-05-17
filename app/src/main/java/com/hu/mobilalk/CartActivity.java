package com.hu.mobilalk;

import static com.hu.mobilalk.NotificationHandler.NOTIFICATION_CART;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private ArrayList<ShopItem> mItemList;
    private CollectionReference mItems;
    private CartItemAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private NotificationHandler mNotificationHandler;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UNAUTHENTICATED USER
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
        }

        prefs = this.getSharedPreferences("cart", MODE_PRIVATE);
        editor = prefs.edit();

        mRecyclerView = findViewById(R.id.cart_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        mItems = FirebaseFirestore.getInstance().collection("Items");

        mItemList = new ArrayList<>();
        mAdapter = new CartItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNotificationHandler = new NotificationHandler(this);

        queryData();
    }

    private void queryData() {
        mItemList.clear();

        mItems.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                String json = prefs.getString("cart", "[]");
                try {
                    JSONArray jsonArray = new JSONArray(json);

                    for(QueryDocumentSnapshot document : task.getResult()) {
                        ShopItem item = document.toObject(ShopItem.class);
                        boolean itemExists = false;
                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            if(obj.getString("name").equals(item.getName())) {
                                int quantity = obj.getInt("quantity");
                                obj.put("quantity", quantity + 1);
                                itemExists = true;
                                break;
                            }
                        }

                        if(itemExists) {
                            mItemList.add(item);
                        }
                    }
                }
                catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        mAdapter.notifyDataSetChanged();
    }

    // MENU BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shop_menu_cart, menu);
        return true;
    }

    // MENU STUFF
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.shop_logout_alt) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.shop_home) {
            Intent intent = new Intent(CartActivity.this, ShopActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        mNotificationHandler.cancel(NOTIFICATION_CART);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CartActivity.this, ShopActivity.class);
        startActivity(intent);
    }
}