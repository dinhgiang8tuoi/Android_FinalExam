package com.example.android_finalexam;

import java.io.Serializable;

public class Song implements Serializable {

    private int id;
    private String title;
    private String artist;
    private String imageResource;
    private String audioResource;

    public Song(int id, String title, String artist, String imageResource, String audioResource) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.imageResource = imageResource;
        this.audioResource = audioResource;
    }

    public int getId() {return id; }
    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageResource() {
        return imageResource;
    }

    public String getAudioResource() {
        return audioResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    public void setAudioResource(String audioResource) {
        this.audioResource = audioResource;
    }
}
