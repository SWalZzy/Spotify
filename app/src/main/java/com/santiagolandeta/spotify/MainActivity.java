package com.santiagolandeta.spotify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MyService.OnIndexChangeListener, SongAdapter.OnItemClickListener{

    private MyService myService;
    private boolean isBound = false;
    private SeekBar seekBar;
    private ScheduledExecutorService scheduledExecutorService;
    private boolean isPlaying = false;
    private ArrayList<Song> songList;
    private SongAdapter songAdapter;
    private TextView tvNombreCancion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songList = new ArrayList<>();
        songAdapter = new SongAdapter(songList, this);
        RecyclerView recyclerView = findViewById(R.id.rvListado);
        recyclerView.setAdapter(songAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(recyclerView.getChildAdapterPosition(view));
            }
        });

        Intent intent = new Intent(this, MyService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        ImageButton playButton = findViewById(R.id.btnPlay);
        ImageButton prevButton = findViewById(R.id.btnPrevious);
        ImageButton nextButton = findViewById(R.id.btnNext);


        songList.add(new Song("Chillwave", "Kevin", "ChillWave", R.drawable.chillwave, R.raw.chillwave));
        songList.add(new Song("Darkhaunts", "ManuelG", "DarkHaunts", R.drawable.darkhaunts, R.raw.darkhaunts));
        songList.add(new Song("Forest", "Kevin", "Forest", R.drawable.forest, R.raw.forest));
        songList.add(new Song("PlanetEarth", "LuisJ", "PlanetEarth", R.drawable.planetearth, R.raw.planetearth));
        songList.add(new Song("Town", "Kevin", "Town", R.drawable.town, R.raw.town));

        seekBar = findViewById(R.id.seekBar);


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusic(view);
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevSong(view);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong(view);
            }
        });

        startSeekBarUpdateTimer();

    }
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.MyBinder binder = (MyService.MyBinder) iBinder;
            myService = binder.getService();
            isBound = true;

            myService.procesarDatos(songList);
            myService.setOnIndexChangeListener(MainActivity.this);
            onIndexChange(0);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    public void playMusic(View view) {
        if (isBound) {
            if (isPlaying) {
                myService.pauseMusic();
            } else {
                myService.playMusic();
                tvNombreCancion.setSelected(true);
            }
            isPlaying = !isPlaying;
            updatePlayPauseButton();
        }
    }

    private void updatePlayPauseButton() {
        ImageButton playButton = findViewById(R.id.btnPlay);
        if (isPlaying) {
            playButton.setImageResource(R.drawable.pause);
        } else {
            playButton.setImageResource(R.drawable.play);
        }
    }

    public void nextSong(View view) {
        if (isBound) {
            myService.nextSong();
        }
    }

    public void prevSong(View view) {
        if (isBound) {
            myService.prevSong();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        stopSeekBarUpdateTimer();
    }

    @Override
    public void onIndexChange(int newIndex) {

        tvNombreCancion = findViewById(R.id.tvInfoCancion);
        ImageView ivImagenCancion = findViewById(R.id.ivReproductor);

        tvNombreCancion.setText(songList.get(newIndex).Concatenar());
        tvNombreCancion.setSelected(true);
        ivImagenCancion.setImageResource(songList.get(newIndex).getImagen());
            seekBar.setProgress(0);
            int duration = myService.getDuration();
            seekBar.setMax(duration);
            startSeekBarUpdateTimer();
            updateTiempoTranscurridoRestante(0, myService.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i;
                if (isBound) {
                    myService.seekMusic(progressChangedValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (isBound) {
                    myService.pauseMusic();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isBound) {
                    myService.seekMusic(progressChangedValue);
                    myService.playMusic();
                }
            }
        });

    }

    private void updateTiempoTranscurridoRestante(int tiempoTranscurrido, int duracionTotal) {
        TextView tvTiempoTranscurrido = findViewById(R.id.tvTiempoInicio);
        TextView tvTiempoRestante = findViewById(R.id.tvTiempoFinal);

        int minutosTranscurridos = tiempoTranscurrido / 60000;
        int segundosTranscurridos = (tiempoTranscurrido % 60000) / 1000;

        int minutosRestantes = (duracionTotal - tiempoTranscurrido) / 60000;
        int segundosRestantes = ((duracionTotal - tiempoTranscurrido) % 60000) / 1000;

        String tiempoTranscurridoStr = String.format("%02d:%02d", minutosTranscurridos, segundosTranscurridos);
        String tiempoRestanteStr = String.format("%02d:%02d", minutosRestantes, segundosRestantes);

        tvTiempoTranscurrido.setText(tiempoTranscurridoStr);
        tvTiempoRestante.setText(tiempoRestanteStr);
    }

    public void startSeekBarUpdateTimer() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void stopSeekBarUpdateTimer() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }
    public void updateSeekBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isBound && myService != null) {
                    int currentPosition = myService.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    updateTiempoTranscurridoRestante(currentPosition, myService.getDuration());
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        if (isBound) {
            myService.playSong(position);
        }
    }
}
