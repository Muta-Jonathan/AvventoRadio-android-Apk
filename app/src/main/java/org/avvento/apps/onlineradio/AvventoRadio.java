package org.avvento.apps.onlineradio;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class AvventoRadio {
    private static AvventoRadio instance;
    private MainActivity mainActivity;
    private SimpleExoPlayer player;

    private AvventoRadio(Info info, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        player = buildPlayer();
        player.setMediaItem(MediaItem.fromUri(info.getUrls().getStream()));
        player.prepare();
        player.setPlayWhenReady(true);
    }

    public static AvventoRadio getInstance(Info info, MainActivity mainActivity) {
        if(instance == null) {
            synchronized (AvventoRadio.class) {
                instance = new AvventoRadio(info, mainActivity);
            }
        }
        return instance;
    }

    private SimpleExoPlayer buildPlayer() {
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        builder.setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS, 60000, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(mainActivity).setLoadControl(builder.build()).build();
        return player;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pause() {
        player.pause();
    }

    public void play() {
        player.play();
    }
}
