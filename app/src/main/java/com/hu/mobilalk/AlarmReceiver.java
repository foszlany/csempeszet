package com.hu.mobilalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String[] randomText = new String[]{"Pörgess kupont!", "Nyerj nagyokat!", "A szerencsejátékosok 99%-a a nagy nyerés előtt lép ki!", "Nyerj kupont a kosaradhoz!", "Nyerj ajándékot!", "ÓRIÁSI LEHETŐSÉG!", "KIHAGYHATATLAN KUPONKERÉK"};
        Random rnd = new Random();
        int index = rnd.nextInt(randomText.length);

        new NotificationHandler(context).send(randomText[index], NotificationHandler.NOTIFICATION_COUPON);
    }
}