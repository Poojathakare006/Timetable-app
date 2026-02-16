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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddTimetableActivity extends AppCompatActivity {

    // UI Elements
    Button btnAddTeacher, btnAddSubject, btnGenerateTimetable, btnAddRecess, btnSetTeacherAvailability;
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
    private Map<String, List<String>> teacherAvailability = new HashMap<>();
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable);

        dbHelper = new DBHelper(this);

        etCourseName = findViewById(R.id.etCourseName);
        btnAddTeacher = findViewById(R.id.btnTimetableAddTeacher);
        btnAddSubject = findViewById(R.id.btnTimetableAddSubject);
        btnAddTimeSlot = findViewById(R.id.btnAddTimeSlot);
        btnAddDay = findViewById(R.id.btnAddDay);
        btnAddRecess = findViewById(R.id.btnAddRecess);
        btnGenerateTimetable = findViewById(R.id.btnGenerateTimetable);
        btnSetTeacherAvailability = findViewById(R.id.btnSetTeacherAvailability);
        toggleOverlap = findViewById(R.id.toggleOverlap);
        toggleClash = findViewById(R.id.toggleClash);
        toggleWorkload = findViewById(R.id.toggleWorkload);
        tvTeachersAdded = findViewById(R.id.etTimetableTeachersAdded0);
        tvSubjectsAdded = findViewById(R.id.etSubAdd);

        btnAddTeacher.setOnClickListener(v -> showAddDialog("Teacher Name", teachers, tvTeachersAdded));
        btnAddSubject.setOnClickListener(v -> showAddDialog("Subject Name", subjects, tvSubjectsAdded));
        btnAddTimeSlot.setOnClickListener(v -> showAddDialog("Time Slot (e.g., 9:00-10:00)", timeslots, null));
        btnAddDay.setOnClickListener(v -> showDaySelectionDialog());
        btnAddRecess.setOnClickListener(v -> showRecessSelectionDialog());
        btnSetTeacherAvailability.setOnClickListener(v -> showTeacherSelectionDialog());
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
        builder.setMultiChoiceItems(dayOptions, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);

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
        builder.setMultiChoiceItems(timeSlotOptions, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);

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

    private void showTeacherSelectionDialog() {
        if (teachers.isEmpty()) {
            Toast.makeText(this, "Please add teachers first", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] teacherOptions = teachers.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Select Teacher for Availability")
                .setItems(teacherOptions, (dialog, which) -> {
                    showAvailabilityDialog(teacherOptions[which]);
                })
                .show();
    }

    private void showAvailabilityDialog(final String teacherName) {
        if (timeslots.isEmpty()) {
            Toast.makeText(this, "Please add time slots first", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] timeSlotOptions = timeslots.toArray(new String[0]);
        final boolean[] checkedItems = new boolean[timeSlotOptions.length];
        final List<String> unavailableSlots = teacherAvailability.getOrDefault(teacherName, new ArrayList<>());

        for (int i = 0; i < timeSlotOptions.length; i++) {
            if (unavailableSlots.contains(timeSlotOptions[i])) {
                checkedItems[i] = true;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Set Unavailable Times for " + teacherName)
                .setMultiChoiceItems(timeSlotOptions, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton("Save", (dialog, which) -> {
                    List<String> newUnavailableSlots = new ArrayList<>();
                    for (int i = 0; i < timeSlotOptions.length; i++) {
                        if (checkedItems[i]) {
                            newUnavailableSlots.add(timeSlotOptions[i]);
                        }
                    }
                    teacherAvailability.put(teacherName, newUnavailableSlots);
                    Toast.makeText(this, "Availability for " + teacherName + " updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

            for (String timeslot : timeslots) {
                if (recessSlots.contains(timeslot)) {
                    CourseModel course = new CourseModel(0, courseName, "", "Recess", "", timeslot, day);
                    dbHelper.addTimetableEntry(course);
                    continue; 
                }

                boolean assigned = false;
                for (int i = 0; i < teachers.size(); i++) {
                    String teacher = teachers.get(i);
                    List<String> unavailable = teacherAvailability.get(teacher);
                    if (unavailable != null && unavailable.contains(timeslot)) {
                        continue; 
                    }

                    String subject = subjects.get(i % subjects.size());

                    CourseModel course = new CourseModel(0, courseName, teacher, subject, "Default Class", timeslot, day);
                    dbHelper.addTimetableEntry(course);
                    assigned = true;
                    break; 
                }

                if (!assigned) {
                    // Handle case where no available teacher was found for a slot
                    CourseModel course = new CourseModel(0, courseName, "Unassigned", "Unassigned", "Default Class", timeslot, day);
                    dbHelper.addTimetableEntry(course);
                }
            }
        }

        Toast.makeText(this, "Timetable Generated Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
