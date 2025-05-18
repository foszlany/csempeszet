package com.hu.mobilalk;

import static com.hu.mobilalk.NotificationHandler.NOTIFICATION_CART;
import static com.hu.mobilalk.NotificationHandler.NOTIFICATION_COUPON;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {
    private FirebaseUser user;

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

        mNotificationHandler.cancel(NOTIFICATION_CART);

        // GET PERMISSION FOR NOTIFICATIONS
        if(Build.VERSION.SDK_INT >= 33) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("Értesítések")
                        .setMessage("Ez azért kell, hogy tudjunk értesítéseket küldeni.\n\nDurva, mi?")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                    666);
                        })
                        .setNegativeButton("Kihagyom", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }

        queryData();
        initializeData();
        setAlarmManager();
    }

    private void queryData() {
        mItemList.clear();
        mItems.orderBy("name").limit(20).get().addOnSuccessListener(queryDocumentSnapshots -> {
            mItemList.clear();

            for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                mItemList.add(doc.toObject(ShopItem.class));
            }

            mAdapter.notifyDataSetChanged();

            slideAnim();
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

    // MENU STUFF
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.shop_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ShopActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.shop_filter) {
            View menuItemView = findViewById(R.id.shop_filter);
            PopupMenu popup = initPopup(menuItemView);
            popup.show();
            return true;
        }
        else if(id == R.id.shop_cart) {
            Intent intent = new Intent(ShopActivity.this, CartActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // FILTERS (SORTING / QUERIES)
    @NonNull
    private PopupMenu initPopup(View menuItemView) {
        PopupMenu popup = new PopupMenu(this, menuItemView);

        popup.inflate(R.menu.shop_filter_menu);
        popup.setOnMenuItemClickListener(menuItem -> {
            int menuId = menuItem.getItemId();
            Query query = mItems;

            if(menuId == R.id.menu_filter_all) {
                query = mItems.limit(20);
            }
            if(menuId == R.id.shop_filter_az) {
                query = mItems.orderBy("name", Query.Direction.ASCENDING).limit(20);
            }
            if(menuId == R.id.shop_filter_za) {
                query = mItems.orderBy("name", Query.Direction.DESCENDING).limit(20);
            }
            else if(menuId == R.id.shop_filter_pricehl) {
                query = mItems.orderBy("price", Query.Direction.DESCENDING).limit(20);
            }
            else if(menuId == R.id.shop_filter_pricelh) {
                query = mItems.orderBy("price", Query.Direction.ASCENDING).limit(20);
            }

            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                mItemList.clear();

                for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    mItemList.add(doc.toObject(ShopItem.class));
                }

                mAdapter.notifyDataSetChanged();

                slideAnim();
            });

            return true;
        });
        return popup;
    }

    private void slideAnim() {
        Animation slideFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_n_fade);
        mRecyclerView.startAnimation(slideFadeIn);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    // COUPON NOTIFICATION SETUP
    private void setAlarmManager() {
        long repeatInterval = 1;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_COUPON, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, pendingIntent);
    }

    // SEND NOTIFICATION WHEN PUT ON TRAY TO ANNOY THE USER
    @Override
    protected void onUserLeaveHint() {
        mNotificationHandler.send("Koppintson ide a kosár megtekintéséhez.", NOTIFICATION_CART);
        super.onUserLeaveHint();
    }
}