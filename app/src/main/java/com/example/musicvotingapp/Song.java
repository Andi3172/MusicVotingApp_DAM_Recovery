package com.example.musicvotingapp;

import java.util.ArrayList;
import java.util.List;

public class Song {
    private String title;
    private String artist;
    private String genre;
    private List<String> voters;

    public Song(String title, String artist, String genre) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.voters = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public List<String> getVoters() {
        return voters;
    }

    public int getVoteCount() {
        return voters.size();
    }

    public void vote(String username) {
        if (!voters.contains(username)) {
            voters.add(username);
        }
    }

    public boolean hasVoted(String username) {
        return voters.contains(username);
    }
}
