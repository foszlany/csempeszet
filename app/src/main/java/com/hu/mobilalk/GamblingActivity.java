package com.hu.mobilalk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.Random;

public class GamblingActivity extends AppCompatActivity {
    private NotificationHandler mNotificationHandler;

    private HorizontalScrollView scrollView;
    private LinearLayout itemsContainer;
    private final String[] items = {"Vesztett!", "-5%", "Vesztett!", "-10%", "Vesztett!", "-15%", "Vesztett!", "-20%", "Vesztett!", "-25%", "Vesztett!", "-30%", "Vesztett!", "-40%", "Vesztett!", "-50%", "Vesztett!"};
    private final int ITEM_WIDTH_DP = 100;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambling);

        // UNAUTHENTICATED USER
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            finish();
        }

        prefs = this.getSharedPreferences("coupon", MODE_PRIVATE);
        editor = prefs.edit();

        scrollView = findViewById(R.id.scrollView);
        itemsContainer = findViewById(R.id.itemsContainer);

        setupWheelItems();

        // KILL INPUT
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        // BUTTON EVENT
        Button spinButton = findViewById(R.id.spinButton);
        spinButton.setOnClickListener(v -> {
            spinButton.setEnabled(false);
            startSpinAnimation();
        });

        mNotificationHandler = new NotificationHandler(this);
        mNotificationHandler.cancel(1);
    }

    // ADD COUPONS
    private void setupWheelItems() {
        int itemWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ITEM_WIDTH_DP, getResources().getDisplayMetrics());

        for(int i = 0; i < 40; i++) {
            for(String item : items) {
                TextView coupon = new TextView(this);
                coupon.setPadding(0, 0, 0, 0);
                coupon.setLayoutParams(new LinearLayout.LayoutParams(itemWidthPx, ViewGroup.LayoutParams.MATCH_PARENT));
                coupon.setGravity(Gravity.CENTER);
                coupon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                coupon.setTextColor(Color.BLACK);
                coupon.setText(item);
                itemsContainer.addView(coupon);
            }
        }
    }

    // SPIN DA WHEEL
    private void startSpinAnimation() {
        int itemWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ITEM_WIDTH_DP, getResources().getDisplayMetrics());
        int currentScroll = scrollView.getScrollX();
        int visibleWidth = scrollView.getWidth();

        // DETERMINE WINNING FIELD
        Random random = new Random();
        int targetIndex = random.nextInt(items.length);
        int targetPosition = currentScroll +
                (5 * items.length * itemWidthPx) +
                (targetIndex * itemWidthPx) +
                (visibleWidth / 2);

        // ANIMATION
        ValueAnimator animator = ValueAnimator.ofInt(currentScroll, targetPosition);
        animator.setDuration(5000);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.addUpdateListener(animation ->
                scrollView.scrollTo((int) animation.getAnimatedValue(), 0)
        );
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.spinButton).setEnabled(true);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                int itemWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ITEM_WIDTH_DP, getResources().getDisplayMetrics());
                int visibleWidth = scrollView.getWidth();
                int finalPosition = scrollView.getScrollX();

                int adjustedPosition = finalPosition + (visibleWidth / 2 - itemWidthPx / 2);
                int selectedIndex = Math.round(adjustedPosition / (float) itemWidthPx) % items.length;
                if(selectedIndex < 0) {
                    selectedIndex += items.length;
                }

                // APPLY COUPON
                if(!"Vesztett!".equals(items[selectedIndex])) {
                    String numStr = items[selectedIndex].substring(1, items[selectedIndex].length() - 1);
                    String multiplierStr = String.valueOf(1 - (Double.parseDouble(numStr) / 100));

                    editor.putString("coupon", multiplierStr);
                    editor.apply();
                }

                showResult(items[selectedIndex]);
            }
        });
        animator.start();
    }

    // POPUP
    private void showResult(String result) {
        mNotificationHandler.cancel(1);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Végeredmény")
                .setMessage(Objects.equals(result, "Vesztett!") ? result : "NYERTÉL: " + result)
                .setPositiveButton("Ok", (d, which) -> {
                    toCart();
                })
                .setCancelable(true)
                .create();

        dialog.setOnDismissListener(d -> {
            toCart();
        });

        dialog.show();
    }

    // GO TO CART ACTIVITY
    private void toCart() {
        Intent intent = new Intent(GamblingActivity.this, CartActivity.class);
        startActivity(intent);
    }
}