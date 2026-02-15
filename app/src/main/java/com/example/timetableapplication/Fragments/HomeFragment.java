package com.example.timetableapplication.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.R;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    ImageSlider imageSlider;
    TextView tvToday, tvNextClass, tvTimetablesCreated;
    DBHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DBHelper(getContext());

        imageSlider = view.findViewById(R.id.imageSlider);
        tvToday = view.findViewById(R.id.tvMonday); // Corresponds to "Monday" in your layout
        tvNextClass = view.findViewById(R.id.ivNoClassesScheduled); // Corresponds to "No Classes..."
        tvTimetablesCreated = view.findViewById(R.id.tvZero); // Corresponds to "0"

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
        // 1. Get and Display Current Day
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String currentDay = dayFormat.format(new Date());
        tvToday.setText(currentDay);

        // 2. Get and Display Timetable Count
        int timetableCount = dbHelper.getTimetableCount();
        tvTimetablesCreated.setText(String.valueOf(timetableCount));

        // 3. Find and Display Next Class
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

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the dashboard every time the fragment is shown
        updateDashboard();
    }
}
