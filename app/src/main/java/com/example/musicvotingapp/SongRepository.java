package com.example.musicvotingapp;

import java.util.ArrayList;
import java.util.List;

public class SongRepository {
    private static final List<Song> SONGS = new ArrayList<>();
    public static void setSongs(List<Song> songs) {
        SONGS.clear();
        SONGS.addAll(songs);
    }
    public static List<Song> getSongs() {
        return SONGS;
    }
}
