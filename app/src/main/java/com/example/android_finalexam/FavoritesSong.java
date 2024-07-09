package com.example.android_finalexam;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FavoritesSong extends AppCompatActivity {

    ListView listViewFavorites;
    ArrayList<Song> favoriteSongsList;
    SongAdapter songAdapter;

    Button btnBack;

    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_song);

        listViewFavorites = findViewById(R.id.lvFavoritesSong);
        btnBack = findViewById(R.id.btnBack);

        databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getReadableDatabase();
        loadFavoriteSongs();

        songAdapter = new SongAdapter(FavoritesSong.this, favoriteSongsList);
        listViewFavorites.setAdapter(songAdapter);

        listViewFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = favoriteSongsList.get(position);
                Intent openMusicPlayer = new Intent(FavoritesSong.this, MainActivity.class);
                openMusicPlayer.putExtra("song", song);
                openMusicPlayer.putExtra("currentPosition", position);
                openMusicPlayer.putExtra("songList", favoriteSongsList); // Gửi danh sách bài hát yêu thích
                startActivity(openMusicPlayer);
            }
        });

        listViewFavorites.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeFavoriteSong(position);
                return true;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Kết thúc Activity hiện tại để quay lại Activity trước đó (ListSong)
            }
        });

    }

    private void loadFavoriteSongs() {
        favoriteSongsList = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FAVORITES, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTIST));
            String imageResource = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_RESOURCE));
            String audioResource = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AUDIO_RESOURCE));
            favoriteSongsList.add(new Song(id, title, artist, imageResource, audioResource));
        }
        cursor.close();
    }

    private void removeFavoriteSong(int position) {
        if (position >= 0 && position < favoriteSongsList.size()) {
            Song songToRemove = favoriteSongsList.get(position);
            favoriteSongsList.remove(position);
            songAdapter.notifyDataSetChanged();
            db.delete(DatabaseHelper.TABLE_FAVORITES, DatabaseHelper.COLUMN_TITLE + "=?", new String[]{songToRemove.getTitle()});
            Toast.makeText(this, "Đã xóa bài hát yêu thích", Toast.LENGTH_SHORT).show();
        }
    }
}
