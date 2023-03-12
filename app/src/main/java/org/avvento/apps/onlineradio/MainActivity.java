package org.avvento.apps.onlineradio;

import static org.avvento.apps.onlineradio.services.BackgroundService.ACTION_EXIT;
import static org.avvento.apps.onlineradio.services.BackgroundService.ACTION_PLAY;
import static org.avvento.apps.onlineradio.services.BackgroundService.playbutton;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.vinay.ticker.lib.TickerView;

import org.avvento.apps.onlineradio.services.BackgroundService;
import org.avvento.apps.onlineradio.events.StreamingEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements Serializable {
    private Button streamBtn;
    private EventBus bus = EventBus.getDefault();
    private AvventoRadio radio;
    private Info info;
    private MainActivity mainActivity;
    private AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 100;
    private boolean BtnPlayRadio = true;
    static boolean active = false;
    private static final String ONESIGNAL_APP_ID = "5fe211ea-b161-42c9-8643-20ce8db65da0";
    Context context;

    private void initialise(final boolean playing) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                info = getInfo();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (info == null) {
                            streamBtn.setText(getString(R.string.connect_to_internet));
                            streamBtn.setEnabled(false);
                        } else {
                            startService();
                            streamBtn.setEnabled(true);
                            radio = AvventoRadio.getInstance(info, mainActivity);
                            if (radio.isPlaying()) {
                                streamBtn.setText(getString(R.string.pause_streaming));
                            } else {
                                streamBtn.setText(getString(R.string.start_streaming));
                             }
                            ((TextView) findViewById(R.id.info)).setText(info.getInfo());
                            ((TextView) findViewById(R.id.warning)).setText(info.getWarning());
                            final TickerView tickerView = findViewById(R.id.tickerView);
                            for (int i = 0; i < info.getTicker().length; i++) {
                                TextView tv = new TextView(mainActivity);
                                tv.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                                tv.setText(info.getTicker()[i]);
                                tv.setBackgroundColor(ContextCompat.getColor(mainActivity, android.R.color.white));
                                tv.setTextColor(ContextCompat.getColor(mainActivity, android.R.color.black));
                                tv.setPadding(10, 5, 10, 5);
                                tickerView.addChildView(tv);
                            }
                            tickerView.showTickers();
                            if(!playing) {
                                bus.post(new StreamingEvent(radio));
                            }
                        }
                    }
                });
            }
        }).start();
    }

    public void playAudio(View view) {
        bus.post(new StreamingEvent(radio));
    }

    private Info getInfo() {
        //allow running all on the main thread
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        try {
            java.net.URL url = new URL("https://raw.githubusercontent.com/Muta-Jonathan/AvventoRadio-android-Apk/master/info");
            return new Gson().fromJson(new Scanner(url.openStream()).useDelimiter("\\Z").next(), Info.class);
        } catch(Exception ex) {
            Log.e("AvventoRadio Error!", ex.getMessage());
            return null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStreaming(StreamingEvent streamingEvent){
        if(streamingEvent.getAvventoRadio().isPlaying()) {
            streamingEvent.getAvventoRadio().pause();
            streamBtn.setText(getString(R.string.start_streaming));
        } else {
            streamingEvent.getAvventoRadio().play();
            streamBtn.setText(getString(R.string.pause_streaming));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        streamBtn = findViewById(R.id.audioStreamBtn);
        mainActivity = this;
        initialise(false);
        bus.register(mainActivity);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        //Update feature
        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        mAppUpdateManager.startUpdateFlowForResult(result, AppUpdateType.FLEXIBLE, MainActivity.this
                                , RC_APP_UPDATE);

                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mAppUpdateManager.registerListener(installStateUpdatedListener);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_PLAY);
            registerReceiver(receiver, intentFilter);

            intentFilter.addAction(ACTION_EXIT);
            registerReceiver(receiver, intentFilter);
    }
    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(isConnected()) {
                if (action.equals(ACTION_PLAY) && BtnPlayRadio == true) {
                    //Do whatever you want. Ex. Pause
                    if (playbutton == R.drawable.ic_pause && radio.isPlaying() ) {
                        playbutton = R.drawable.ic_play;
                        if (radio.isPlaying()) {
                            radio.pause();
                            streamBtn.setText(getString(R.string.start_streaming));
                        }
                    } else {
                        playbutton = R.drawable.ic_pause;
                        if (!radio.isPlaying()) {
                            radio.play();
                            streamBtn.setText(getString(R.string.pause_streaming));
                        }
                    }
                }
            }
            if (action.equals(ACTION_EXIT)) {
                stopService();
                System.exit(0);
            }
        }
    };

    private boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void startService() {
        if (!foregroundServiceRunnning()) {
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        stopService(serviceIntent);
    }

    public boolean foregroundServiceRunnning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (BackgroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialise(true);
        active = true;
    }

    @Override
    protected void onDestroy() {
        active = false;
        bus.unregister(mainActivity);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.schedule) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://avventohome.org/avventoradio-schedules")));
            return true;
        } else if(id == R.id.share) {
           // startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://avventohome.org/avvento-radio-ads")));
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/Plain");
            String body = "Share AvventoRadio With Friends";
            String sub = "https://play.google.com/store/apps/details?id=org.avvento.apps.onlineradio&hl=en&gl=US";
            intent.putExtra(Intent.EXTRA_TEXT,body);
            intent.putExtra(Intent.EXTRA_TEXT,sub);
            startActivity(Intent.createChooser(intent,"Share Via"));
            return true;
        } else if(id == R.id.broadcast) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://avventohome.org/previous-broadcasts")));
            return true;
        }
        //removed whatsapp Q/A for admin until renewned
        /* if(id == R.id.whatsapp) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/256784809418")));
            return true;
        } */
        else if(id == R.id.whatsapp_group) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/I6meIZSpogs43FbpAYvsLJ")));
            return true;
        } else if(id == R.id.radio) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://media.avventohome.org")));
            return true;
        } else if(id == R.id.facebook) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/AvventoProductions")));
            return true;
        } else if(id == R.id.youtube) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/avventoProductions")));
            return true;
        } else if(id == R.id.mixcloud) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mixcloud.com/avventoProductions")));
            return true;
        } else if(id == R.id.explore) {
            Intent intent = new Intent(MainActivity.this, Explore.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.prayerRequest) {
            Intent intent = new Intent(MainActivity.this, PrayerRequest.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.exit) {
            finish();
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private InstallStateUpdatedListener installStateUpdatedListener = new InstallStateUpdatedListener()
    {
        @Override
        public void onStateUpdate(InstallState state)
        {
            if(state.installStatus() == InstallStatus.DOWNLOADED)
            {
                showCompletedUpdate();
            }
        }
    };

    private void showCompletedUpdate()
    {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"AvventoRadio app update is ready!",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mAppUpdateManager.completeUpdate();
            }
        });
        snackbar.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
    /* we can check without requestCode == RC_APP_UPDATE because
    we known exactly there is only requestCode from  startUpdateFlowForResult() */
        if(requestCode == RC_APP_UPDATE && resultCode != RESULT_OK)
        {
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop()
    {
        if(mAppUpdateManager!=null) mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop();
    }
}