package com.example.android_finalexam;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Locale;

public class ListSong extends AppCompatActivity {
    private static final int ADD_SONG_REQUEST = 1;

    private static final int DOUBLE_TAP_DELAY = 600; // Định nghĩa biến DOUBLE_TAP_DELAY
    private ActivityResultLauncher<Intent> editSongLauncher;

    ListView listViewSongs;
    ArrayList<Song> songList;
    ArrayList<Song> filteredSongList;
    SongAdapter songAdapter;
    EditText edtSearch;
    Button btnSearch, btnShowFavorites, btnAddSong;

    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    private GestureDetector gestureDetector;
    private boolean isDoubleTap = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_song);

        listViewSongs = findViewById(R.id.lvsongs);
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnShowFavorites = findViewById(R.id.btnFavorites);
        btnAddSong = findViewById(R.id.btnAdd);

        databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getReadableDatabase();

        songList = new ArrayList<>();
        loadSongsFromDatabase();
        filteredSongList = new ArrayList<>(songList);
        songAdapter = new SongAdapter(ListSong.this, filteredSongList);
        listViewSongs.setAdapter(songAdapter);

        // Register the ActivityResultLauncher
        editSongLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Song updatedSong = (Song) data.getSerializableExtra("updatedSong");
                            if (updatedSong != null) {
                                updateSongInList(updatedSong);
                                songAdapter.notifyDataSetChanged();
                                Toast.makeText(this, "Bài hát đã được cập nhật", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int position = listViewSongs.pointToPosition((int) e.getX(), (int) e.getY());
                if (position != ListView.INVALID_POSITION) {
                    handler.postDelayed(() -> {
                        if (!isDoubleTap) {
                            Song song = filteredSongList.get(position);
                            Intent openMusicPlayer = new Intent(ListSong.this, MainActivity.class);
                            openMusicPlayer.putExtra("song", song);
                            openMusicPlayer.putExtra("currentPosition", position);
                            openMusicPlayer.putExtra("songList", filteredSongList);
                            startActivity(openMusicPlayer);
                        }
                    }, DOUBLE_TAP_DELAY);
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                isDoubleTap = true;
                int position = listViewSongs.pointToPosition((int) e.getX(), (int) e.getY());
                if (position != ListView.INVALID_POSITION) {
                    Song song = filteredSongList.get(position);
                    Intent editSongIntent = new Intent(ListSong.this, EditSong.class);
                    editSongIntent.putExtra("song", song);
                    editSongLauncher.launch(editSongIntent); // Use the ActivityResultLauncher
                }
                handler.postDelayed(() -> isDoubleTap = false, DOUBLE_TAP_DELAY);
                return true;
            }
        });

        listViewSongs.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        btnSearch.setOnClickListener(v -> {
            String keyword = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
            filterSongs(keyword);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase(Locale.getDefault());
                filterSongs(keyword);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnShowFavorites.setOnClickListener(v -> {
            Intent showFavorites = new Intent(ListSong.this, FavoritesSong.class);
            startActivity(showFavorites);
        });

        btnAddSong.setOnClickListener(v -> {
            Intent addSongIntent = new Intent(ListSong.this, AddSong.class);
            startActivityForResult(addSongIntent, ADD_SONG_REQUEST);
        });

        listViewSongs.setOnItemLongClickListener((parent, view, position, id) -> {
            removeSong(position);
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_SONG_REQUEST && resultCode == RESULT_OK) {
            Song newSong = (Song) data.getSerializableExtra("newSong");
            if (newSong != null) {
                songList.add(newSong);
                if (passesFilter(newSong)) {
                    filteredSongList.add(newSong);
                }



                songAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Bài hát đã được thêm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateSongInList(Song updatedSong) {
        for (int i = 0; i < songList.size(); i++) {
            Song song = songList.get(i);
            if (song.getId() == updatedSong.getId()) { // So sánh bằng id
                songList.set(i, updatedSong);
                break;
            }
        }

        for (int i = 0; i < filteredSongList.size(); i++) {
            Song song = filteredSongList.get(i);
            if (song.getId() == updatedSong.getId()) { // So sánh bằng id
                filteredSongList.set(i, updatedSong);
                break;
            }
        }
    }

    private void filterSongs(String keyword) {
        filteredSongList.clear();
        if (keyword.isEmpty()) {
            filteredSongList.addAll(songList);
        } else {
            for (Song song : songList) {
                if (passesFilter(song)) {
                    filteredSongList.add(song);
                }
            }
        }
        songAdapter.notifyDataSetChanged();
    }

    private void removeSong(int position) {
        if (position >= 0 && position < filteredSongList.size()) {
            Song removedSong = filteredSongList.get(position);
            if (databaseHelper.deleteSong(removedSong)) {
                songList.remove(removedSong);
                filteredSongList.remove(position);
                songAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Bài hát đã được xóa", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete song", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSongsFromDatabase() {
        Cursor cursor = db.query(DatabaseHelper.TABLE_SONGS, null, null, null, null, null, null);
        songList.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTIST));
            String imageResource = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_RESOURCE));
            String audioResource = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AUDIO_RESOURCE));
            songList.add(new Song(id, title, artist, imageResource, audioResource));
        }
        cursor.close();
    }

    private boolean passesFilter(Song song) {
        String keyword = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
        return song.getTitle().toLowerCase(Locale.getDefault()).contains(keyword);
    }
}
