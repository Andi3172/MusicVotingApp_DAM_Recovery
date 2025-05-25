package com.example.musicvotingapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;


import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {
    private BarChart chart;
    private Button reloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        chart = findViewById(R.id.barChart);
        reloadButton = findViewById(R.id.reloadChartButton);

        reloadButton.setOnClickListener(v -> drawChart());

        drawChart();
    }

    private void drawChart() {
        List<Song> songList = SongRepository.getSongs();  // <— live data

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < songList.size(); i++) {
            entries.add(new BarEntry(i, songList.get(i).getVoteCount()));
            labels.add(songList.get(i).getTitle());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Votes");
        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getDescription().setText("Vote Results");

        chart.invalidate();
    }
}
