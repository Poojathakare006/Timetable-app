package com.example.timetableapplication.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.SubjectAnalytics;
import com.example.timetableapplication.ModelClass.User;
import com.example.timetableapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    DBHelper dbHelper;
    TextView tvWelcomeMessage, tvCurrentDate, tvTodayLecturesCount, tvNextClassInfo;
    ProgressBar circularProgress;
    TextView tvAttendancePercentage;
    EditText etSyllabusNotes;
    ImageView ivFlame1, ivFlame2, ivFlame3, ivFlame4, ivFlame5, ivFlame6, ivFlame7;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DBHelper(getContext());

        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        tvTodayLecturesCount = view.findViewById(R.id.tvTodayLecturesCount);
        tvNextClassInfo = view.findViewById(R.id.tvNextClassInfo);
        circularProgress = view.findViewById(R.id.circularProgress);
        tvAttendancePercentage = view.findViewById(R.id.tvAttendancePercentage);
        etSyllabusNotes = view.findViewById(R.id.etSyllabusNotes);
        ivFlame1 = view.findViewById(R.id.ivFlame1);
        ivFlame2 = view.findViewById(R.id.ivFlame2);
        ivFlame3 = view.findViewById(R.id.ivFlame3);
        ivFlame4 = view.findViewById(R.id.ivFlame4);
        ivFlame5 = view.findViewById(R.id.ivFlame5);
        ivFlame6 = view.findViewById(R.id.ivFlame6);
        ivFlame7 = view.findViewById(R.id.ivFlame7);

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
        String username = preferences.getString("username", null);

        if (username != null) {
            User user = dbHelper.getUser(username);
            if (user != null) {
                tvWelcomeMessage.setText("Welcome, " + user.getName() + "!");

                // For both students and teachers, today's lectures and next class are relevant
                String currentDayAbbr = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date()).toUpperCase();
                int todayLectures = dbHelper.getLecturesForTeacherToday(user.getName(), currentDayAbbr); // Re-using this for simplicity
                tvTodayLecturesCount.setText("You have " + todayLectures + " lectures today.");

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = timeFormat.format(new Date());
                CourseModel nextClass = dbHelper.getNextClass(currentDayAbbr, currentTime);

                if (nextClass != null) {
                    tvNextClassInfo.setText(String.format("%s at %s", nextClass.getSubjectName(), nextClass.getTimeslot()));
                } else {
                    tvNextClassInfo.setText("No more classes today.");
                }
            }
        }
    }

    private void loadProgressInfo() {
        // Attendance
        Map<String, SubjectAnalytics> summary = dbHelper.getSubjectAttendanceSummary();
        int totalAttended = 0;
        int totalLectures = 0;
        for (SubjectAnalytics analytics : summary.values()) {
            totalAttended += analytics.getAttendedLectures();
            totalLectures += analytics.getTotalLectures();
        }

        int attendancePercentage = 0;
        if (totalLectures > 0) {
            attendancePercentage = (int) (((double) totalAttended / totalLectures) * 100);
        }

        circularProgress.setProgress(attendancePercentage);
        tvAttendancePercentage.setText(attendancePercentage + "%" );

        // Dummy data for streak
        int streak = 3;
        ImageView[] flames = {ivFlame1, ivFlame2, ivFlame3, ivFlame4, ivFlame5, ivFlame6, ivFlame7};
        for (int i = 0; i < flames.length; i++) {
            if (i < streak) {
                flames[i].setColorFilter(getResources().getColor(android.R.color.holo_orange_light));
            } else {
                flames[i].setColorFilter(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh dashboard every time the user returns to the home screen
        loadDashboardInfo();
        loadProgressInfo();
    }
}
