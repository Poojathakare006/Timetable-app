package com.example.timetableapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.example.timetableapplication.ModelClass.CourseModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String role = preferences.getString("role", "Student");

        if (intent.getAction() != null && 
            (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || 
             intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))) {
            
            // Reschedule alarms after boot
            if (preferences.getBoolean("notifications_enabled", false)) {
                scheduleNextAlarm(context);
            }
            return;
        }

        DBHelper dbHelper = new DBHelper(context);
        
        Calendar now = Calendar.getInstance();
        String currentDay = new SimpleDateFormat("EEE", Locale.US).format(now.getTime()).toUpperCase();
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.US).format(now.getTime());

        CourseModel nextClass = dbHelper.getNextClass(currentDay, currentTime, username, role);

        if (nextClass != null) {
            String subjectName = nextClass.getSubjectName();
            String timeslot = nextClass.getTimeslot();
            showNotification(context, "Upcoming Class Reminder", 
                "Your next class: " + subjectName + " starts at " + timeslot);
        }
    }

    private void scheduleNextAlarm(Context context) {
        // Logic to reschedule from SettingsActivity context
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = 15 * 60 * 1000;
        if (alarmManager != null) {
            alarmManager.setRepeating(android.app.AlarmManager.RTC_WAKEUP, 
                System.currentTimeMillis() + interval, interval, pendingIntent);
        }
    }

    private void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "timetable_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Timetable Reminders", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) 
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
