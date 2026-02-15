package com.example.timetableapplication.ModelClass;

public class User {
    private String name;
    private String email;
    private String mobile;
    private String username;
    private String userType;
    private String course;
    private String year;
    private String collegeName;

    public User(String name, String email, String mobile, String username, String userType, String course, String year, String collegeName) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.username = username;
        this.userType = userType;
        this.course = course;
        this.year = year;
        this.collegeName = collegeName;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getUsername() { return username; }
    public String getUserType() { return userType; }
    public String getCourse() { return course; }
    public String getYear() { return year; }
    public String getCollegeName() { return collegeName; }
}
