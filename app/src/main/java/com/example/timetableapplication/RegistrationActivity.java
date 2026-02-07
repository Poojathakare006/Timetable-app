package com.example.timetableapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    EditText etRegisterName, etRegisterMobileNumber, etRegisterEmailId, etRegisterUsername, etRegisterPassword;
    CheckBox cbRegisterShowAndHidePassword;
    Button btnRegisterRegister;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        dbHelper = new DBHelper(this);

        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterMobileNumber = findViewById(R.id.etRegisterMobileNumber);
        etRegisterEmailId = findViewById(R.id.etRegisterEmailId);
        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        cbRegisterShowAndHidePassword = findViewById(R.id.cbRegisterShowAndHidePassword);
        btnRegisterRegister = findViewById(R.id.btnRegisterRegister);

        cbRegisterShowAndHidePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etRegisterPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etRegisterPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etRegisterPassword.setSelection(etRegisterPassword.getText().length());
        });

        btnRegisterRegister.setOnClickListener(view -> {
            String name = etRegisterName.getText().toString();
            String email = etRegisterEmailId.getText().toString();
            String mobile = etRegisterMobileNumber.getText().toString();
            String username = etRegisterUsername.getText().toString();
            String password = etRegisterPassword.getText().toString();

            if (name.isEmpty()) {
                etRegisterName.setError("Please enter your Name");
            } else if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etRegisterEmailId.setError("Please enter a valid Email id");
            } else if (mobile.length() < 10) {
                etRegisterMobileNumber.setError("Please enter a valid 10-digit mobile number");
            } else if (username.isEmpty()) {
                etRegisterUsername.setError("Please enter a username");
            } else if (password.isEmpty()) {
                etRegisterPassword.setError("Please enter your password");
            } else if (password.length() < 8) {
                etRegisterPassword.setError("Password must be at least 8 characters");
            } else if (!password.matches(".*[A-Z].*")) {
                etRegisterPassword.setError("Password must contain at least one uppercase letter");
            } else if (!password.matches(".*[a-z].*")) {
                etRegisterPassword.setError("Password must contain at least one lowercase letter");
            } else if (!password.matches(".*[0-9].*")) {
                etRegisterPassword.setError("Password must contain at least one number");
            } else if (!password.matches(".*[@#$%^&+=!].*")) {
                etRegisterPassword.setError("Password must contain at least one special character (@#$%^&+=!)");
            } else {
                if (dbHelper.insertUser(name, email, mobile, username, password)) {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed. Username, email, or mobile may already be in use.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
