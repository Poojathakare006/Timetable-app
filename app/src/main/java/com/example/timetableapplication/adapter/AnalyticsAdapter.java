package com.example.timetableapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.ModelClass.SubjectAnalytics;
import com.example.timetableapplication.R;

import java.util.List;

public class AnalyticsAdapter extends RecyclerView.Adapter<AnalyticsAdapter.ViewHolder> {

    private final List<SubjectAnalytics> analyticsList;

    public AnalyticsAdapter(List<SubjectAnalytics> analyticsList) {
        this.analyticsList = analyticsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_analytics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectAnalytics analytics = analyticsList.get(position);
        holder.subjectName.setText(analytics.getSubjectName());
        holder.attendancePercentage.setText(String.format("%d%%", analytics.getAttendancePercentage()));
        holder.progressBar.setProgress(analytics.getAttendancePercentage());
    }

    @Override
    public int getItemCount() {
        return analyticsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectName;
        TextView attendancePercentage;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.tv_subject_name);
            attendancePercentage = itemView.findViewById(R.id.tv_attendance_percentage);
            progressBar = itemView.findViewById(R.id.progress_bar_attendance);
        }
    }
}
