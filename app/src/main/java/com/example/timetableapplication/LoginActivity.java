package com.example.timetableapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

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

        preferences = getSharedPreferences("user_details", MODE_PRIVATE);
        editor = preferences.edit();

        boolean firsttime = preferences.getBoolean("isfirsttime", true);
        if (firsttime) {

            Welcome();
        }

        if (preferences.getBoolean("isLogin", false)) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        {

            setContentView(R.layout.activity_login);


            dbHelper = new DBHelper(this);


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

                if (etLoginUsername.getText().toString().isEmpty()) {
                    etLoginUsername.setError("Please enter Username");
                } else if (etLoginUsername.getText().toString().length() < 8) {
                    etLoginUsername.setError("Username must be atleast 8 charecters");
                } else if (etLoginPassword.getText().toString().isEmpty()) {
                    etLoginPassword.setError("Please enter your Password");
                } else if (etLoginPassword.getText().toString().length() < 8) {
                    etLoginPassword.setError("Password must be atleast 8 charecters");
                } else if (!etLoginPassword.getText().toString().matches(".*[A-Z].*")) {
                    etLoginPassword.setError("Password must contain atleast 1 Uppercase letter");
                } else if (!etLoginPassword.getText().toString().matches(".*[a-z].*")) {
                    etLoginPassword.setError("Password must contain atleast 1 lowercase letter");
                } else if (!etLoginPassword.getText().toString().matches(".*[0-9].*")) {
                    etLoginPassword.setError("Password must contain atlast 1 number");
                } else if (!etLoginPassword.getText().toString().matches(".*[@#$%&!+=].*")) {
                    etLoginPassword.setError("Plaase enter atleast one special symbol");
                } else {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("isLogin", true);
                    editor.putString("username",etLoginUsername.getText().toString());
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            tvNewUserClickHere.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            });
        }

    }

    private void Welcome() {
        AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
        ad.setTitle("Timetable");
        ad.setMessage("Welcome to Timetable");
        ad.setPositiveButton("Thank you", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        ad.create().show();
    }
}
