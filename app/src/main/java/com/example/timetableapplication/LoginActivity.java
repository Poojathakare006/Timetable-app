package com.example.timetableapplication;

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
    TextView tvNewUserClickHere, tvForgotPassword;
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
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        cbLoginShowandHidePassword = findViewById(R.id.cbLoginShowandHidePassword);
        btnLoginLogin = findViewById(R.id.btnLoginLogin);
        tvNewUserClickHere = findViewById(R.id.tvNewUserClickHere);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        cbLoginShowandHidePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etLoginPassword.setSelection(etLoginPassword.getText().length());
        });

        btnLoginLogin.setOnClickListener(v -> {
            String username = etLoginUsername.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.checkLogin(username, password)) {
                editor.putBoolean("isLogin", true);
                editor.putString("username", username);
                editor.apply();
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });

        tvNewUserClickHere.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class))
        );

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        final EditText input = new EditText(this);
        input.setHint("Enter your username");
        builder.setView(input);
        builder.setPositiveButton("Reset", (dialog, which) -> {
            // Revert placeholder
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
