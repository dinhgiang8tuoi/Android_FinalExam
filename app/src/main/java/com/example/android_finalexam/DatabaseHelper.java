package com.example.android_finalexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "songs_database";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_FAVORITES = "favorites";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_IMAGE_RESOURCE = "image_resource";
    public static final String COLUMN_AUDIO_RESOURCE = "audio_resource";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSongsTable = "CREATE TABLE " + TABLE_SONGS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_ARTIST + " TEXT, " +
                COLUMN_IMAGE_RESOURCE + " TEXT, " +
                COLUMN_AUDIO_RESOURCE + " TEXT)";
        db.execSQL(createSongsTable);

        String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_ARTIST + " TEXT, " +
                COLUMN_IMAGE_RESOURCE + " TEXT, " +
                COLUMN_AUDIO_RESOURCE + " TEXT)";
        db.execSQL(createFavoritesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public boolean deleteSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = { song.getTitle() }; // Sử dụng tiêu đề của bài hát để xóa

        int deletedRows = db.delete(TABLE_SONGS, COLUMN_TITLE + " = ?", whereArgs);
        db.close();

        return deletedRows > 0;
    }

    public boolean updateSong(int id, String newTitle, String newArtist, String newImageResource, String newAudioResource) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, newTitle);
        contentValues.put(COLUMN_ARTIST, newArtist);
        contentValues.put(COLUMN_IMAGE_RESOURCE, newImageResource);
        contentValues.put(COLUMN_AUDIO_RESOURCE, newAudioResource);

        int rowsAffected = db.update(TABLE_SONGS, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();

        return rowsAffected > 0;
    }

}


