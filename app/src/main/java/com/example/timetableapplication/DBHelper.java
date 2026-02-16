package com.example.timetableapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.ModelClass.User;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "UserDB.db";
    public static final int DB_VERSION = 9; 

    public static final String TABLE_USERS = "users";
    public static final String TABLE_TIMETABLE = "timetable";
    public static final String TABLE_TIMETABLE_HISTORY = "timetable_history";

    public DBHelper(Context context) {
        super(context, DBNAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, mobile TEXT UNIQUE, username TEXT UNIQUE, password TEXT, user_type TEXT, course TEXT, year TEXT, college_name TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_TIMETABLE + " (course_id INTEGER PRIMARY KEY AUTOINCREMENT, course_name TEXT, teacher_name TEXT, subject_name TEXT, class_name TEXT, timeslot TEXT, day TEXT)");
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
        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        return userExists;
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

    public boolean addTimetableEntry(CourseModel course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("course_name", course.getCourseName());
        values.put("teacher_name", course.getTeacherName());
        values.put("subject_name", course.getSubjectName());
        values.put("class_name", course.getClassName());
        values.put("timeslot", course.getTimeslot());
        values.put("day", course.getDay());
        long result = db.insert(TABLE_TIMETABLE, null, values);
        return result != -1;
    }

    public boolean updateTimetableEntry(CourseModel course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("course_name", course.getCourseName());
        values.put("teacher_name", course.getTeacherName());
        values.put("subject_name", course.getSubjectName());
        values.put("class_name", course.getClassName());
        values.put("timeslot", course.getTimeslot());
        values.put("day", course.getDay());
        int result = db.update(TABLE_TIMETABLE, values, "course_id = ?", new String[]{String.valueOf(course.getCourseid())});
        return result > 0;
    }

    public ArrayList<CourseModel> getAllTimetableEntries() {
        ArrayList<CourseModel> timetable = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TIMETABLE, null);
        if (cursor.moveToFirst()) {
            do {
                timetable.add(new CourseModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return timetable;
    }
    
    public void clearTimetableData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMETABLE, null, null);
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

    public CourseModel getNextClass(String day, String currentTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TIMETABLE + " WHERE day = ? AND timeslot > ? ORDER BY timeslot ASC LIMIT 1", new String[]{day, currentTime});
        CourseModel course = null;
        if (cursor.moveToFirst()) {
            course = new CourseModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
        }
        cursor.close();
        return course;
    }
    
    public int getLecturesForTeacherToday(String teacherName, String day) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TIMETABLE + " WHERE teacher_name = ? AND day = ?", new String[]{teacherName, day});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getLecturesForTeacherThisWeek(String teacherName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TIMETABLE + " WHERE teacher_name = ?", new String[]{teacherName});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
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
}
