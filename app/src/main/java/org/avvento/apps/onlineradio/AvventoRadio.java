package org.avvento.apps.onlineradio;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class AvventoRadio {
    private static AvventoRadio instance;
    private MainActivity mainActivity;
    private SimpleExoPlayer player;
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    private AvventoRadio(Info info, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        player = buildPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager = (AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);
        }

        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS:
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        player.setVolume(0.3f);
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        player.setVolume(1.0f);
                        play();
                        break;
                }
            }
        };

        player.setMediaItem(MediaItem.fromUri(info.getUrls().getStream()));
        player.prepare();

        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.setPlayWhenReady(true);
        }
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
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    public void play() {
        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.play();
        }
    }
}
