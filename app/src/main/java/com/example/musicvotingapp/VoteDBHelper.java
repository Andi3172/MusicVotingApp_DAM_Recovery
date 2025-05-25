package com.example.musicvotingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VoteDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "music_voting.db";
    private static final int DB_VERSION = 1;

    public VoteDBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // A single table for individual votes
        db.execSQL("CREATE TABLE votes (" +
                "id   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title  TEXT NOT NULL," +
                "artist TEXT NOT NULL," +
                "voter  TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldV,int newV) {
        db.execSQL("DROP TABLE IF EXISTS votes");
        onCreate(db);
    }

    public void insertVote(String title, String artist, String voter) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title",  title);
        cv.put("artist", artist);
        cv.put("voter",  voter);
        db.insert("votes", null, cv);
    }

    public Cursor getAllVotes() {
        return getReadableDatabase()
                .rawQuery("SELECT title,artist,voter FROM votes ORDER BY id DESC", null);
    }

    public void clearAllVotes() {
        getWritableDatabase().delete("votes", null, null);
    }
}
