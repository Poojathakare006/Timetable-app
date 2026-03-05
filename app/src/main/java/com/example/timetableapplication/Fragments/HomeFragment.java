package com.example.timetableapplication.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    DBHelper dbHelper;
    TextView tvWelcomeMessage, tvCurrentDate, tvTodayLecturesCount, tvNextClassInfo;
    ProgressBar circularProgress;
    TextView tvAttendancePercentage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DBHelper(getContext());

        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        tvTodayLecturesCount = view.findViewById(R.id.tvTodayLecturesCount);
        tvNextClassInfo = view.findViewById(R.id.tvNextClassInfo);
        circularProgress = view.findViewById(R.id.circularProgress);
        tvAttendancePercentage = view.findViewById(R.id.tvAttendancePercentage);

        loadDashboardInfo();
        loadProgressInfo();

        return view;
    }

    private void loadDashboardInfo() {
        if (getContext() == null) return;

        // Set Current Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        tvCurrentDate.setText(currentDate);

        SharedPreferences preferences = getContext().getSharedPreferences("user_details", MODE_PRIVATE);
        String username = preferences.getString("username", "User");

        tvWelcomeMessage.setText(String.format("Welcome, %s!", username));

        // Real-time data from local SQLite
        String currentDay = new SimpleDateFormat("EEE", Locale.US).format(new Date()).toUpperCase();
        int lectureCount = dbHelper.getLecturesCountForDay(currentDay);
        tvTodayLecturesCount.setText(String.format(Locale.getDefault(), "You have %d lectures today.", lectureCount));

        String currentTime = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        CourseModel nextClass = dbHelper.getNextClass(currentDay, currentTime);

        if (nextClass != null) {
            tvNextClassInfo.setText(String.format("%s at %s", nextClass.getSubjectName(), nextClass.getTimeslot()));
        } else {
            tvNextClassInfo.setText("No more classes today.");
        }
    }

    private void loadProgressInfo() {
        if (getContext() == null) return;

        // Real-time attendance data from SQLite
        int attendance = dbHelper.getAttendancePercentage();
        circularProgress.setProgress(attendance);
        tvAttendancePercentage.setText(String.format(Locale.getDefault(), "%d%%", attendance));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh dashboard every time the user returns to the home screen
        loadDashboardInfo();
        loadProgressInfo();
    }
}
