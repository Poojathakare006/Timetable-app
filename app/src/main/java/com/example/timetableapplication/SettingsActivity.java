package com.example.timetableapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    TextView tvEditProfile, tvChangePassword, tvClearHistory, tvDeleteAccount, tvColorScheme, tvTimetableOrientation;
    Button btnLogout;
    SwitchCompat notificationSwitch;
    DBHelper dbHelper;
    SharedPreferences preferences;
    private int tempSelectedThemeIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("user_details", MODE_PRIVATE);

        tvEditProfile = findViewById(R.id.setting_edit_profile);
        tvChangePassword = findViewById(R.id.setting_change_password);
        tvClearHistory = findViewById(R.id.setting_clear_history);
        tvDeleteAccount = findViewById(R.id.setting_delete_account);
        btnLogout = findViewById(R.id.setting_logout);
        notificationSwitch = findViewById(R.id.setting_notifications);
        tvColorScheme = findViewById(R.id.setting_color_scheme);
        tvTimetableOrientation = findViewById(R.id.setting_timetable_orientation);

        notificationSwitch.setChecked(preferences.getBoolean("notifications_enabled", false));

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkNotificationPermissionAndSchedule();
            } else {
                cancelAlarm();
                preferences.edit().putBoolean("notifications_enabled", false).apply();
            }
        });

        tvEditProfile.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, MyProfileActivity.class)));
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        tvChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        tvDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        tvClearHistory.setOnClickListener(v -> showClearHistoryDialog());
        tvColorScheme.setOnClickListener(v -> showColorSchemeDialog());
        tvTimetableOrientation.setOnClickListener(v -> showOrientationDialog());
    }

    private void checkNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                scheduleAlarm();
                preferences.edit().putBoolean("notifications_enabled", true).apply();
            }
        } else {
            scheduleAlarm();
            preferences.edit().putBoolean("notifications_enabled", true).apply();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleAlarm();
                preferences.edit().putBoolean("notifications_enabled", true).apply();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                notificationSwitch.setChecked(false);
            }
        }
    }

    private void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        long interval = 15 * 60 * 1000;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
        Toast.makeText(this, "Reminders Enabled", Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Reminders Disabled", Toast.LENGTH_SHORT).show();
    }


    private void showOrientationDialog() {
        final String[] orientations = {"Classic (Time on side)", "Horizontal (Days on side)"};
        int currentOrientation = preferences.getInt("timetable_orientation", 0);

        new AlertDialog.Builder(this)
                .setTitle("Choose Timetable Orientation")
                .setSingleChoiceItems(orientations, currentOrientation, null)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    preferences.edit().putInt("timetable_orientation", selectedPosition).apply();
                    Toast.makeText(this, orientations[selectedPosition] + " selected", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showColorSchemeDialog() {
        final String[] themes = {"Default", "Vibrant", "Pastel", "Corporate Blue", "Autumn Harvest", "Sunrise", "Emerald", "Lavender Bliss", "Minty Fresh", "Simple"};
        tempSelectedThemeIndex = preferences.getInt("timetable_theme", 0);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_color_scheme_list, null);
        ListView listView = dialogView.findViewById(R.id.color_scheme_list_view);

        final ColorSchemeAdapter adapter = new ColorSchemeAdapter(this, Arrays.asList(themes), tempSelectedThemeIndex);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Choose Color Scheme")
                .setView(dialogView)
                .setPositiveButton("OK", (d, which) -> {
                    preferences.edit().putInt("timetable_theme", tempSelectedThemeIndex).apply();
                    Toast.makeText(SettingsActivity.this, themes[tempSelectedThemeIndex] + " theme selected", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            tempSelectedThemeIndex = position;
            adapter.setSelectedIndex(position);
        });

        dialog.show();
    }

    private static class ColorSchemeAdapter extends ArrayAdapter<String> {
        private final List<String> themeNames;
        private int selectedIndex;

        public ColorSchemeAdapter(Context context, List<String> themeNames, int selectedIndex) {
            super(context, R.layout.dialog_color_scheme_item, themeNames);
            this.themeNames = themeNames;
            this.selectedIndex = selectedIndex;
        }

        public void setSelectedIndex(int index) {
            this.selectedIndex = index;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_color_scheme_item, parent, false);
                holder = new ViewHolder();
                holder.radioButton = convertView.findViewById(R.id.radio_button);
                holder.themeName = convertView.findViewById(R.id.tv_theme_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.themeName.setText(themeNames.get(position));
            holder.radioButton.setChecked(position == selectedIndex);

            return convertView;
        }

        private static class ViewHolder {
            RadioButton radioButton;
            TextView themeName;
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    preferences.edit().clear().apply();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText oldPasswordInput = new EditText(this);
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPasswordInput.setHint("Old Password");
        layout.addView(oldPasswordInput);

        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordInput.setHint("New Password");
        layout.addView(newPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString();
            String newPassword = newPasswordInput.getText().toString();
            String username = preferences.getString("username", null);

            if (username != null && dbHelper.checkLogin(username, oldPassword)) {
                if (newPassword.length() >= 8) {
                    dbHelper.updatePassword(username, newPassword);
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "New password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This action is permanent and cannot be undone. Are you absolutely sure you want to delete your account and all associated data?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    String username = preferences.getString("username", null);
                    if (username != null) {
                        dbHelper.deleteUser(username);
                        dbHelper.clearAllTimetableHistory();
                        preferences.edit().clear().apply();
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all saved timetable PDFs? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    dbHelper.clearAllTimetableHistory();
                    Toast.makeText(this, "Timetable history cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
