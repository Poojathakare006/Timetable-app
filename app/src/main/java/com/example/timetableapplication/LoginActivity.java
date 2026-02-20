package com.example.timetableapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your username");
        builder.setView(input);

        builder.setPositiveButton("Reset", (dialog, which) -> {
            String username = input.getText().toString().trim();
            if (dbHelper.getUser(username) != null) {
                showResetPasswordDialog(username);
            } else {
                Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showResetPasswordDialog(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordInput.setHint("New Password");
        layout.addView(newPasswordInput);

        final EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordInput.setHint("Confirm New Password");
        layout.addView(confirmPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newPassword = newPasswordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (newPassword.equals(confirmPassword)) {
                if (newPassword.length() >= 6) {
                    dbHelper.updatePassword(username, newPassword);
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
