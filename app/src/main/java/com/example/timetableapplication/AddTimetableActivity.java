package com.example.timetableapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.TimetableHistory;

import java.util.ArrayList;
import java.util.Random;

public class AddTimetableActivity extends AppCompatActivity {

    private EditText etCourseName;
    private Button btnAddTeacher, btnAddSubject, btnAddTimeSlot, btnAddDay,
            btnAddRecess, btnAddLabTiming, btnGenerateTimetable;
    private TextView tvTeachersAdded, tvSubjectsAdded;

    private ArrayList<String> teachers = new ArrayList<>();
    private ArrayList<String> subjects = new ArrayList<>();
    private ArrayList<String> timeslots = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> recessSlots = new ArrayList<>();
    private ArrayList<String> labSlots = new ArrayList<>();

    private DBHelper dbHelper;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable);

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        etCourseName = findViewById(R.id.etCourseName);
        btnAddTeacher = findViewById(R.id.btnTimetableAddTeacher);
        btnAddSubject = findViewById(R.id.btnTimetableAddSubject);
        btnAddTimeSlot = findViewById(R.id.btnAddTimeSlot);
        btnAddDay = findViewById(R.id.btnAddDay);
        btnAddRecess = findViewById(R.id.btnAddRecess);
        btnAddLabTiming = findViewById(R.id.btnAddLabTiming);
        btnGenerateTimetable = findViewById(R.id.btnGenerateTimetable);

        tvTeachersAdded = findViewById(R.id.etTimetableTeachersAdded0);
        tvSubjectsAdded = findViewById(R.id.etSubAdd);

        btnAddTeacher.setOnClickListener(v ->
                showAddDialog("Teacher Name", teachers, tvTeachersAdded));

        btnAddSubject.setOnClickListener(v ->
                showAddDialog("Subject Name", subjects, tvSubjectsAdded));

        btnAddTimeSlot.setOnClickListener(v ->
                showAddDialog("Time Slot (Example: 10:00 AM - 11:00 AM)", timeslots, null));

        btnAddDay.setOnClickListener(v -> showDaySelectionDialog());
        btnAddRecess.setOnClickListener(v -> showRecessSelectionDialog());
        btnAddLabTiming.setOnClickListener(v -> showLabSelectionDialog());
        btnGenerateTimetable.setOnClickListener(v -> generateTimetable());
    }

    private void generateTimetable() {
        String courseName = etCourseName.getText().toString().trim();

        if (courseName.isEmpty() || teachers.isEmpty() || subjects.isEmpty() || timeslots.isEmpty() || days.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and add necessary items.", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.clearTimetableData();

        String username = preferences.getString("username", "Unknown");

        Random random = new Random();
        for (String day : days) {
            for (String slot : timeslots) {
                String teacher = "";
                String subject;

                if (recessSlots.contains(slot)) {
                    subject = "Recess";
                } else if (labSlots.contains(slot)) {
                    teacher = teachers.get(random.nextInt(teachers.size()));
                    subject = subjects.get(random.nextInt(subjects.size())) + " (Lab)";
                } else {
                    teacher = teachers.get(random.nextInt(teachers.size()));
                    subject = subjects.get(random.nextInt(subjects.size()));
                }

                // Constructor: id, courseName, teacher, subject, className, timeslot, day, status
                CourseModel course = new CourseModel(0, courseName, teacher, subject, "", slot, day, null);
                dbHelper.addTimetableEntry(course, username);
            }
        }

        // Add to history
        dbHelper.addTimetableHistory(new TimetableHistory(0, courseName, "Generated Locally", System.currentTimeMillis()));

        Toast.makeText(this, "Timetable Generated Successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showAddDialog(String title, ArrayList<String> list, TextView counter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add " + title);
        EditText input = new EditText(this);
        input.setPadding(40, 20, 40, 20);
        builder.setView(input);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty() && !list.contains(value)) {
                list.add(value);
                if (counter != null) counter.setText("Added : " + list.size());
                Toast.makeText(this, value + " added.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDaySelectionDialog() {
        final String[] options = {"MON", "TUE", "WED", "THU", "FRI", "SAT"};
        final boolean[] checked = new boolean[options.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Days");
        builder.setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            days.clear();
            for (int i = 0; i < options.length; i++) {
                if (checked[i]) days.add(options[i]);
            }
            Toast.makeText(this, days.size() + " days selected.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showRecessSelectionDialog() {
        if (timeslots.isEmpty()) {
            Toast.makeText(this, "Add timeslots first", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] options = timeslots.toArray(new String[0]);
        final boolean[] checked = new boolean[options.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Recess Slots");
        builder.setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            recessSlots.clear();
            for (int i = 0; i < options.length; i++) {
                if (checked[i]) recessSlots.add(options[i]);
            }
            Toast.makeText(this, recessSlots.size() + " recess slots set.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showLabSelectionDialog() {
        if (timeslots.isEmpty()) {
            Toast.makeText(this, "Add timeslots first", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] options = timeslots.toArray(new String[0]);
        final boolean[] checked = new boolean[options.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Lab/Practical Slots");
        builder.setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            labSlots.clear();
            for (int i = 0; i < options.length; i++) {
                if (checked[i]) labSlots.add(options[i]);
            }
            Toast.makeText(this, labSlots.size() + " lab slots set.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
