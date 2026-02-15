package com.example.timetableapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TimetableHistoryAdapter extends RecyclerView.Adapter<TimetableHistoryAdapter.ViewHolder> {

    private ArrayList<TimetableHistory> historyList;
    private OnHistoryItemClickListener listener;
    private Context context;

    public interface OnHistoryItemClickListener {
        void onViewClick(TimetableHistory history);
        void onShareClick(TimetableHistory history);
        void onDeleteClick(TimetableHistory history);
    }

    public TimetableHistoryAdapter(Context context, ArrayList<TimetableHistory> historyList, OnHistoryItemClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
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
        holder.tvCourseName.setText(history.getCourseName());

        // Format the date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault());
        String dateString = sdf.format(new Date(history.getCreatedAt()));
        holder.tvDate.setText("Created on: " + dateString);

        // Calculate and display file size
        File file = new File(history.getPdfPath());
        if (file.exists()) {
            long length = file.length();
            holder.tvFileSize.setText("Size: " + formatFileSize(length));
        } else {
            holder.tvFileSize.setText("Size: N/A");
        }

        holder.btnView.setOnClickListener(v -> listener.onViewClick(history));
        holder.btnShare.setOnClickListener(v -> listener.onShareClick(history));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(history));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvDate, tvFileSize;
        Button btnView, btnShare, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvHistoryCourseName);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvFileSize = itemView.findViewById(R.id.tvHistoryFileSize);
            btnView = itemView.findViewById(R.id.btnViewPdf);
            btnShare = itemView.findViewById(R.id.btnSharePdf);
            btnDelete = itemView.findViewById(R.id.btnDeletePdf);
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
