package com.example.timetableapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etLoginUsername, etLoginPassword;
    CheckBox cbLoginShowandHidePassword;
    Button btnLoginLogin;
    TextView tvNewUserClickHere;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("user_details", MODE_PRIVATE);
        editor = preferences.edit();

        if (preferences.getBoolean("isLogin", false)) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        boolean firstTime = preferences.getBoolean("isfirsttime", true);
        if (firstTime) {
            Welcome();
            editor.putBoolean("isfirsttime", false).apply();
        }

        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        cbLoginShowandHidePassword = findViewById(R.id.cbLoginShowandHidePassword);
        btnLoginLogin = findViewById(R.id.btnLoginLogin);
        tvNewUserClickHere = findViewById(R.id.tvNewUserClickHere);

        cbLoginShowandHidePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etLoginPassword.setSelection(etLoginPassword.getText().length());
        });

        btnLoginLogin.setOnClickListener(v -> {
            String username = etLoginUsername.getText().toString();
            String password = etLoginPassword.getText().toString();

            if (username.isEmpty()) {
                etLoginUsername.setError("Please enter Username");
            } else if (password.isEmpty()) {
                etLoginPassword.setError("Please enter your Password");
            } else {
                if (dbHelper.checkLogin(username, password)) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("isLogin", true);
                    editor.putString("username", username);
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvNewUserClickHere.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void Welcome() {
        AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
        ad.setTitle("Timetable");
        ad.setMessage("Welcome to Timetable");
        ad.setPositiveButton("Thank you", (dialog, which) -> {
        });
        ad.create().show();
    }
}
