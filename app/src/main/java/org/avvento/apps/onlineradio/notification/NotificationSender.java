package org.avvento.apps.onlineradio.notification;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.multidex.MultiDex;

public class NotificationSender extends Application {
    public static final String CHANNEL_1_ID = "AvventoRadio_Updates";
    public static final String CHANNEL_2_ID = "AvventoRadio_Announcements";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "AvventoRadio_Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("AvventoRadio Updates notifications");
            channel1.enableVibration(true);
            channel1.setVibrationPattern(new long[]{100, 1000, 200, 340});
            channel1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "AvventoRadio_Announcements",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("AvventoRadio Announcements notifications");
            channel1.enableVibration(true);
            channel1.setVibrationPattern(new long[]{100, 1000, 200, 340});
            channel1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);


        }

    }

}