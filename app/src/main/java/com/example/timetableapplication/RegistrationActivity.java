package com.example.timetableapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    EditText etName, etEmail, etMobile, etUsername, etPassword, etCourse, etYear, etCollegeName;
    RadioGroup rgUserType;
    RadioButton rbStudent, rbTeacher;
    LinearLayout studentFieldsContainer;
    Button btnRegister;
    TextView tvAlreadyRegistered;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        dbHelper = new DBHelper(this);

        etName = findViewById(R.id.etRegistrationName);
        etEmail = findViewById(R.id.etRegistrationEmail);
        etMobile = findViewById(R.id.etRegistrationMobile);
        etUsername = findViewById(R.id.etRegistrationUsername);
        etPassword = findViewById(R.id.etRegistrationPassword);
        rgUserType = findViewById(R.id.rgUserType);
        rbStudent = findViewById(R.id.rbStudent);
        rbTeacher = findViewById(R.id.rbTeacher);
        studentFieldsContainer = findViewById(R.id.student_fields_container);
        etCourse = findViewById(R.id.etCourse);
        etYear = findViewById(R.id.etYear);
        etCollegeName = findViewById(R.id.etCollegeName);
        btnRegister = findViewById(R.id.btnRegister);
        tvAlreadyRegistered = findViewById(R.id.tvAlreadyRegistered);

        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbStudent) {
                studentFieldsContainer.setVisibility(View.VISIBLE);
            } else {
                studentFieldsContainer.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> registerUser());

        tvAlreadyRegistered.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String userType = rbStudent.isChecked() ? "Student" : "Teacher";

        String course = "";
        String year = "";
        String collegeName = "";

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        // ... add other validation as needed ...

        if (userType.equals("Student")) {
            course = etCourse.getText().toString().trim();
            year = etYear.getText().toString().trim();
            collegeName = etCollegeName.getText().toString().trim();
            if (course.isEmpty() || year.isEmpty() || collegeName.isEmpty()) {
                Toast.makeText(this, "Please fill all student fields", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean success = dbHelper.insertUser(name, email, mobile, username, password, userType, course, year, collegeName);

        if (success) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Username, email, or mobile may already exist.", Toast.LENGTH_LONG).show();
        }
    }
}
