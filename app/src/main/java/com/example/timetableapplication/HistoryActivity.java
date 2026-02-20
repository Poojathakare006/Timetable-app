package com.example.timetableapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.adapter.HistoryAdapter;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private RecyclerView historyRecyclerView;
    private TextView tvNoHistory;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DBHelper(this);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        tvNoHistory = findViewById(R.id.tvNoHistory);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
    }

    private void loadHistory() {
        ArrayList<TimetableHistory> historyList = dbHelper.getAllTimetableHistory();

        if (historyList.isEmpty()) {
            historyRecyclerView.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            tvNoHistory.setVisibility(View.GONE);
            historyAdapter = new HistoryAdapter(this, historyList);
            historyRecyclerView.setAdapter(historyAdapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
