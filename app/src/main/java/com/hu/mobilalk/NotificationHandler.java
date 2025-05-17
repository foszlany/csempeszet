package com.hu.mobilalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {
    private static final String CHANNEL_ID = "shop_notification_channel";
    public static final int NOTIFICATION_CART = 0;
    public static final int NOTIFICATION_COUPON = 1;

    private NotificationManager mManager;
    private Context mContext;

    NotificationHandler(Context context) {
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notifications", NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Notifications for CSEMPESZET");

        this.mManager.createNotificationChannel(channel);
    }

    public void send(String msg, int ID) {
        Intent intent;
        if(ID == NOTIFICATION_CART) {
            intent = new Intent(mContext, CartActivity.class);
        }
        else if(ID == NOTIFICATION_COUPON) {
            intent = new Intent(mContext, GamblingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }
        else {
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("Csempeszet")
                .setContentText(msg)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent);

        this.mManager.notify(ID, builder.build());
    }

    public void cancel(int ID) {
        this.mManager.cancel(ID);
    }
}