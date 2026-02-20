package com.example.timetableapplication.ModelClass;

public class SubjectAnalytics {
    private String subjectName;
    private int attendedLectures;
    private int totalLectures;

    public SubjectAnalytics(String subjectName) {
        this.subjectName = subjectName;
        this.attendedLectures = 0;
        this.totalLectures = 0;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getAttendedLectures() {
        return attendedLectures;
    }

    public int getTotalLectures() {
        return totalLectures;
    }

    public void incrementAttendedLectures() {
        this.attendedLectures++;
    }

    public void incrementTotalLectures() {
        this.totalLectures++;
    }

    public int getAttendancePercentage() {
        if (totalLectures == 0) {
            return 0;
        }
        return (int) (((double) attendedLectures / totalLectures) * 100);
    }
}
