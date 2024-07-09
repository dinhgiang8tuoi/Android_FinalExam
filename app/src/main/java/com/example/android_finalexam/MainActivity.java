package com.example.android_finalexam;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView tvTimeBegin, tvTimeEnd, tvNamePlaying, tvArtist;
    SeekBar seekBarTime;
    Button btnPlay, btnBack, btnPlayNext, btnPlayPrevious, btnLike;
    MediaPlayer mediaPlayer;
    ImageView ivCover;
    int currentPosition;
    ArrayList<Song> songList;
    Thread updateSeekBarThread;

    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playing);

        songList = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        currentPosition = getIntent().getIntExtra("currentPosition", 0);

        tvTimeBegin = findViewById(R.id.tvTimeBegin);
        tvTimeEnd = findViewById(R.id.tvTimeEnd);
        seekBarTime = findViewById(R.id.seekBarTime);
        btnPlay = findViewById(R.id.btnPlay);
        btnBack = findViewById(R.id.btnBack);
        btnPlayNext = findViewById(R.id.btnPlayNext);
        btnLike = findViewById(R.id.btnLike);
        btnPlayPrevious = findViewById(R.id.btnPlayPrevious);
        tvNamePlaying = findViewById(R.id.tvNamePlaying);
        tvArtist = findViewById(R.id.tvArtist);
        ivCover = findViewById(R.id.ivCover);

        databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getWritableDatabase();

        playSong(currentPosition, true);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlay.setText("Play");
                } else {
                    mediaPlayer.start();
                    btnPlay.setText("Pause");
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setText("Play");
            }
        });

        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                tvTimeBegin.setText(millisecondsToString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                finish();
            }
        });

        btnPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition++;
                if (currentPosition >= songList.size()) {
                    currentPosition = 0;
                }
                playSong(currentPosition, true);
            }
        });

        btnPlayPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition--;
                if (currentPosition < 0) {
                    currentPosition = songList.size() - 1;
                }
                playSong(currentPosition, true);
            }
        });

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSongToFavorites(songList.get(currentPosition));
            }
        });
    }

    private void playSong(int position, boolean autoPlay) {
        Song song = songList.get(position);

        tvNamePlaying.setText(song.getTitle());
        tvArtist.setText(song.getArtist());

        // Set image resource
        int imageResource = getResources().getIdentifier(song.getImageResource(), "drawable", getPackageName());
        ivCover.setImageResource(imageResource);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // Get resource ID of audio file
        int audioResourceId = getResources().getIdentifier(song.getAudioResource(), "raw", getPackageName());

        // Create
        mediaPlayer = MediaPlayer.create(MainActivity.this, audioResourceId);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                tvTimeEnd.setText(millisecondsToString(mp.getDuration()));
                seekBarTime.setMax(mp.getDuration());
                if (autoPlay) {
                    mediaPlayer.start();
                    btnPlay.setText("Pause");
                } else {
                    btnPlay.setText("Play");
                }
                startSeekBarUpdater();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setText("Play");
            }
        });
    }


    private void addSongToFavorites(Song song) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, song.getTitle());
        values.put(DatabaseHelper.COLUMN_ARTIST, song.getArtist());
        values.put(DatabaseHelper.COLUMN_IMAGE_RESOURCE, song.getImageResource());
        values.put(DatabaseHelper.COLUMN_AUDIO_RESOURCE, song.getAudioResource());
        db.insert(DatabaseHelper.TABLE_FAVORITES, null, values);
        Toast.makeText(MainActivity.this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
    }

    private void startSeekBarUpdater() {
        if (updateSeekBarThread != null) {
            updateSeekBarThread.interrupt();
        }

        updateSeekBarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        try {
                            final int current = mediaPlayer.getCurrentPosition();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvTimeBegin.setText(millisecondsToString(current));
                                    seekBarTime.setProgress(current);
                                }
                            });
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        updateSeekBarThread.start();
    }

    private String millisecondsToString(long milliseconds) {
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (updateSeekBarThread != null) {
            updateSeekBarThread.interrupt();
        }
        if (db != null) {
            db.close();
        }
    }
}
