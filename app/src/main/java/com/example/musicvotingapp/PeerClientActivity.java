package com.example.musicvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PeerClientActivity extends AppCompatActivity {
    private EditText hostIpInput;
    private ListView songListView;
    private Button joinSessionButton;
    private Button confirmButton;
    private String username = "Anonymous";
    private List<Song> songs = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int selectedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_client);

        hostIpInput         = findViewById(R.id.hostIpInput);
        joinSessionButton   = findViewById(R.id.joinSessionButton);
        songListView        = findViewById(R.id.songListView);
        confirmButton       = findViewById(R.id.confirmVoteButton);

        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        }

        confirmButton.setEnabled(false);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_single_choice,
                new ArrayList<>()
        );
        songListView.setAdapter(adapter);
        songListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        songListView.setOnItemClickListener((parent, view, pos, id) -> {
            selectedIndex = pos;
            confirmButton.setEnabled(true);
        });

        joinSessionButton.setOnClickListener(v -> {
            String ip = hostIpInput.getText().toString().trim();
            if (ip.isEmpty()) {
                Toast.makeText(this, "Enter host IP to join session", Toast.LENGTH_SHORT).show();
            } else {
                fetchSongList(ip);
            }
        });

        confirmButton.setOnClickListener(v -> {
            if (selectedIndex < 0) {
                Toast.makeText(this, "Select a song first", Toast.LENGTH_SHORT).show();
            } else {
                String ip = hostIpInput.getText().toString().trim();
                String title = songs.get(selectedIndex).getTitle();
                sendVote(ip, title);
            }
        });
    }

    private void fetchSongList(String ip) {
        new Thread(() -> {
            try (Socket socket = new Socket(ip, 8888)) {
                PrintWriter w = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true);
                w.println("GET_SONGS");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                String json = reader.readLine();

                JSONArray arr = new JSONArray(json);
                songs.clear();
                List<String> titles = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    Song s = new Song(
                            o.getString("title"),
                            o.getString("artist"),
                            o.optString("genre", "")
                    );
                    songs.add(s);
                    titles.add(s.getTitle());
                }

                runOnUiThread(() -> {
                    adapter.clear();
                    adapter.addAll(titles);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Joined session – song list loaded", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("PeerClient", "Fetch error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void sendVote(String ip, String songTitle) {
        new Thread(() -> {
            try (Socket socket = new Socket(ip, 8888)) {
                PrintWriter w = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true);
                w.println("VOTE:" + songTitle + ":" + username);
                runOnUiThread(() ->
                        Toast.makeText(this, "Voted for \"" + songTitle + "\"", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                Log.e("PeerClient", "Vote error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error sending vote: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
