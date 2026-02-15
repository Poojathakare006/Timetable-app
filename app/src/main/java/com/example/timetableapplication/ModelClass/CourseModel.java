package com.example.timetableapplication.ModelClass;

public class CourseModel {

    int courseid;
    String courseName;
    String TeacherName, SubjectName, ClassName;
    String Timeslot, day;

    public CourseModel(int courseid, String courseName, String TeacherName, String SubjectName, String ClassName, String Timeslot, String day)
    {
        this.courseid = courseid;
        this.courseName = courseName;
        this.TeacherName = TeacherName;
        this.SubjectName = SubjectName;
        this.ClassName = ClassName;
        this.Timeslot = Timeslot;
        this.day = day;
    }

    public int getCourseid() {
        return courseid;
    }

    public void setCourseid(int courseid) {
        this.courseid = courseid;
    }

    public String getCourseName() { // Changed return type
        return courseName;
    }

    public void setCourseName(String courseName) { // Changed parameter type
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return TeacherName;
    }

    public void setTeacherName(String teacherName) {
        TeacherName = teacherName;
    }

    public String getSubjectName() {
        return SubjectName;
    }

    public void setSubjectName(String subjectName) {
        SubjectName = subjectName;
    }

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public String getTimeslot() {
        return Timeslot;
    }

    public void setTimeslot(String timeslot) {
        Timeslot = timeslot;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
