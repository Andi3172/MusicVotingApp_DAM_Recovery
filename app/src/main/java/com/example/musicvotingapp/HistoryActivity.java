package com.example.musicvotingapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    private TextView historyText;
    private VoteDBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyText = findViewById(R.id.historyText);
        db = new VoteDBHelper(this);

        Button clear = findViewById(R.id.clearHistoryButton);
        clear.setOnClickListener(v -> {
            db.clearAllVotes();
            historyText.setText(R.string.no_history_yet);
            Toast.makeText(this,"Cleared history",Toast.LENGTH_SHORT).show();
        });

        loadHistory();
    }

    private void loadHistory() {
        Cursor c = db.getAllVotes();
        StringBuilder sb = new StringBuilder();
        while (c.moveToNext()) {
            sb.append(c.getString(0))      // title
                    .append(" - ")
                    .append(c.getString(1))      // artist
                    .append(" → ")
                    .append(c.getString(2))      // voter
                    .append("\n");
        }
        c.close();
        if (sb.length()==0) sb.append("No voting history yet.");
        historyText.setText(sb.toString());
    }
}
