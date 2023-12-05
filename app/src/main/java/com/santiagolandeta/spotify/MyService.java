package com.santiagolandeta.spotify;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MyBinder();
    private int currentSongIndex = 0;
    private ArrayList<Song> songList = new ArrayList<>();
    private OnIndexChangeListener onIndexChangeListener;

    public void setOnIndexChangeListener(OnIndexChangeListener listener) {
        this.onIndexChangeListener = listener;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public void playSong(int position) {
        if (position >= 0 && position < songList.size()) {
            currentSongIndex = position;
            stopMusic();
            try {
                mediaPlayer = MediaPlayer.create(this, songList.get(currentSongIndex).getCancion());
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                notifyIndexChange(currentSongIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnIndexChangeListener {
        void onIndexChange(int newIndex);
    }
    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void seekMusic(int progressChangedValue) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progressChangedValue);
        }
    }

    public void procesarDatos(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    public void playMusic() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "No Hay Canciones", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, songList.get(currentSongIndex).getCancion());
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void nextSong() {
        if (currentSongIndex < songList.size() - 1) {
            currentSongIndex++;
        } else {
            currentSongIndex = 0;
        }
        stopMusic();
        playMusic();
        notifyIndexChange(currentSongIndex);
    }

    public void prevSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
        } else {
            currentSongIndex = songList.size() - 1;
        }
        stopMusic();
        playMusic();
        notifyIndexChange(currentSongIndex);
    }
    private void notifyIndexChange(int newIndex) {
        if (onIndexChangeListener != null) {
            onIndexChangeListener.onIndexChange(newIndex);
        }
    }

}
