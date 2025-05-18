package com.hu.mobilalk;

import static com.hu.mobilalk.NotificationHandler.NOTIFICATION_CART;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    protected ArrayList<ShopItem> mItemList;
    private CollectionReference mItems;
    private CartItemAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private NotificationHandler mNotificationHandler;
    static SharedPreferences prefs_cart;
    static SharedPreferences prefs_coupon;
    SharedPreferences.Editor editor_cart;
    SharedPreferences.Editor editor_coupon;
    private static TextView totalText;

    private static int total;

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

        findViewById(R.id.cart_checkout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(CartActivity.this)
                        .setTitle("Sikeres rendelés!")
                        .setMessage("Megrendelése sikeres.\n\n" + (total == 0 ? "A semmit majd digitálisan átküldjük, számlát igény szerint adunk.\n(De azért fizetni is kell)" : "Köszönjük, hogy velünk csempészett!"))
                        .setPositiveButton("Ok", (d, which) -> {})
                        .setCancelable(true)
                        .create();
                dialog.show();
            }
        });

        totalText = findViewById(R.id.cart_total);
        totalText.setText("0 Ft");

        prefs_cart = this.getSharedPreferences("cart", MODE_PRIVATE);
        editor_cart = prefs_cart.edit();

        prefs_coupon = this.getSharedPreferences("coupon", MODE_PRIVATE);
        editor_coupon = prefs_coupon.edit();

        mRecyclerView = findViewById(R.id.cart_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        mItems = FirebaseFirestore.getInstance().collection("Items");

        mItemList = new ArrayList<>();
        mAdapter = new CartItemAdapter(this, mItemList, this);
        mRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNotificationHandler = new NotificationHandler(this);

        total = 0;

        queryData();
    }

    private void queryData() {
        mItemList.clear();
        String json = prefs_cart.getString("cart", "[]");

        try {
            JSONArray jsonArray = new JSONArray(json);
            ArrayList<String> cartItemNames = new ArrayList<>();

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                cartItemNames.add(obj.getString("name"));
            }

            // CART IS EMPTY
            if(cartItemNames.isEmpty()) {
                mAdapter.notifyDataSetChanged();
                total = 0;
                updateTotalUI();
                return;
            }

            // QUERY CART ITEMS
            mItems.whereIn("name", cartItemNames).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    total = 0;
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        ShopItem item = document.toObject(ShopItem.class);
                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj;
                            try {
                                obj = jsonArray.getJSONObject(i);
                                if(obj.getString("name").equals(item.getName())) {
                                    int quantity = obj.getInt("quantity");
                                    item.setQuantity(quantity);
                                    mItemList.add(item);
                                    total += item.getPrice() * quantity;
                                    break;
                                }
                            }
                            catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    updateTotalUI();
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // UPDATE TOTAL (W/ COUPONS)
    private static void updateTotalUI() {
        String coupon = prefs_coupon.getString("coupon", "1");

        double couponValue = Double.parseDouble(coupon);
        if(couponValue == 1.0 || total == 0) {
            totalText.setText(total + " Ft");
        }
        else {
            totalText.setText(total + " Ft helyett csak " + (int)Math.round(total * couponValue) + " Ft!");
        }
    }
    protected static void updateTotalUIParam(int n) {
        total += n;
        updateTotalUI();
    }

    // REMOVE ELEMENT UPON
    protected void removeElement(int el) {
        mItemList.remove(el);
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
        else if(id == R.id.shop_profile) {
            Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
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

    // PREVENT USER FROM ABUSING THE GAMBLING FEATURE
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CartActivity.this, ShopActivity.class);
        startActivity(intent);
    }

    // SEND NOTIFICATION WHEN PUT ON TRAY TO ANNOY THE USER
    @Override
    protected void onUserLeaveHint() {
        mNotificationHandler.send("Koppintson ide a kosár megtekintéséhez.", NOTIFICATION_CART);
        super.onUserLeaveHint();
    }
}