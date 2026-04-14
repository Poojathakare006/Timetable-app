package com.example.timetableapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.TimetableHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddPersonalTimetableActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskTime;
    private Button btnAddTask, btnGenerate;
    private TextView tvSummary;
    private ArrayList<TaskItem> taskList = new ArrayList<>();
    private DBHelper dbHelper;
    private SharedPreferences preferences;

    private static class TaskItem {
        String name;
        String time;
        TaskItem(String name, String time) {
            this.name = name;
            this.time = time;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personal_timetable);

        preferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Explicitly set the status bar to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            
            // On API 23+, use dark icons on white background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DBHelper(this);

        etTaskName = findViewById(R.id.etPersonalTask);
        etTaskTime = findViewById(R.id.etPersonalTime);
        btnAddTask = findViewById(R.id.btnAddPersonalItem);
        btnGenerate = findViewById(R.id.btnGeneratePersonalTimetable);
        tvSummary = findViewById(R.id.tvAddedTasksLabel);

        btnAddTask.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            String time = etTaskTime.getText().toString().trim();

            if (!name.isEmpty() && !time.isEmpty()) {
                taskList.add(new TaskItem(name, time));
                tvSummary.setText("Tasks Added: " + taskList.size());
                etTaskName.setText("");
                etTaskTime.setText("");
                Toast.makeText(this, "Task added to list", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter both task and time", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerate.setOnClickListener(v -> {
            if (taskList.isEmpty()) {
                Toast.makeText(this, "Add at least one task first", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = preferences.getString("username", "Unknown");
            
            // Fix: Clear only this user's personal timetable instead of the entire database
            dbHelper.clearPersonalTimetable(username);

            String today = new SimpleDateFormat("EEE", Locale.US).format(new Date()).toUpperCase();

            for (TaskItem task : taskList) {
                CourseModel course = new CourseModel(0, "Personal Timetable", "Self", task.name, "Personal", task.time, today, null);
                dbHelper.addTimetableEntry(course, username);
            }

            dbHelper.addTimetableHistory(new TimetableHistory(0, "Personal Timetable", "Manual Entry", System.currentTimeMillis()));
            Toast.makeText(this, "Personal Timetable Generated!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
