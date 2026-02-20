package com.example.timetableapplication.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.R;
import com.example.timetableapplication.adapter.HistoryAdapter;

import java.util.ArrayList;

public class ManageFragment extends Fragment {

    private DBHelper dbHelper;
    private RecyclerView historyRecyclerView;
    private TextView tvNoHistory;
    private HistoryAdapter historyAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);

        dbHelper = new DBHelper(getContext());
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void loadHistory() {
        if(getContext() == null) return;
        
        ArrayList<TimetableHistory> historyList = dbHelper.getAllTimetableHistory();

        if (historyList.isEmpty()) {
            historyRecyclerView.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            tvNoHistory.setVisibility(View.GONE);
            historyAdapter = new HistoryAdapter(getContext(), historyList);
            historyRecyclerView.setAdapter(historyAdapter);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list every time the user comes to this screen
        loadHistory();
    }
}
