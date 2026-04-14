package com.example.timetableapplication.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private DBHelper dbHelper;
    private TextView tvWelcomeMessage, tvCurrentDate, tvTodayLecturesCount, tvNextClassInfo;
    private ProgressBar circularProgress;
    private TextView tvAttendancePercentage;

    // Status/Countdown/Quote Views
    private CardView cvHomeStatus;
    private TextView tvHomeStatusTitle, tvHomeStatusMain, tvHomeStatusSubtitle;
    private CountDownTimer countDownTimer;

    private final String[] motivationalQuotes = {
        "The secret of getting ahead is getting started.",
        "It always seems impossible until it's done.",
        "Don't let what you cannot do interfere with what you can do.",
        "Success is the sum of small efforts, repeated day in and day out.",
        "Your education is a dress rehearsal for a life that is yours to lead.",
        "The expert in anything was once a beginner.",
        "Believe you can and you're halfway there."
    };

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

        cvHomeStatus = view.findViewById(R.id.cvHomeStatus);
        tvHomeStatusTitle = view.findViewById(R.id.tvHomeStatusTitle);
        tvHomeStatusMain = view.findViewById(R.id.tvHomeStatusMain);
        tvHomeStatusSubtitle = view.findViewById(R.id.tvHomeStatusSubtitle);

        loadDashboardInfo();
        loadProgressInfo();

        return view;
    }

    private void loadDashboardInfo() {
        if (getContext() == null) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        tvCurrentDate.setText(dateFormat.format(new Date()));

        SharedPreferences preferences = getContext().getSharedPreferences("user_details", MODE_PRIVATE);
        String name = preferences.getString("name", null);
        String username = preferences.getString("username", "");
        String role = preferences.getString("role", "Student");
        
        if (name == null) name = preferences.getString("username", "User");
        tvWelcomeMessage.setText(String.format("Welcome, %s!", name));

        String currentDay = new SimpleDateFormat("EEE", Locale.US).format(new Date()).toUpperCase();
        
        // Updated call to DBHelper
        int lectureCount = dbHelper.getLecturesCountForDay(currentDay, username, role);
        tvTodayLecturesCount.setText(String.format(Locale.getDefault(), "You have %d lectures today.", lectureCount));

        String currentTime = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        
        // Updated call to DBHelper
        CourseModel nextClass = dbHelper.getNextClass(currentDay, currentTime, username, role);

        if (nextClass != null) {
            tvNextClassInfo.setText(String.format("%s at %s", nextClass.getSubjectName(), nextClass.getTimeslot()));
            updateStatusWithCountdown(nextClass);
        } else {
            tvNextClassInfo.setText("No more classes today.");
            updateStatusWithQuote();
        }
    }

    private void updateStatusWithCountdown(CourseModel nextClass) {
        if (countDownTimer != null) countDownTimer.cancel();

        long diff = getMillisUntil(nextClass.getTimeslot());

        if (diff > 0) {
            if (cvHomeStatus != null) cvHomeStatus.setVisibility(View.VISIBLE);
            if (tvHomeStatusTitle != null) {
                tvHomeStatusTitle.setVisibility(View.VISIBLE);
                tvHomeStatusTitle.setText("Next Lecture Starts In");
            }
            if (tvHomeStatusSubtitle != null) tvHomeStatusSubtitle.setText(nextClass.getSubjectName());

            countDownTimer = new CountDownTimer(diff, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long h = (millisUntilFinished / 3600000) % 24;
                    long m = (millisUntilFinished / 60000) % 60;
                    long s = (millisUntilFinished / 1000) % 60;
                    if (tvHomeStatusMain != null) {
                        tvHomeStatusMain.setText(String.format(Locale.getDefault(), "%02d : %02d : %02d", h, m, s));
                    }
                }

                @Override
                public void onFinish() {
                    if (tvHomeStatusMain != null) tvHomeStatusMain.setText("00 : 00 : 00");
                    loadDashboardInfo(); 
                }
            }.start();
        } else {
            updateStatusWithQuote();
        }
    }

    private void updateStatusWithQuote() {
        if (countDownTimer != null) countDownTimer.cancel();
        if (cvHomeStatus != null) {
            cvHomeStatus.setVisibility(View.VISIBLE);
            if (tvHomeStatusTitle != null) tvHomeStatusTitle.setVisibility(View.GONE);
            if (tvHomeStatusMain != null) {
                tvHomeStatusMain.setText(motivationalQuotes[new Random().nextInt(motivationalQuotes.length)]);
            }
            if (tvHomeStatusSubtitle != null) tvHomeStatusSubtitle.setText("");
        }
    }

    private long getMillisUntil(String timeslot) {
        try {
            String startTimeStr = timeslot.split("(?i) to | - ")[0].trim().toLowerCase();
            int startMinutes = parseTimeToMinutes(startTimeStr);
            
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, startMinutes / 60);
            target.set(Calendar.MINUTE, startMinutes % 60);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);

            return target.getTimeInMillis() - System.currentTimeMillis();
        } catch (Exception e) { return -1; }
    }

    private int parseTimeToMinutes(String timeStr) {
        try {
            if (timeStr.contains("am") || timeStr.contains("pm")) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                if (!timeStr.contains(":")) {
                    sdf = new SimpleDateFormat("hh a", Locale.US);
                }
                Date date = sdf.parse(timeStr);
                Calendar c = Calendar.getInstance();
                if (date != null) {
                    c.setTime(date);
                    return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
                }
            } else {
                String[] parts = timeStr.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
                return h * 60 + m;
            }
        } catch (Exception e) { return 0; }
        return 0;
    }

    private void loadProgressInfo() {
        if (getContext() == null) return;
        int attendance = dbHelper.getAttendancePercentage();
        circularProgress.setProgress(attendance);
        tvAttendancePercentage.setText(String.format(Locale.getDefault(), "%d%%", attendance));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardInfo();
        loadProgressInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
