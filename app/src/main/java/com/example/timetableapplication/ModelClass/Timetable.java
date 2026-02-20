package com.example.timetableapplication.ModelClass;

public class Timetable {
    public String courseName;
    public String creatorUid;
    public String shareCode;

    public Timetable() {
        // Default constructor required for calls to DataSnapshot.getValue(Timetable.class)
    }

    public Timetable(String courseName, String creatorUid, String shareCode) {
        this.courseName = courseName;
        this.creatorUid = creatorUid;
        this.shareCode = shareCode.toUpperCase();
    }
}
