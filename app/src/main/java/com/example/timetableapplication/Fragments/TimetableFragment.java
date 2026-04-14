package com.example.timetableapplication.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.AddPersonalTimetableActivity;
import com.example.timetableapplication.AddTimetableActivity;
import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.R;
import com.example.timetableapplication.adapter.TimetableAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TimetableFragment extends Fragment {

    private DBHelper dbHelper;
    private RecyclerView rvTimetable;
    private TableLayout tableTimetable;
    private View weeklyScrollView;
    private GridLayout legend_grid; 
    private View fabContainer;
    private FloatingActionButton fab_add_timetable;
    private Button btnSharePdf;
    private ImageButton btnFilterFaculty, btnFilterSubject, btnLegendInfo;
    private SearchView searchView;
    private TextView tvCourseTitle;
    private View legend_card;
    private String role;
    private String username;
    private boolean isWeeklyView = false;
    
    private String currentSearchQuery = "";
    private String selectedFaculty = "All";
    private String selectedSubject = "All";
    
    private final List<String> daysOrder = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT");
    private ArrayList<CourseModel> fullTimetableList = new ArrayList<>();
    private ArrayList<CourseModel> filteredTimetableList = new ArrayList<>();

    private float dX, dY;
    private static final int CLICK_DRAG_TOLERANCE = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        dbHelper = new DBHelper(getContext());
        SharedPreferences preferences = getActivity().getSharedPreferences("user_details", Context.MODE_PRIVATE);
        role = preferences.getString("role", "Student");
        username = preferences.getString("username", "");

        rvTimetable = view.findViewById(R.id.rvTimetable);
        tableTimetable = view.findViewById(R.id.tableTimetable);
        weeklyScrollView = view.findViewById(R.id.weeklyScrollView);
        legend_grid = view.findViewById(R.id.legend_grid); 
        legend_card = view.findViewById(R.id.legend_card);
        fabContainer = view.findViewById(R.id.fab_container);
        fab_add_timetable = view.findViewById(R.id.fab_add_timetable);
        btnSharePdf = view.findViewById(R.id.btnSharePdf);
        tvCourseTitle = view.findViewById(R.id.tvCourseTitle);
        searchView = view.findViewById(R.id.searchView);
        btnFilterFaculty = view.findViewById(R.id.btnFilterFaculty);
        btnFilterSubject = view.findViewById(R.id.btnFilterSubject);
        btnLegendInfo = view.findViewById(R.id.btnLegendInfo);

        if (rvTimetable != null) {
            rvTimetable.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        setupToggle(view);
        setupSearchAndFilters();

        if (fab_add_timetable != null) {
            setupMovableFab();
        }

        if (btnSharePdf != null) {
            btnSharePdf.setOnClickListener(v -> showExportOptionsDialog());
        }

        if (btnLegendInfo != null) {
            btnLegendInfo.setOnClickListener(v -> {
                if (legend_card != null) {
                    if (legend_card.getVisibility() == View.VISIBLE) legend_card.setVisibility(View.GONE);
                    else legend_card.setVisibility(View.VISIBLE);
                }
            });
        }
        loadData();

        return view;
    }

    private void setupMovableFab() {
        if (fabContainer == null) return;

        fab_add_timetable.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = fabContainer.getX() - event.getRawX();
                        dY = fabContainer.getY() - event.getRawY();
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        View parent = (View) fabContainer.getParent();
                        if (newX < 0) newX = 0;
                        if (newX > parent.getWidth() - fabContainer.getWidth())
                            newX = parent.getWidth() - fabContainer.getWidth();
                        
                        if (newY < 0) newY = 0;
                        if (newY > parent.getHeight() - fabContainer.getHeight())
                            newY = parent.getHeight() - fabContainer.getHeight();

                        fabContainer.animate().x(newX).y(newY).setDuration(0).start();

                        if (Math.abs(event.getRawX() - initialX) > CLICK_DRAG_TOLERANCE || 
                            Math.abs(event.getRawY() - initialY) > CLICK_DRAG_TOLERANCE) {
                            isDragging = true;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            showCreationOptionsDialog();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void showCreationOptionsDialog() {
        if (getContext() == null) return;
        
        List<String> optionsList = new ArrayList<>();
        optionsList.add("Create Personal Timetable");
        if ("Teacher".equalsIgnoreCase(role)) {
            optionsList.add("Create School/College Timetable");
        }
        
        String[] options = optionsList.toArray(new String[0]);
        new AlertDialog.Builder(getContext())
                .setTitle("Select Timetable Type")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(getActivity(), AddPersonalTimetableActivity.class));
                    } else {
                        Intent intent = new Intent(getActivity(), AddTimetableActivity.class);
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void setupToggle(View view) {
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    isWeeklyView = (checkedId == R.id.btnWeeklyView);
                    updateView();
                }
            });
        }
    }

    private void setupSearchAndFilters() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentSearchQuery = query;
                    applyFilters();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentSearchQuery = newText;
                    applyFilters();
                    return true;
                }
            });
        }

        if (btnFilterFaculty != null) btnFilterFaculty.setOnClickListener(v -> showFacultyFilterDialog());
        if (btnFilterSubject != null) btnFilterSubject.setOnClickListener(v -> showSubjectFilterDialog());
    }

    private void showFacultyFilterDialog() {
        if (getContext() == null) return;
        Set<String> facultySet = new HashSet<>();
        facultySet.add("All");
        for (CourseModel course : fullTimetableList) {
            if (course.getTeacherName() != null && !course.getTeacherName().isEmpty()) {
                facultySet.add(course.getTeacherName());
            }
        }
        String[] faculties = facultySet.toArray(new String[0]);
        Arrays.sort(faculties);

        int checkedItem = 0;
        for (int i = 0; i < faculties.length; i++) if (faculties[i].equals(selectedFaculty)) checkedItem = i;

        new AlertDialog.Builder(getContext())
                .setTitle("Filter by Faculty")
                .setSingleChoiceItems(faculties, checkedItem, (dialog, which) -> {
                    selectedFaculty = faculties[which];
                    applyFilters();
                    dialog.dismiss();
                }).show();
    }

    private void showSubjectFilterDialog() {
        if (getContext() == null) return;
        Set<String> subjectSet = new HashSet<>();
        subjectSet.add("All");
        for (CourseModel course : fullTimetableList) {
            if (!"Recess".equalsIgnoreCase(course.getSubjectName()) && !"Free".equalsIgnoreCase(course.getSubjectName())) {
                subjectSet.add(course.getSubjectName());
            }
        }
        String[] subjects = subjectSet.toArray(new String[0]);
        Arrays.sort(subjects);

        int checkedItem = 0;
        for (int i = 0; i < subjects.length; i++) if (subjects[i].equals(selectedSubject)) checkedItem = i;

        new AlertDialog.Builder(getContext())
                .setTitle("Filter by Subject")
                .setSingleChoiceItems(subjects, checkedItem, (dialog, which) -> {
                    selectedSubject = subjects[which];
                    applyFilters();
                    dialog.dismiss();
                }).show();
    }

    private void applyFilters() {
        filteredTimetableList.clear();
        for (CourseModel course : fullTimetableList) {
            boolean matchesSearch = course.getSubjectName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            boolean matchesFaculty = selectedFaculty.equals("All") || (course.getTeacherName() != null && course.getTeacherName().equals(selectedFaculty));
            boolean matchesSubject = selectedSubject.equals("All") || (course.getSubjectName() != null && course.getSubjectName().equals(selectedSubject));

            if (matchesSearch && matchesFaculty && matchesSubject) {
                filteredTimetableList.add(course);
            }
        }
        updateView();
    }

    private void loadData() {
        if (getContext() == null) return;
        fullTimetableList = dbHelper.getUserTimetable(username);
        applyFilters();
    }

    private void updateView() {
        if (fullTimetableList.isEmpty()) {
            if (tvCourseTitle != null) tvCourseTitle.setText("No Timetable Found");
            if (legend_card != null) legend_card.setVisibility(View.GONE);
            if (rvTimetable != null) rvTimetable.setVisibility(View.GONE);
            if (weeklyScrollView != null) weeklyScrollView.setVisibility(View.GONE);
            return;
        }

        if (tvCourseTitle != null) tvCourseTitle.setText(fullTimetableList.get(0).getCourseName());
        
        if (isWeeklyView) {
            if (rvTimetable != null) rvTimetable.setVisibility(View.GONE);
            if (weeklyScrollView != null) weeklyScrollView.setVisibility(View.VISIBLE);
            drawWeeklyGrid();
        } else {
            if (rvTimetable != null) rvTimetable.setVisibility(View.VISIBLE);
            if (weeklyScrollView != null) weeklyScrollView.setVisibility(View.GONE);
            drawDailyList();
        }
        updateLegend();
    }

    private void drawDailyList() {
        String today = new SimpleDateFormat("EEE", Locale.US).format(new Date()).toUpperCase();
        if (today.equals("SUN")) today = "MON";
        
        ArrayList<CourseModel> todayList = new ArrayList<>();
        for (CourseModel course : filteredTimetableList) {
            if (course.getDay().equalsIgnoreCase(today)) todayList.add(course);
        }

        int[] colors = getColorArray();
        TimetableAdapter adapter = new TimetableAdapter(getContext(), todayList, colors, this::showLectureDetailsBottomSheet);
        if (rvTimetable != null) rvTimetable.setAdapter(adapter);
    }

    private void drawWeeklyGrid() {
        if (tableTimetable == null) return;
        tableTimetable.removeAllViews();
        Map<String, Map<String, CourseModel>> groupedByTime = new LinkedHashMap<>();
        for (CourseModel course : filteredTimetableList) {
            groupedByTime.computeIfAbsent(course.getTimeslot(), k -> new LinkedHashMap<>());
            Map<String, CourseModel> dayMap = groupedByTime.get(course.getTimeslot());
            if (dayMap != null) dayMap.put(course.getDay().toUpperCase(), course);
        }

        TableRow headerRow = new TableRow(getContext());
        headerRow.addView(createHeaderTextView("TIME"));
        for (String day : daysOrder) headerRow.addView(createHeaderTextView(day));
        tableTimetable.addView(headerRow);

        int[] colors = getColorArray();
        for (String timeslot : groupedByTime.keySet()) {
            TableRow row = new TableRow(getContext());
            row.addView(createTimeCellTextView(timeslot));
            Map<String, CourseModel> rowData = groupedByTime.get(timeslot);
            for (String day : daysOrder) {
                CourseModel course = rowData != null ? rowData.get(day) : null;
                addCellToRow(row, course, colors);
            }
            tableTimetable.addView(row);
        }
    }

    private void addCellToRow(TableRow row, CourseModel course, int[] colors) {
        TextView classCell;
        if (course != null) {
            String statusPrefix = getStatusPrefix(course.getStatus());
            int c;
            if ("Recess".equalsIgnoreCase(course.getSubjectName()) || "Free".equalsIgnoreCase(course.getSubjectName())) {
                c = Color.parseColor("#FFF9C4");
                classCell = createStyledTextView(statusPrefix + course.getSubjectName(), 0, c);
            } else {
                String subAbbr = getAbbreviation(course.getSubjectName(), 8);
                String teachAbbr = getAbbreviation(course.getTeacherName(), 10);
                
                SpannableString content = new SpannableString(statusPrefix + subAbbr + "\n(" + teachAbbr + ")");
                content.setSpan(new StyleSpan(Typeface.BOLD), statusPrefix.length(), statusPrefix.length() + subAbbr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                int cIndex = colors.length > 0 ? Math.abs(course.getSubjectName().hashCode()) % colors.length : 0;
                c = colors.length > 0 ? colors[cIndex] : Color.LTGRAY;
                classCell = createStyledTextView(content, 0, c);
                classCell.setOnClickListener(v -> showLectureDetailsBottomSheet(course));
            }
        } else {
            classCell = createStyledTextView("", 0, Color.WHITE);
        }
        row.addView(classCell);
    }

    private void showLectureDetailsBottomSheet(CourseModel course) {
        if (getContext() == null) return;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_lecture_details, null);
        
        TextView tvSubject = sheetView.findViewById(R.id.bsSubject);
        TextView tvFaculty = sheetView.findViewById(R.id.bsFaculty);
        TextView tvClassroom = sheetView.findViewById(R.id.bsClassroom);
        EditText etNotes = sheetView.findViewById(R.id.etLectureNotes);
        Button btnSaveNotes = sheetView.findViewById(R.id.btnSaveNotes);
        Button btnMarkAttended = sheetView.findViewById(R.id.btnMarkAttended);
        Button btnMarkSkipped = sheetView.findViewById(R.id.btnMarkSkipped);
        ImageButton btnEditLecture = sheetView.findViewById(R.id.btnEditLecture);

        if (tvSubject != null) tvSubject.setText(course.getSubjectName());
        if (tvFaculty != null) tvFaculty.setText(String.format("Faculty: %s", course.getTeacherName()));
        if (tvClassroom != null) tvClassroom.setText(String.format("Classroom: %s", (course.getClassName() != null && !course.getClassName().isEmpty()) ? course.getClassName() : "Not Assigned"));

        if (etNotes != null) etNotes.setText(dbHelper.getLectureNotes(course.getCourseid()));

        // Edit functionality for Teachers
        if ("Teacher".equalsIgnoreCase(role)) {
            if (btnEditLecture != null) {
                btnEditLecture.setVisibility(View.VISIBLE);
                btnEditLecture.setOnClickListener(v -> {
                    showEditLectureDialog(course, bottomSheetDialog);
                });
            }
        }

        // Restriction: Students can't edit School/College timetables
        if ("Student".equalsIgnoreCase(role) && !"Personal Timetable".equals(course.getCourseName())) {
            if (btnSaveNotes != null) btnSaveNotes.setVisibility(View.GONE);
            if (btnMarkAttended != null) btnMarkAttended.setVisibility(View.GONE);
            if (btnMarkSkipped != null) btnMarkSkipped.setVisibility(View.GONE);
            if (etNotes != null) {
                etNotes.setFocusable(false);
                etNotes.setClickable(false);
                etNotes.setHint("Notes can only be edited by teachers");
            }
        }

        if (btnSaveNotes != null) {
            btnSaveNotes.setOnClickListener(v -> {
                if (etNotes != null) {
                    String newNotes = etNotes.getText().toString().trim();
                    if (dbHelper.updateLectureNotes(course.getCourseid(), newNotes)) {
                        Toast.makeText(getContext(), "Notes saved!", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                }
            });
        }

        if (btnMarkAttended != null) {
            btnMarkAttended.setOnClickListener(v -> {
                dbHelper.updateLectureStatus(course.getCourseid(), "Attended");
                loadData();
                bottomSheetDialog.dismiss();
            });
        }

        if (btnMarkSkipped != null) {
            btnMarkSkipped.setOnClickListener(v -> {
                dbHelper.updateLectureStatus(course.getCourseid(), "Skipped");
                loadData();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void showEditLectureDialog(CourseModel course, BottomSheetDialog parentSheet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Lecture Details");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etSub = new EditText(getContext());
        etSub.setHint("Subject Name");
        etSub.setText(course.getSubjectName());
        layout.addView(etSub);

        final EditText etFac = new EditText(getContext());
        etFac.setHint("Faculty Name");
        etFac.setText(course.getTeacherName());
        layout.addView(etFac);

        final EditText etRoom = new EditText(getContext());
        etRoom.setHint("Classroom/Lab");
        etRoom.setText(course.getClassName());
        layout.addView(etRoom);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newSub = etSub.getText().toString().trim();
            String newFac = etFac.getText().toString().trim();
            String newRoom = etRoom.getText().toString().trim();

            if (!newSub.isEmpty() && !newFac.isEmpty()) {
                if (dbHelper.updateTimetableEntry(course.getCourseid(), newFac, newSub, newRoom)) {
                    Toast.makeText(getContext(), "Timetable Updated!", Toast.LENGTH_SHORT).show();
                    loadData();
                    parentSheet.dismiss();
                }
            } else {
                Toast.makeText(getContext(), "Subject and Faculty cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateLegend() {
        if (legend_grid == null) return;
        Map<String, String> subjectLegendMap = new LinkedHashMap<>();
        Map<String, String> teacherLegendMap = new LinkedHashMap<>();
        for (CourseModel course : filteredTimetableList) {
            if (!"Recess".equalsIgnoreCase(course.getSubjectName()) && !"Free".equalsIgnoreCase(course.getSubjectName())) {
                String subAbbr = getAbbreviation(course.getSubjectName(), 8);
                String teachAbbr = getAbbreviation(course.getTeacherName(), 10);
                if (!subAbbr.equalsIgnoreCase(course.getSubjectName())) subjectLegendMap.put(subAbbr, course.getSubjectName());
                if (course.getTeacherName() != null && !teachAbbr.equalsIgnoreCase(course.getTeacherName())) teacherLegendMap.put(teachAbbr, course.getTeacherName());
            }
        }
        if (subjectLegendMap.isEmpty() && teacherLegendMap.isEmpty()) {
            if (legend_card != null) legend_card.setVisibility(View.GONE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (!subjectLegendMap.isEmpty()) {
            sb.append("Subjects: ");
            for (Map.Entry<String, String> entry : subjectLegendMap.entrySet()) sb.append(entry.getKey()).append(" - ").append(entry.getValue()).append("; ");
            sb.append("\n\n");
        }
        if (!teacherLegendMap.isEmpty()) {
            sb.append("Teachers: ");
            for (Map.Entry<String, String> entry : teacherLegendMap.entrySet()) sb.append(entry.getKey()).append(" - ").append(entry.getValue()).append("; ");
        }
        legend_grid.removeAllViews();
        TextView tvLegend = new TextView(getContext());
        tvLegend.setText(sb.toString());
        tvLegend.setTextSize(14);
        tvLegend.setTextColor(Color.DKGRAY);
        legend_grid.addView(tvLegend);
    }

    private void showExportOptionsDialog() {
        if (getContext() == null || filteredTimetableList.isEmpty()) return;
        String[] options = {"Export with Colors", "Export without Color (B&W)"};
        new AlertDialog.Builder(getContext()).setTitle("Choose Export Option").setItems(options, (dialog, which) -> generateAndSharePdf(which == 0)).show();
    }

    private void generateAndSharePdf(boolean withColor) {
        if (filteredTimetableList.isEmpty() || getContext() == null) return;
        String courseName = filteredTimetableList.get(0).getCourseName();
        File pdfDir = new File(getContext().getExternalFilesDir(null), "pdfs");
        if (!pdfDir.exists() && !pdfDir.mkdirs()) return;
        File pdfFile = new File(pdfDir, String.format(Locale.US, "%s_%d.pdf", courseName, System.currentTimeMillis()));
        
        SharedPreferences preferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
        int orientation = preferences.getInt("timetable_orientation", 0);

        boolean success = false;
        try (PdfWriter writer = new PdfWriter(pdfFile); PdfDocument pdf = new PdfDocument(writer); Document document = new Document(pdf)) {
            document.add(new Paragraph(courseName).setBold().setFontSize(20).setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER));

            if (orientation == 1) generateHorizontalPdf(document, withColor);
            else generateClassicPdf(document, withColor);

            success = true;
        } catch (Exception e) { 
            e.printStackTrace(); 
            Toast.makeText(getContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show();
        }

        if (success) {
            dbHelper.addTimetableHistory(new TimetableHistory(0, courseName, pdfFile.getAbsolutePath(), System.currentTimeMillis()));
            sharePdf(pdfFile);
        }
    }

    private void generateClassicPdf(Document document, boolean withColor) {
        float[] columnWidths = {2f, 3f, 3f, 3f, 3f, 3f, 3f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        DeviceRgb headerBg = withColor ? new DeviceRgb(248, 215, 218) : new DeviceRgb(220, 220, 220);
        table.addHeaderCell(new Cell().add(new Paragraph("TIME")).setBackgroundColor(headerBg));
        for (String day : daysOrder) table.addHeaderCell(new Cell().add(new Paragraph(day)).setBackgroundColor(headerBg));
        Map<String, Map<String, CourseModel>> groupedByTime = new LinkedHashMap<>();
        for (CourseModel course : filteredTimetableList) {
            groupedByTime.computeIfAbsent(course.getTimeslot(), k -> new LinkedHashMap<>());
            Map<String, CourseModel> dayMap = groupedByTime.get(course.getTimeslot());
            if (dayMap != null) dayMap.put(course.getDay().toUpperCase(), course);
        }
        int[] colors = getColorArray();
        for (String timeslot : groupedByTime.keySet()) {
            table.addCell(new Cell().add(new Paragraph(timeslot)).setBackgroundColor(withColor ? new DeviceRgb(253, 234, 234) : new DeviceRgb(245, 245, 245)));
            Map<String, CourseModel> rowData = groupedByTime.get(timeslot);
            for (String day : daysOrder) {
                CourseModel course = rowData != null ? rowData.get(day) : null;
                addCellToPdfTable(table, course, withColor, colors);
            }
        }
        document.add(table);
    }

    private void generateHorizontalPdf(Document document, boolean withColor) {
        Map<String, Map<String, CourseModel>> groupedByTime = new LinkedHashMap<>();
        for (CourseModel course : filteredTimetableList) {
            groupedByTime.computeIfAbsent(course.getTimeslot(), k -> new LinkedHashMap<>());
            Map<String, CourseModel> dayMap = groupedByTime.get(course.getTimeslot());
            if (dayMap != null) dayMap.put(course.getDay().toUpperCase(), course);
        }
        List<String> timeslots = new ArrayList<>(groupedByTime.keySet());
        float[] columnWidths = new float[timeslots.size() + 1];
        Arrays.fill(columnWidths, 3f); columnWidths[0] = 2f;
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        DeviceRgb headerBg = withColor ? new DeviceRgb(248, 215, 218) : new DeviceRgb(220, 220, 220);
        table.addHeaderCell(new Cell().add(new Paragraph("DAY")).setBackgroundColor(headerBg));
        for (String ts : timeslots) table.addHeaderCell(new Cell().add(new Paragraph(ts)).setBackgroundColor(headerBg));
        int[] colors = getColorArray();
        for (String day : daysOrder) {
            table.addCell(new Cell().add(new Paragraph(day)).setBackgroundColor(withColor ? new DeviceRgb(253, 234, 234) : new DeviceRgb(245, 245, 245)));
            for (String ts : timeslots) {
                CourseModel course = (groupedByTime.get(ts) != null) ? groupedByTime.get(ts).get(day) : null;
                addCellToPdfTable(table, course, withColor, colors);
            }
        }
        document.add(table);
    }

    private void addCellToPdfTable(Table table, CourseModel course, boolean withColor, int[] colors) {
        Cell cell = new Cell();
        if (course != null) {
            String status = getStatusPrefix(course.getStatus());
            String subject = course.getSubjectName();
            String teacher = ("Recess".equalsIgnoreCase(subject) || "Free".equalsIgnoreCase(subject)) ? "" : String.format("\n(%s)", course.getTeacherName());
            Paragraph p = new Paragraph().add(new com.itextpdf.layout.element.Text(status + subject).setBold());
            if (!teacher.isEmpty()) p.add(new com.itextpdf.layout.element.Text(teacher).setFontSize(9));
            cell.add(p);
            if (withColor) {
                if ("Recess".equalsIgnoreCase(subject) || "Free".equalsIgnoreCase(subject)) cell.setBackgroundColor(new DeviceRgb(255, 249, 196));
                else {
                    int cIndex = colors.length > 0 ? Math.abs(subject.hashCode()) % colors.length : 0;
                    int c = colors.length > 0 ? colors[cIndex] : Color.LTGRAY;
                    cell.setBackgroundColor(new DeviceRgb(Color.red(c), Color.green(c), Color.blue(c)));
                }
            }
        } else cell.add(new Paragraph(""));
        table.addCell(cell);
    }

    private void sharePdf(File file) {
        if (getContext() == null) return;
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share Timetable PDF"));
    }

    private String getStatusPrefix(String status) {
        if (status == null) return "";
        return "Attended".equals(status) ? "✅ " : "Skipped".equals(status) ? "❌ " : "";
    }

    private String getAbbreviation(String name, int threshold) {
        if (name == null || name.isEmpty()) return "";
        if (name.length() <= threshold && !name.contains(" ")) return name;
        StringBuilder abbr = new StringBuilder();
        for (String word : name.split("\\s+")) if (!word.isEmpty()) abbr.append(Character.toUpperCase(word.charAt(0)));
        return abbr.toString();
    }

    private int[] getColorArray() {
        if (getContext() == null) return new int[0];
        SharedPreferences preferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
        int themeIndex = preferences.getInt("timetable_theme", 0);
        int arrayId;
        switch (themeIndex) {
            case 1: arrayId = R.array.vibrant_colors; break;
            case 2: arrayId = R.array.pastel_colors; break;
            case 3: arrayId = R.array.corporate_blue_colors; break;
            case 4: arrayId = R.array.autumn_harvest_colors; break;
            case 5: arrayId = R.array.sunrise_colors; break;
            case 6: arrayId = R.array.emerald_colors; break;
            case 7: arrayId = R.array.lavender_bliss_colors; break;
            case 8: arrayId = R.array.minty_fresh_colors; break;
            default: arrayId = R.array.muted_pastels;
        }
        try (TypedArray ta = getResources().obtainTypedArray(arrayId)) {
            int[] colors = new int[ta.length()];
            for (int i = 0; i < ta.length(); i++) colors[i] = ta.getColor(i, 0);
            return colors;
        }
    }

    private TextView createStyledTextView(CharSequence text, int styleResId, int backgroundColor) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        textView.setGravity(Gravity.CENTER);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(backgroundColor);
        background.setStroke(dpToPx(1), Color.parseColor("#E0E0E0"));
        textView.setBackground(background);
        return textView;
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = createStyledTextView(text, 0, Color.parseColor("#FDEAEA"));
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(Color.parseColor("#1A237E"));
        return textView;
    }

    private TextView createTimeCellTextView(String text) {
        return createStyledTextView(text, 0, Color.parseColor("#F8D7DA"));
    }

    private int dpToPx(int dp) {
        if (getContext() == null) return dp;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
