package com.example.timetableapplication.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.User;
import com.example.timetableapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    ImageSlider imageSlider;
    DBHelper dbHelper;

    // Dashboard Cards
    CardView studentDashboard, teacherDashboard;

    // Student Views
    TextView tvTodayDay, tvNextClass, tvTimetableCount;

    // Teacher Views
    TextView tvTodayLectures, tvWeekLectures;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DBHelper(getContext());

        // Image Slider
        imageSlider = view.findViewById(R.id.imageSlider);

        // Dashboards
        studentDashboard = view.findViewById(R.id.student_info_card);
        teacherDashboard = view.findViewById(R.id.teacher_info_card);

        // Student Views
        tvTodayDay = view.findViewById(R.id.tvTodayDay);
        tvNextClass = view.findViewById(R.id.tvNextClass);
        tvTimetableCount = view.findViewById(R.id.tvTimetableCount);

        // Teacher Views
        tvTodayLectures = view.findViewById(R.id.tvTodayLectures);
        tvWeekLectures = view.findViewById(R.id.tvWeekLectures);

        setupImageSlider();
        updateDashboard();

        return view;
    }

    private void setupImageSlider() {
        ArrayList<SlideModel> slideModelArrayList = new ArrayList<>();
        slideModelArrayList.add(new SlideModel(R.drawable.timetable1, "Timetable1", ScaleTypes.CENTER_CROP));
        slideModelArrayList.add(new SlideModel(R.drawable.timetable2, "Timetable2", ScaleTypes.CENTER_CROP));
        slideModelArrayList.add(new SlideModel(R.drawable.timetable3, "Timetable3", ScaleTypes.CENTER_CROP));
        slideModelArrayList.add(new SlideModel(R.drawable.timetable4, "Timetable4", ScaleTypes.CENTER_CROP));
        slideModelArrayList.add(new SlideModel(R.drawable.timetable5, "Timetable5", ScaleTypes.CENTER_CROP));
        slideModelArrayList.add(new SlideModel(R.drawable.timetable6, "Timetable6", ScaleTypes.CENTER_CROP));
        imageSlider.setImageList(slideModelArrayList);
    }

    private void updateDashboard() {
        if (getContext() == null) return;

        SharedPreferences preferences = getContext().getSharedPreferences("user_details", MODE_PRIVATE);
        String username = preferences.getString("username", null);

        if (username != null) {
            User user = dbHelper.getUser(username);
            if (user != null) {
                if ("Teacher".equals(user.getUserType())) {
                    teacherDashboard.setVisibility(View.VISIBLE);
                    studentDashboard.setVisibility(View.GONE);
                    loadTeacherDashboard(user.getName());
                } else {
                    studentDashboard.setVisibility(View.VISIBLE);
                    teacherDashboard.setVisibility(View.GONE);
                    loadStudentDashboard();
                }
            }
        }
    }

    private void loadStudentDashboard() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String currentDay = dayFormat.format(new Date());
        tvTodayDay.setText(currentDay);

        int timetableCount = dbHelper.getTimetableCount();
        tvTimetableCount.setText(String.valueOf(timetableCount));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        String currentDayAbbr = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date()).toUpperCase();

        CourseModel nextClass = dbHelper.getNextClass(currentDayAbbr, currentTime);

        if (nextClass != null) {
            tvNextClass.setText(String.format("%s at %s", nextClass.getSubjectName(), nextClass.getTimeslot()));
        } else {
            tvNextClass.setText("No Classes Scheduled");
        }
    }

    private void loadTeacherDashboard(String teacherName) {
        String currentDayAbbr = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date()).toUpperCase();
        int todayLectures = dbHelper.getLecturesForTeacherToday(teacherName, currentDayAbbr);
        int weekLectures = dbHelper.getLecturesForTeacherThisWeek(teacherName);

        tvTodayLectures.setText(String.valueOf(todayLectures));
        tvWeekLectures.setText(String.valueOf(weekLectures));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDashboard();
    }
}
