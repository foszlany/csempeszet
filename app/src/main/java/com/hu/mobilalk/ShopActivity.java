package com.hu.mobilalk;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShopActivity.class.getName();

    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerView;
    private ArrayList<ShopItem> mItemList;
    private ShopItemAdapter mAdapter;

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

        initializeData();
    }

    private void initializeData() {
        String[] item_names = getResources().getStringArray(R.array.shop_item_names);
        String[] item_descriptions = getResources().getStringArray(R.array.shop_item_descriptions);
        String[] item_prices = getResources().getStringArray(R.array.shop_item_prices);
        TypedArray item_images = getResources().obtainTypedArray(R.array.shop_item_images);

        for(int i = 0; i < item_names.length; i++) {
            mItemList.add(new ShopItem(item_names[i], item_descriptions[i], Integer.parseInt(item_prices[i]), item_images.getResourceId(i, 0)));
        }

        item_images.recycle();

        mAdapter.notifyDataSetChanged();
    }


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
        if(R.id.shop_logout == item.getItemId()) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        }
        else if(R.id.shop_setting == item.getItemId()) {
            return true;
        }
        else if(R.id.shop_cart == item.getItemId()) {
            return true;
        }
        else if(R.id.shop_search_bar == item.getItemId()) {
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
}