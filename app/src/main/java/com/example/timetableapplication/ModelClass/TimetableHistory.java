package com.example.timetableapplication.ModelClass;

public class TimetableHistory {
    private int id;
    private String courseName;
    private String pdfPath;
    private long createdAt;

    public TimetableHistory(int id, String courseName, String pdfPath, long createdAt) {
        this.id = id;
        this.courseName = courseName;
        this.pdfPath = pdfPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
