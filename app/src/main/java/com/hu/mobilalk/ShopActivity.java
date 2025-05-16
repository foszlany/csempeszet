package com.hu.mobilalk;

import static com.hu.mobilalk.NotificationHandler.NOTIFICATION_CART;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class ShopActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShopActivity.class.getName();

    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerView;
    private ArrayList<ShopItem> mItemList;
    private ShopItemAdapter mAdapter;

    private FirebaseFirestore mFireStore;
    private CollectionReference mItems;

    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;

    private final int grid_number = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shop);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        
        // UNAUTHENTICATED USER
        if(user == null) {
            finish();
        }

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, grid_number));

        mItemList = new ArrayList<>();
        mAdapter = new ShopItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFireStore = FirebaseFirestore.getInstance();
        mItems = mFireStore.collection("Items");

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        queryData();
        initializeData();
        setAlarmManager();
    }

    private void queryData() {
        mItemList.clear();
        mItems.orderBy("name").limit(20).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                mItemList.add(doc.toObject(ShopItem.class));
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    private void initializeData() {
        String[] item_names = getResources().getStringArray(R.array.shop_item_names);
        String[] item_descriptions = getResources().getStringArray(R.array.shop_item_descriptions);
        String[] item_prices = getResources().getStringArray(R.array.shop_item_prices);
        TypedArray item_images = getResources().obtainTypedArray(R.array.shop_item_images);

        /*
        for(int i = 0; i < item_names.length; i++) {
            ShopItem shopItem = new ShopItem(
                    item_names[i],
                    item_descriptions[i],
                    Integer.parseInt(item_prices[i]),
                    item_images.getResourceId(i, 0)
            );

            mItems.add(shopItem)
                    .addOnSuccessListener(documentReference -> {
                        Log.i(LOG_TAG, "ADDED DATA: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "ERROR ADDING DATA: ", e);
                    });
        }
        */

        item_images.recycle();
    }

    String[] bankNames={"BOI","SBI","HDFC","PNB","OBC"};
    // MENU BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shop_menu, menu);

        MenuItem menu_item = menu.findItem(R.id.shop_search_bar);
        SearchView search_view = (SearchView) menu_item.getActionView();
        assert search_view != null;
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.shop_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        }
        else if (id == R.id.shop_filter) {
            View menuItemView = findViewById(R.id.shop_filter);
            PopupMenu popup = initPopup(menuItemView);
            popup.show();
            return true;
        }
        else if (id == R.id.shop_cart) {
            return true;
        }
        else if (id == R.id.shop_search_bar) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private PopupMenu initPopup(View menuItemView) {
        PopupMenu popup = new PopupMenu(this, menuItemView);

        popup.inflate(R.menu.shop_filter_menu);
        popup.setOnMenuItemClickListener(menuItem -> {
            int menuId = menuItem.getItemId();

            if (menuId == R.id.menu_filter_all) {

            }
            else if (menuId == R.id.shop_filter_pricehl) {

            }
            else if (menuId == R.id.shop_filter_pricelh) {

            }
            return true;
        });
        return popup;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void setAlarmManager() {
        long repeatInterval = 60;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CART, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, pendingIntent);

        // mAlarmManager.cancel(pendingIntent);
    }

    // SEND NOTIFICATION WHEN PUT ON TRAY TO ANNOY THE USER
    @Override
    protected void onUserLeaveHint() {
        mNotificationHandler.send("Koppintson ide a kosár megtekintéséhez.", NOTIFICATION_CART);
        super.onUserLeaveHint();
    }

    // PUT IN CART
    @Override
    protected void onResume() {
        mNotificationHandler.cancel(NOTIFICATION_CART);
        super.onResume();
    }
}