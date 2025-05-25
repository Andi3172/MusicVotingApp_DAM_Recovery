package com.example.musicvotingapp;

import static com.example.musicvotingapp.R.*;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Song> songList;
    private SongAdapter adapter;
    private String username;
    private String role;
    private Handler handler = new Handler(Looper.getMainLooper());
    private VoteDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new VoteDBHelper(this);

        username = getIntent().getStringExtra("username");
        role     = getIntent().getStringExtra("role");

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText(String.format("Welcome, %s! Role: %s", username, role));

        songList = loadSongsFromJson();
        SongRepository.setSongs(songList);

        ListView listView = findViewById(R.id.songListView);
        //adapter = new SongAdapter(this, songList, role.equals("Voter") ? username : null);
        adapter = new SongAdapter(this, songList);
        listView.setAdapter(adapter);

        findViewById(R.id.viewHistoryButton).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.viewChartButton).setOnClickListener(v ->
                startActivity(new Intent(this, ChartActivity.class)));
        findViewById(R.id.saveWinnerButton).setOnClickListener(v -> saveTopWinner());

        if ("Host".equals(role)) {
            findViewById(R.id.startPeerButton).setOnClickListener(v ->
                    Toast.makeText(this, "Hosting session… clients can connect", Toast.LENGTH_SHORT).show());
            findViewById(R.id.startClientButton).setVisibility(Button.GONE);
            startServerSocket();
        } else {
            findViewById(R.id.startPeerButton).setVisibility(Button.GONE);
            findViewById(R.id.startClientButton).setOnClickListener(v -> {
                Intent i = new Intent(this, PeerClientActivity.class);
                i.putExtra("username", username);
                startActivity(i);
            });
        }
    }

    private ArrayList<Song> loadSongsFromJson() {
        ArrayList<Song> list = new ArrayList<>();
        try {
            InputStream is = getResources().openRawResource(R.raw.songs);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String title  = obj.getString("title");
                String artist = obj.getString("artist");
                String genre  = obj.optString("genre", "");
                boolean exists = false;
                for (Song s: list) {
                    if (s.getTitle().equals(title) && s.getArtist().equals(artist)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) list.add(new Song(title, artist, genre));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading songs: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return list;
    }

    private void saveTopWinner() {
        if (songList.isEmpty()) return;
        Song top = songList.get(0);
        for (Song s: songList) {
            if (s.getVoteCount() > top.getVoteCount()) top = s;
        }
        if (top.getVoteCount() > 0) {
            for (String voter: top.getVoters()) {
                dbHelper.insertVote(top.getTitle(), top.getArtist(), voter);
            }
            Toast.makeText(this, getString(R.string.saved_winner) + top.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.no_votes_to_save, Toast.LENGTH_SHORT).show();
        }
    }

    private void startServerSocket() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(8888)) {
                while (true) {
                    Socket client = server.accept();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    String line = reader.readLine();

                    if (line != null) {
                        if ("GET_SONGS".equals(line)) {
                            PrintWriter writer = new PrintWriter(
                                    new OutputStreamWriter(client.getOutputStream()), true);
                            JSONArray arr = new JSONArray();
                            for (Song s: songList) {
                                JSONObject o = new JSONObject();
                                o.put("title", s.getTitle());
                                o.put("artist", s.getArtist());
                                o.put("genre", s.getGenre());
                                arr.put(o);
                            }
                            writer.println(arr.toString());
                        } else if (line.startsWith(getString(string.vote))) {
                            String[] p = line.split(":");
                            if (p.length == 3) {
                                handler.post(() -> applyVote(p[1], p[2]));
                            }
                        }
                    }
                    client.close();
                }
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this,
                        getString(string.server_error) + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void applyVote(String title, String voter) {
        for (Song s: songList) {
            if (s.getTitle().equals(title) && !s.hasVoted(voter)) {
                s.vote(voter);
                dbHelper.insertVote(s.getTitle(), s.getArtist(), voter);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }
}
