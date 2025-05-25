package com.example.musicvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class PeerHostActivity extends AppCompatActivity {
    private TextView hostLog;
    private ScrollView hostScroll;
    private ListView hostListView;
    private Button joinPeerButton;
    private SongAdapter adapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean running = true;
    private VoteDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_host);

        dbHelper = new VoteDBHelper(this);
        hostLog         = findViewById(R.id.hostLog);
        hostScroll      = findViewById(R.id.hostScroll);
        hostListView    = findViewById(R.id.hostListView);
        joinPeerButton  = findViewById(R.id.joinPeerButton);

        List<Song> songs = SongRepository.getSongs();
        adapter = new SongAdapter(this, songs);
        hostListView.setAdapter(adapter);


        joinPeerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PeerClientActivity.class);
            intent.putExtra("username", getIntent().getStringExtra("username"));
            startActivity(intent);
        });

        //start server ect
        new Thread(this::runServer).start();
    }

    private void runServer() {
        try (ServerSocket server = new ServerSocket(8888)) {
            appendLog("Server started on port 8888");
            while (running) {
                Socket client = server.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                String line = reader.readLine();

                if (line != null) {
                    if ("GET_SONGS".equals(line)) {

                        JSONArray arr = new JSONArray();
                        for (Song s : SongRepository.getSongs()) {
                            JSONObject o = new JSONObject();
                            o.put("title", s.getTitle());
                            o.put("artist", s.getArtist());
                            o.put("genre", s.getGenre());
                            arr.put(o);
                        }
                        PrintWriter writer = new PrintWriter(
                                new OutputStreamWriter(client.getOutputStream()), true);
                        writer.println(arr.toString());
                        writer.close();
                        appendLog(getString(R.string.sent_song_list_to_client));
                    } else if (line.startsWith("VOTE:")) {
                        String[] parts = line.split(":");
                        if (parts.length == 3) {
                            String title = parts[1];
                            String voter = parts[2];
                            handler.post(() -> applyVote(title, voter));
                        } else {
                            appendLog(getString(R.string.malformed_vote) + line);
                        }
                    }
                }
                client.close();
            }
        } catch (Exception e) {
            handler.post(() -> {
                appendLog("Server error: " + e.getMessage());
                Toast.makeText(this, String.format("Server error: %s", e.getMessage()), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void applyVote(String title, String voter) {
        for (Song s : SongRepository.getSongs()) {
            if (s.getTitle().equals(title) && !s.hasVoted(voter)) {
                s.vote(voter);
                dbHelper.insertVote(title, s.getArtist(), voter);
                adapter.notifyDataSetChanged();
                appendLog(String.format("Vote for '%s' by %s", title, voter));
                return;
            }
        }
        appendLog(getString(R.string.ignored_duplicate_or_unknown_vote) + title);
    }

    private void appendLog(String text) {
        handler.post(() -> {
            hostLog.append("\n" + text);
            hostScroll.post(() -> hostScroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
