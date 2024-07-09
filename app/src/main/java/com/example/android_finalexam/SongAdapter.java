package com.example.android_finalexam;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<Song> {

    private static class ViewHolder {
        TextView titleTextView;
        TextView artistTextView;
        ImageView imageView;
    }

    private DatabaseHelper databaseHelper;

    public SongAdapter(@NonNull Context context, @NonNull ArrayList<Song> objects) {
        super(context, 0, objects);
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song, parent, false);
            viewHolder.titleTextView = convertView.findViewById(R.id.song_title);
            viewHolder.artistTextView = convertView.findViewById(R.id.song_artist);
            viewHolder.imageView = convertView.findViewById(R.id.song_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Song song = getItem(position);
        if (song != null) {
            viewHolder.titleTextView.setText(song.getTitle());
            viewHolder.artistTextView.setText(song.getArtist());

            // Load image from resources based on image name
            String imageName = song.getImageResource();
            int imageResourceId = getImageResourceId(imageName);
            viewHolder.imageView.setImageResource(imageResourceId);

            // Load audio from resources based on audio name
            String audioName = song.getAudioResource();
            int audioResourceId = getAudioResourceId(audioName);
            // Example: MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), audioResourceId);
        }

        return convertView;
    }

    private int getImageResourceId(String imageName) {
        Resources resources = getContext().getResources();
        return resources.getIdentifier(imageName, "drawable", getContext().getPackageName());
    }

    private int getAudioResourceId(String audioName) {
        Resources resources = getContext().getResources();
        return resources.getIdentifier(audioName, "raw", getContext().getPackageName());
    }
}
