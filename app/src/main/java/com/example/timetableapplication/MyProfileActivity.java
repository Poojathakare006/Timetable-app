package com.example.timetableapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.timetableapplication.ModelClass.User;

public class MyProfileActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SharedPreferences preferences;
    TextView tvName, tvUsername, tvCourse, tvYear, tvCollege, tvTimetablesCreated, tvCourseLabel;
    LinearLayout studentInfoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = findViewById(R.id.toolbar_my_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("user_details", MODE_PRIVATE);

        tvName = findViewById(R.id.tv_profile_name);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvCourse = findViewById(R.id.tv_profile_course);
        tvYear = findViewById(R.id.tv_profile_year);
        tvCollege = findViewById(R.id.tv_profile_college);
        tvTimetablesCreated = findViewById(R.id.tv_timetables_created);
        tvCourseLabel = findViewById(R.id.tv_course_label);
        studentInfoContainer = findViewById(R.id.student_info_container);

        loadProfileData();
    }

    private void loadProfileData() {
        String username = preferences.getString("username", null);
        if (username != null) {
            User user = dbHelper.getUser(username);
            if (user != null) {
                tvName.setText(user.getName());
                tvUsername.setText("@" + user.getUsername());

                if ("Student".equals(user.getUserType())) {
                    studentInfoContainer.setVisibility(View.VISIBLE);
                    tvCourseLabel.setText("Course");
                    tvCourse.setText(user.getCourse());
                    tvYear.setText(user.getYear());
                    tvCollege.setText(user.getCollegeName());
                } else { // Teacher
                    studentInfoContainer.setVisibility(View.GONE);
                    tvCourseLabel.setText("Subjects Taught");
                    tvCourse.setText("Not specified"); // Placeholder for teacher subjects
                }

                // Set timetable count
                int count = dbHelper.getTimetableCount();
                tvTimetablesCreated.setText(String.valueOf(count));

            }
        }
    }
}
