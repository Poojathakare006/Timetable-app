package com.example.timetableapplication.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.R;
import com.example.timetableapplication.adapter.TimetableHistoryAdapter;

import java.io.File;
import java.util.ArrayList;

public class ManageFragment extends Fragment implements TimetableHistoryAdapter.OnHistoryItemClickListener {

    DBHelper dbHelper;
    RecyclerView recyclerView;
    TimetableHistoryAdapter adapter;
    TextView tvNoHistory;
    ArrayList<TimetableHistory> historyList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);

        dbHelper = new DBHelper(getContext());
        recyclerView = view.findViewById(R.id.historyRecyclerView);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        historyList = dbHelper.getAllTimetableHistory();
        if (historyList.isEmpty()) {
            tvNoHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoHistory.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new TimetableHistoryAdapter(getContext(), historyList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onViewClick(TimetableHistory history) {
        File file = new File(history.getPdfPath());
        if (file.exists()) {
            Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No PDF viewer installed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "File not found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShareClick(TimetableHistory history) {
        File file = new File(history.getPdfPath());
        if (file.exists()) {
            Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Timetable PDF"));
        } else {
            Toast.makeText(getContext(), "File not found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(TimetableHistory history) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Timetable")
                .setMessage("Are you sure you want to delete this timetable?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete from database
                    dbHelper.deleteTimetableHistory(history.getId());

                    // Delete the file
                    File file = new File(history.getPdfPath());
                    if (file.exists()) {
                        file.delete();
                    }

                    // Refresh the list
                    loadHistory();
                    Toast.makeText(getContext(), "Timetable deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
