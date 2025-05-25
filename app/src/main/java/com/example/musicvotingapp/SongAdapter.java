// SongAdapter.java
package com.example.musicvotingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;
import java.util.List;

public class SongAdapter extends ArrayAdapter<Song> {
    public SongAdapter(Context ctx, List<Song> songs) {
        super(ctx, 0, songs);
    }

    @NonNull @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_song, parent, false);
        }

        Song s = getItem(pos);

        TextView titleTv     = convertView.findViewById(R.id.songTitle);
        TextView artistTv    = convertView.findViewById(R.id.songArtist);
        TextView genreTv     = convertView.findViewById(R.id.songGenre);
        TextView voteCountTv = convertView.findViewById(R.id.songVotes);

        titleTv.setText(s.getTitle());
        artistTv.setText(s.getArtist());
        genreTv.setText(s.getGenre());
        voteCountTv.setText(String.valueOf(s.getVoteCount()));

        return convertView;
    }
}
