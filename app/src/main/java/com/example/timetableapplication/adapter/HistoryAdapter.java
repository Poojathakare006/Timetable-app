package com.example.timetableapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<TimetableHistory> historyList;
    private final DBHelper dbHelper;

    public HistoryAdapter(Context context, List<TimetableHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timetable_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableHistory history = historyList.get(position);
        holder.courseName.setText(history.getCourseName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(history.getCreatedAt()));
        holder.date.setText("Created on: " + dateString);

        File file = new File(history.getPdfPath());
        if (file.exists()) {
            String fileSize = Formatter.formatShortFileSize(context, file.length());
            holder.fileSize.setText("Size: " + fileSize);
        } else {
            holder.fileSize.setText("Size: N/A");
        }

        holder.btnView.setOnClickListener(v -> viewPdf(history));
        holder.btnShare.setOnClickListener(v -> sharePdf(history));
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(history, position));
    }

    private void viewPdf(TimetableHistory history) {
        File file = new File(history.getPdfPath());
        if (file.exists()) {
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "No application found to open PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File not found. It may have been deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdf(TimetableHistory history) {
        File file = new File(history.getPdfPath());
        if (file.exists()) {
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share PDF"));
            } catch (Exception e) {
                Toast.makeText(context, "No application found to share PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File not found. It may have been deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(final TimetableHistory history, final int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete this PDF? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteFile(history, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFile(TimetableHistory history, int position) {
        // 1. Delete from device storage
        File file = new File(history.getPdfPath());
        boolean fileDeleted = false;
        if (file.exists()) {
            fileDeleted = file.delete();
        }

        // 2. Delete from local SQLite database
        dbHelper.deleteTimetableHistory(history.getHistoryId());

        // 3. Remove from RecyclerView
        historyList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, historyList.size());

        if(fileDeleted) {
             Toast.makeText(context, "File deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
             Toast.makeText(context, "Record deleted from database.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, date, fileSize;
        Button btnView, btnShare, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.tvHistoryCourseName);
            date = itemView.findViewById(R.id.tvHistoryDate);
            fileSize = itemView.findViewById(R.id.tvHistoryFileSize);
            btnView = itemView.findViewById(R.id.btnViewPdf);
            btnShare = itemView.findViewById(R.id.btnSharePdf);
            btnDelete = itemView.findViewById(R.id.btnDeletePdf);
        }
    }
}
