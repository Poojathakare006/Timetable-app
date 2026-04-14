package com.example.timetableapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.ModelClass.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "UserDB.db";
    public static final int DB_VERSION = 12;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_TIMETABLE = "timetable";
    public static final String TABLE_TIMETABLE_HISTORY = "timetable_history";

    public DBHelper(Context context) {
        super(context, DBNAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, mobile TEXT UNIQUE, username TEXT UNIQUE, password TEXT, user_type TEXT, course TEXT, year TEXT, college_name TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_TIMETABLE + " (course_id INTEGER PRIMARY KEY AUTOINCREMENT, course_name TEXT, teacher_name TEXT, subject_name TEXT, class_name TEXT, timeslot TEXT, day TEXT, status TEXT, notes TEXT, creator_username TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_TIMETABLE_HISTORY + " (history_id INTEGER PRIMARY KEY AUTOINCREMENT, course_name TEXT, pdf_path TEXT, created_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE_HISTORY);
        onCreate(db);
    }

    public boolean insertUser(String name, String email, String mobile, String username, String password, String userType, String course, String year, String collegeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("mobile", mobile);
        values.put("username", username);
        values.put("password", password);
        values.put("user_type", userType);
        values.put("course", course);
        values.put("year", year);
        values.put("college_name", collegeName);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ? AND password = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, email, mobile, username, user_type, course, year, college_name FROM " + TABLE_USERS + " WHERE username = ?", new String[]{username});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
        }
        cursor.close();
        return user;
    }

    public boolean addTimetableEntry(CourseModel course, String creatorUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("course_name", course.getCourseName());
        values.put("teacher_name", course.getTeacherName());
        values.put("subject_name", course.getSubjectName());
        values.put("class_name", course.getClassName());
        values.put("timeslot", course.getTimeslot());
        values.put("day", course.getDay());
        values.put("status", course.getStatus());
        values.put("notes", "");
        values.put("creator_username", creatorUsername);
        long result = db.insert(TABLE_TIMETABLE, null, values);
        return result != -1;
    }

    public void clearPersonalTimetable(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMETABLE, "creator_username = ? AND course_name = 'Personal Timetable'", new String[]{username});
    }

    public void clearSchoolTimetable(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMETABLE, "creator_username = ? AND course_name != 'Personal Timetable'", new String[]{username});
    }

    public void clearTimetableData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMETABLE, null, null);
    }

    public ArrayList<CourseModel> getAllTimetableEntries() {
        ArrayList<CourseModel> timetable = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TIMETABLE, null);
        if (cursor.moveToFirst()) {
            do {
                timetable.add(new CourseModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return timetable;
    }

    public ArrayList<CourseModel> getUserTimetable(String username) {
        ArrayList<CourseModel> timetable = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TIMETABLE + " WHERE creator_username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            do {
                timetable.add(new CourseModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return timetable;
    }

    public int getTimetableCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT course_name) FROM " + TABLE_TIMETABLE, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getLecturesCountForDay(String day, String username, String role) {
        ArrayList<CourseModel> list = getUserTimetable(username);
        int count = 0;
        for (CourseModel c : list) {
            if (c.getDay().equalsIgnoreCase(day) && !"Recess".equalsIgnoreCase(c.getSubjectName()) && !"Free".equalsIgnoreCase(c.getSubjectName())) {
                count++;
            }
        }
        return count;
    }

    public CourseModel getNextClass(String day, String currentTime, String username, String role) {
        ArrayList<CourseModel> list = getUserTimetable(username);
        CourseModel nextClass = null;
        long minDiff = Long.MAX_VALUE;
        long nowMillis = convertTimeToMillis(currentTime);

        for (CourseModel c : list) {
            if (c.getDay().equalsIgnoreCase(day) && !"Recess".equalsIgnoreCase(c.getSubjectName()) && !"Free".equalsIgnoreCase(c.getSubjectName())) {
                String startTimeStr = c.getTimeslot().split("(?i) to | - ")[0].trim();
                long startMillis = convertTimeToMillis(startTimeStr);
                if (startMillis > nowMillis) {
                    long diff = startMillis - nowMillis;
                    if (diff < minDiff) {
                        minDiff = diff;
                        nextClass = c;
                    }
                }
            }
        }
        return nextClass;
    }

    private long convertTimeToMillis(String timeStr) {
        try {
            String cleanTime = timeStr.toLowerCase().trim();
            if (cleanTime.contains("am") || cleanTime.contains("pm")) {
                if (!cleanTime.contains(" ")) cleanTime = cleanTime.replace("am", " am").replace("pm", " pm").trim();
                SimpleDateFormat format = new SimpleDateFormat(cleanTime.contains(":") ? "hh:mm a" : "h a", Locale.US);
                Date date = format.parse(cleanTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return (cal.get(Calendar.HOUR_OF_DAY) * 3600000L) + (cal.get(Calendar.MINUTE) * 60000L);
            } else {
                String[] hm = cleanTime.split(":");
                int h = Integer.parseInt(hm[0]);
                int m = (hm.length > 1) ? Integer.parseInt(hm[1]) : 0;
                return (h * 3600000L) + (m * 60000L);
            }
        } catch (Exception e) { return 0; }
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        int result = db.update(TABLE_USERS, values, "username = ?", new String[]{username});
        return result > 0;
    }

    public void deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, "username = ?", new String[]{username});
    }

    public boolean updateLectureStatus(int courseId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        int result = db.update(TABLE_TIMETABLE, values, "course_id = ?", new String[]{String.valueOf(courseId)});
        return result > 0;
    }

    public String getLectureNotes(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT notes FROM " + TABLE_TIMETABLE + " WHERE course_id = ?", new String[]{String.valueOf(courseId)});
        String notes = "";
        if (cursor.moveToFirst()) notes = cursor.getString(0);
        cursor.close();
        return notes != null ? notes : "";
    }

    public boolean updateLectureNotes(int courseId, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("notes", notes);
        int result = db.update(TABLE_TIMETABLE, values, "course_id = ?", new String[]{String.valueOf(courseId)});
        return result > 0;
    }

    public boolean updateTimetableEntry(int courseId, String teacher, String subject, String classroom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("teacher_name", teacher);
        values.put("subject_name", subject);
        values.put("class_name", classroom);
        int result = db.update(TABLE_TIMETABLE, values, "course_id = ?", new String[]{String.valueOf(courseId)});
        return result > 0;
    }

    public boolean addTimetableHistory(TimetableHistory history) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("course_name", history.getCourseName());
        values.put("pdf_path", history.getPdfPath());
        values.put("created_at", history.getCreatedAt());
        long result = db.insert(TABLE_TIMETABLE_HISTORY, null, values);
        return result != -1;
    }

    public ArrayList<TimetableHistory> getAllTimetableHistory() {
        ArrayList<TimetableHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TIMETABLE_HISTORY + " ORDER BY created_at DESC", null);
        if (cursor.moveToFirst()) {
            do {
                historyList.add(new TimetableHistory(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return historyList;
    }

    public void deleteTimetableHistory(int historyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMETABLE_HISTORY, "history_id = ?", new String[]{String.valueOf(historyId)});
    }

    public void clearAllTimetableHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMETABLE_HISTORY, null, null);
    }

    public int getAttendancePercentage() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT status FROM " + TABLE_TIMETABLE + " WHERE status IS NOT NULL AND subject_name != 'Recess' AND subject_name != 'Free'", null);
        int total = cursor.getCount();
        if (total == 0) {
            cursor.close();
            return 0;
        }
        int attended = 0;
        if (cursor.moveToFirst()) {
            do {
                if ("Attended".equals(cursor.getString(0))) attended++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return (attended * 100) / total;
    }
}
