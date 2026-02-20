package com.example.timetableapplication.ModelClass;

public class TimetableHistory {
    private int historyId;
    private String courseName;
    private String pdfPath;
    private long createdAt;

    public TimetableHistory(int historyId, String courseName, String pdfPath, long createdAt) {
        this.historyId = historyId;
        this.courseName = courseName;
        this.pdfPath = pdfPath;
        this.createdAt = createdAt;
    }

    public int getHistoryId() {
        return historyId;
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
