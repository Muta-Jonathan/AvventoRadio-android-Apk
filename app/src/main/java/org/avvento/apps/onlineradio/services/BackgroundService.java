package org.avvento.apps.onlineradio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import org.avvento.apps.onlineradio.MainActivity;
import org.avvento.apps.onlineradio.R;

public class BackgroundService extends Service {

    private static final String TAG = "bac_ground";
    private MediaSessionCompat mediaSession;
    public static final String ACTION_PLAY = "action-play";
    public static final String ACTION_EXIT = "action-exit";

    public static int playbutton =  R.drawable.ic_pause;
    private boolean BtnPlayRadio = true;


    @Override
        public void onCreate() {
            super.onCreate();
        mediaSession = new MediaSessionCompat(this,"tag");

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ACTION_PLAY);
        registerReceiver(receiver,intentFilter);

        intentFilter.addAction(ACTION_EXIT);
        registerReceiver(receiver,intentFilter);
        }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_PLAY) && BtnPlayRadio == true){
                //Do whatever you want. Ex. Pause
               if (playbutton == R.drawable.ic_pause){
                   onStartCommand(intent,0,0);
               }else {
                   onStartCommand(intent, 0, 0);
               }
            }


            if(action.equals(ACTION_EXIT)){
                //Do whatever you want. Ex. Exit
                unregisterReceiver(receiver);
                stopSelf();
               // Toast.makeText(BackgroundService.this, "Playing in background stopped", Toast.LENGTH_SHORT).show();
            }
            //Similarly this can be done for all actions
        }};

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            String id = "AvventoRadio_player_id1";

            Bitmap artwork = BitmapFactory.decodeResource(getResources(),R.drawable.logo);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationChannel channel = manager.getNotificationChannel(id);
                if(channel ==null)
                {
                    channel = new NotificationChannel(id,"AvventoRadio Player", NotificationManager.IMPORTANCE_LOW);
                    //config nofication channel
                    channel.setDescription("Listen to AvventoRadio 24|7");
                    channel.enableVibration(true);
                    channel.setVibrationPattern(new long[]{50,500,100,170});
                    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    manager.createNotificationChannel(channel);
                }
            }

            Intent pauseIntent = new Intent(ACTION_PLAY);
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, 0);

            Intent exitIntent = new Intent(ACTION_EXIT);
            PendingIntent exitPendingIntent = PendingIntent.getBroadcast(this, 1, exitIntent, 0);


            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id)
                    .setSmallIcon(R.drawable.logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                    .addAction(playbutton, "play", pausePendingIntent)
                    .addAction(R.drawable.ic_exit, "exit", exitPendingIntent)
                    // Take advantage of MediaStyle features
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0)
                            .setShowCancelButton(true)
                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                            .setMediaSession(mediaSession.getSessionToken()))
                    .setSubText("media")
                    .setContentTitle("AvventoRadio Live")
                    .setContentText("Thank you for listening")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setVibrate(new long[]{100,1000,200,340})
                    .setAutoCancel(false)//true touch on notificaiton menu dismissed, but swipe to dismiss
                    .setTicker("Notification");
            builder.setContentIntent(contentIntent);
            NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
            //id to generate new notification in list notifications menu
            //m.notify(new Random().nextInt(),builder.build());
            startForeground(1, builder.build());

            //do heavy work on a background thread
           // stopSelf();
            Log.d(TAG, "onStartCommand: running");
            return START_NOT_STICKY;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
}
