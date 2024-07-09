// AddSong.java

package com.example.android_finalexam;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddSong extends AppCompatActivity {

    EditText edtTitle, edtArtist, edtNameImg, edtNameAudio;
    Button btnAddSong, btnCancel;

    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    private boolean isUpdateMode = false;
    private Song songToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_song);

        edtTitle = findViewById(R.id.edtTitle);
        edtArtist = findViewById(R.id.edtArtist);
        edtNameImg = findViewById(R.id.edtNameImg);
        btnAddSong = findViewById(R.id.btnAddSong);
        btnCancel = findViewById(R.id.btnCancel);
        edtNameAudio = findViewById(R.id.edtNameAudio);

        btnAddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString();
                String artist = edtArtist.getText().toString();
                String imgName = edtNameImg.getText().toString();
                String audioName = edtNameAudio.getText().toString();

                if (title.isEmpty() || artist.isEmpty() || imgName.isEmpty() || audioName.isEmpty()) {
                    Toast.makeText(AddSong.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveSongToDatabase(title, artist, imgName, audioName);

                Toast.makeText(AddSong.this, "Song added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getWritableDatabase();
    }

    private void saveSongToDatabase(String title, String artist, String imageFileName, String audioFileName) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, title);
        values.put(DatabaseHelper.COLUMN_ARTIST, artist);
        values.put(DatabaseHelper.COLUMN_IMAGE_RESOURCE, imageFileName); // Lưu tên tệp hình ảnh
        values.put(DatabaseHelper.COLUMN_AUDIO_RESOURCE, audioFileName); // Lưu tên tệp âm thanh

        long newRowId = db.insert(DatabaseHelper.TABLE_SONGS, null, values);
        if (newRowId != -1) {
            Song newSong = new Song((int) newRowId, title, artist, imageFileName, audioFileName);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newSong", newSong);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Song saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save song", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
