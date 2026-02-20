package com.example.timetableapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.timetableapplication.ModelClass.CourseModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class AddTimetableActivity extends AppCompatActivity {

    // UI Elements
    Button btnAddTeacher, btnAddSubject, btnGenerateTimetable, btnAddRecess;
    Button btnAddTimeSlot, btnAddDay;
    SwitchCompat toggleOverlap, toggleClash, toggleWorkload;
    EditText etCourseName;
    TextView tvTeachersAdded, tvSubjectsAdded;

    // Data Lists
    private ArrayList<String> teachers = new ArrayList<>();
    private ArrayList<String> subjects = new ArrayList<>();
    private ArrayList<String> timeslots = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> recessSlots = new ArrayList<>();
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable);

        dbHelper = new DBHelper(this);

        // Initialize UI Elements
        etCourseName = findViewById(R.id.etCourseName);
        btnAddTeacher = findViewById(R.id.btnTimetableAddTeacher);
        btnAddSubject = findViewById(R.id.btnTimetableAddSubject);
        btnAddTimeSlot = findViewById(R.id.btnAddTimeSlot);
        btnAddDay = findViewById(R.id.btnAddDay);
        btnAddRecess = findViewById(R.id.btnAddRecess);
        btnGenerateTimetable = findViewById(R.id.btnGenerateTimetable);
        toggleOverlap = findViewById(R.id.toggleOverlap);
        toggleClash = findViewById(R.id.toggleClash);
        toggleWorkload = findViewById(R.id.toggleWorkload);

        tvTeachersAdded = findViewById(R.id.etTimetableTeachersAdded0);
        tvSubjectsAdded = findViewById(R.id.etSubAdd);

        // Set Click Listeners
        btnAddTeacher.setOnClickListener(v -> showAddDialog("Teacher Name", teachers, tvTeachersAdded));
        btnAddSubject.setOnClickListener(v -> showAddDialog("Subject Name", subjects, tvSubjectsAdded));
        btnAddTimeSlot.setOnClickListener(v -> showAddDialog("Time Slot (e.g., 9:00-10:00)", timeslots, null));
        btnAddDay.setOnClickListener(v -> showDaySelectionDialog());
        btnAddRecess.setOnClickListener(v -> showRecessSelectionDialog());

        btnGenerateTimetable.setOnClickListener(v -> generateTimetable());
    }

    private void showAddDialog(String title, final ArrayList<String> list, final TextView countView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (list.contains(value)) {
                Toast.makeText(this, "This item already exists", Toast.LENGTH_SHORT).show();
            } else if (!value.isEmpty()) {
                list.add(value);
                if (countView != null) {
                    countView.setText("Added : " + list.size());
                }
                Toast.makeText(this, title + " added: " + value, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Value cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDaySelectionDialog() {
        final String[] dayOptions = {"MON", "TUE", "WED", "THU", "FRI", "SAT"};
        final boolean[] checkedItems = new boolean[dayOptions.length];

        for (int i = 0; i < dayOptions.length; i++) {
            if (days.contains(dayOptions[i])) {
                checkedItems[i] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Days");
        builder.setMultiChoiceItems(dayOptions, checkedItems, (dialog, which, isChecked) -> {});

        builder.setPositiveButton("OK", (dialog, which) -> {
            days.clear();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    days.add(dayOptions[i]);
                }
            }
            Toast.makeText(this, "Days selection updated.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showRecessSelectionDialog() {
        if (timeslots.isEmpty()) {
            Toast.makeText(this, "Please add time slots first", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] timeSlotOptions = timeslots.toArray(new String[0]);
        final boolean[] checkedItems = new boolean[timeSlotOptions.length];

        for (int i = 0; i < timeSlotOptions.length; i++) {
            if (recessSlots.contains(timeSlotOptions[i])) {
                checkedItems[i] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Recess Time Slots");
        builder.setMultiChoiceItems(timeSlotOptions, checkedItems, (dialog, which, isChecked) -> {});

        builder.setPositiveButton("OK", (dialog, which) -> {
            recessSlots.clear();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    recessSlots.add(timeSlotOptions[i]);
                }
            }
            Toast.makeText(this, "Recess selection updated.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }


    private void generateTimetable() {
        String courseName = etCourseName.getText().toString().trim();

        if (courseName.isEmpty() || teachers.isEmpty() || subjects.isEmpty() || timeslots.isEmpty() || days.isEmpty()) {
            Toast.makeText(this, "Please add course name and at least one teacher, subject, timeslot, and day", Toast.LENGTH_LONG).show();
            return;
        }

        dbHelper.clearTimetableData();

        for (String day : days) {
            Collections.shuffle(teachers);
            Collections.shuffle(subjects);
            Iterator<String> teacherIt = teachers.iterator();
            Iterator<String> subjectIt = subjects.iterator();

            for (String timeslot : timeslots) {
                if (recessSlots.contains(timeslot)) {
                    CourseModel course = new CourseModel(0, courseName, "", "Recess", "", timeslot, day, null);
                    dbHelper.addTimetableEntry(course);
                } else {
                    if (!teacherIt.hasNext()) teacherIt = teachers.iterator();
                    if (!subjectIt.hasNext()) subjectIt = subjects.iterator();

                    CourseModel course = new CourseModel(0, courseName, teacherIt.next(), subjectIt.next(), "Default Class", timeslot, day, null);
                    dbHelper.addTimetableEntry(course);
                }
            }
        }

        Toast.makeText(this, "Timetable Generated Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
