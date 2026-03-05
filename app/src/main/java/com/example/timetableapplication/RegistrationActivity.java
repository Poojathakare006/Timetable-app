package com.example.timetableapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
    CheckBox cbRegisterShowPassword;
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
        cbRegisterShowPassword = findViewById(R.id.cbregisterShowPassword);
        rgUserType = findViewById(R.id.rgUserType);
        rbStudent = findViewById(R.id.rbStudent);
        rbTeacher = findViewById(R.id.rbTeacher);
        studentFieldsContainer = findViewById(R.id.student_fields_container);
        etCourse = findViewById(R.id.etCourse);
        etYear = findViewById(R.id.etYear);
        etCollegeName = findViewById(R.id.etCollegeName);
        btnRegister = findViewById(R.id.btnRegister);
        tvAlreadyRegistered = findViewById(R.id.tvAlreadyRegistered);

        cbRegisterShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });

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

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(mobile) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }

        String course = etCourse.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String collegeName = etCollegeName.getText().toString().trim();

        if (dbHelper.insertUser(name, email, mobile, username, password, userType, course, year, collegeName)) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
