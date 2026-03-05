package com.example.timetableapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private final Context context;
    private final List<CourseModel> timetableList;
    private final OnItemClickListener listener;
    private final int[] colors;

    public interface OnItemClickListener {
        void onItemClick(CourseModel course);
    }

    public TimetableAdapter(Context context, List<CourseModel> timetableList, int[] colors, OnItemClickListener listener) {
        this.context = context;
        this.timetableList = timetableList;
        this.colors = colors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timetable_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseModel course = timetableList.get(position);

        holder.tvSubject.setText(course.getSubjectName().toUpperCase());
        
        // Add brackets only if not Recess or Free
        String subject = course.getSubjectName().toLowerCase();
        if (subject.contains("recess") || subject.contains("free")) {
            holder.tvTeacher.setText("");
        } else {
            holder.tvTeacher.setText("(" + course.getTeacherName() + ")");
        }
        
        holder.tvTime.setText(formatTimeSlot(course.getTimeslot()));

        // Status Symbol
        String status = course.getStatus();
        if ("Attended".equals(status)) {
            holder.tvStatus.setText("✅");
        } else if ("Skipped".equals(status)) {
            holder.tvStatus.setText("❌");
        } else {
            holder.tvStatus.setText("");
        }

        // Apply Theme Color
        if (subject.contains("recess") || subject.contains("free")) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF9C4"));
        } else if (colors != null && colors.length > 0) {
            int colorIndex = Math.abs(course.getSubjectName().hashCode()) % colors.length;
            holder.cardView.setCardBackgroundColor(colors[colorIndex]);
        }

        // Highlight Ongoing
        if (isCurrentLecture(course)) {
            holder.tvNowBadge.setVisibility(View.VISIBLE);
            holder.viewAccentStrip.setVisibility(View.VISIBLE);
            holder.cardView.setCardElevation(8f);
        } else {
            holder.tvNowBadge.setVisibility(View.GONE);
            holder.viewAccentStrip.setVisibility(View.GONE);
            holder.cardView.setCardElevation(2f);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(course));
    }

    private String formatTimeSlot(String rawTime) {
        try {
            String clean = rawTime.toLowerCase().replace(" to ", "-").replace(" ", "");
            String[] parts = clean.split("-");
            if (parts.length < 2) return rawTime;
            return formatSingleTime(parts[0]) + " – " + formatSingleTime(parts[1]);
        } catch (Exception e) {
            return rawTime;
        }
    }

    private String formatSingleTime(String time) {
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("hh:mm", Locale.US);
            if (time.contains("am") || time.contains("pm")) {
                inFormat = new SimpleDateFormat("hh:mma", Locale.US);
            }
            Date date = inFormat.parse(time);
            SimpleDateFormat outFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            return outFormat.format(date);
        } catch (Exception e) {
            return time.toUpperCase();
        }
    }

    private boolean isCurrentLecture(CourseModel course) {
        try {
            Calendar now = Calendar.getInstance();
            String currentDay = new SimpleDateFormat("EEE", Locale.US).format(now.getTime()).toUpperCase();
            if (!course.getDay().equalsIgnoreCase(currentDay)) return false;

            String timeslot = course.getTimeslot().toLowerCase().replace(" to ", "-").replace(" ", "");
            String[] parts = timeslot.split("-");
            if (parts.length < 2) return false;

            int startMin = convertToMinutes(parts[0]);
            int endMin = convertToMinutes(parts[1]);
            if (endMin <= startMin) endMin += 12 * 60;

            int currentMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            return currentMin >= startMin && currentMin < endMin;
        } catch (Exception e) {
            return false;
        }
    }

    private int convertToMinutes(String timeStr) {
        int offset = 0;
        if (timeStr.contains("pm") && !timeStr.startsWith("12")) offset = 12 * 60;
        if (timeStr.contains("am") && timeStr.startsWith("12")) offset = -12 * 60;
        String cleanTime = timeStr.replaceAll("[^0-9:]", "");
        String[] hm = cleanTime.split(":");
        int h = Integer.parseInt(hm[0]);
        int m = (hm.length > 1) ? Integer.parseInt(hm[1]) : 0;
        return h * 60 + m + offset;
    }

    @Override
    public int getItemCount() {
        return timetableList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvSubject, tvTeacher, tvStatus, tvNowBadge;
        View viewAccentStrip;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvCardTime);
            tvSubject = itemView.findViewById(R.id.tvCardSubject);
            tvTeacher = itemView.findViewById(R.id.tvCardTeacher);
            tvStatus = itemView.findViewById(R.id.tvCardStatus);
            tvNowBadge = itemView.findViewById(R.id.tvNowBadge);
            viewAccentStrip = itemView.findViewById(R.id.viewAccentStrip);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
