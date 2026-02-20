package com.example.timetableapplication.Fragments;

import android.content.ActivityNotFoundException;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.timetableapplication.AddTimetableActivity;
import com.example.timetableapplication.DBHelper;
import com.example.timetableapplication.ModelClass.CourseModel;
import com.example.timetableapplication.ModelClass.TimetableHistory;
import com.example.timetableapplication.R;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimetableFragment extends Fragment {

    DBHelper dbHelper;
    TableLayout tableLayout;
    FloatingActionButton fab_add_timetable;
    CardView legendCard;
    GridLayout legendGrid;
    List<String> daysOrder = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        dbHelper = new DBHelper(getContext());
        tableLayout = view.findViewById(R.id.tableTimetable);
        fab_add_timetable = view.findViewById(R.id.fab_add_timetable);
        legendCard = view.findViewById(R.id.legend_card);
        legendGrid = view.findViewById(R.id.legend_grid);

        fab_add_timetable.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), AddTimetableActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.timetable_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share_pdf) {
            generateAndSharePdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTimetable();
    }

    private void loadTimetable() {
        if (getContext() == null) return;

        ArrayList<CourseModel> timetableList = dbHelper.getAllTimetableEntries();
        tableLayout.removeAllViews();
        legendGrid.removeAllViews();

        if (timetableList.isEmpty()) {
            legendCard.setVisibility(View.GONE);
            return;
        }

        Map<String, Map<String, CourseModel>> groupedByTime = new LinkedHashMap<>();
        for (CourseModel course : timetableList) {
            groupedByTime.computeIfAbsent(course.getTimeslot(), k -> new LinkedHashMap<>());
            groupedByTime.get(course.getTimeslot()).put(course.getDay().toUpperCase(), course);
        }

        List<String> timeslots = new ArrayList<>(groupedByTime.keySet());

        SharedPreferences preferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
        int orientation = preferences.getInt("timetable_orientation", 0);

        if (orientation == 1) { // Horizontal
            drawHorizontalTimetable(timetableList, groupedByTime, timeslots);
        } else { // Classic
            Collections.sort(timeslots, Comparator.comparingInt(this::extractStartTime));
            drawClassicTimetable(timetableList, groupedByTime, timeslots);
        }
    }

    private int extractStartTime(String time) {
        if (time == null) return 0;
        Pattern p = Pattern.compile("^(\\d+)");
        Matcher m = p.matcher(time);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private void drawClassicTimetable(ArrayList<CourseModel> timetableList, Map<String, Map<String, CourseModel>> groupedByTime, List<String> sortedTimeslots) {
        TableRow headerRow = new TableRow(getContext());
        headerRow.addView(createHeaderTextView("TIME"));
        for (String day : daysOrder) {
            headerRow.addView(createHeaderTextView(day));
        }
        tableLayout.addView(headerRow);

        Map<String, String> subjectLegendMap = new LinkedHashMap<>();
        Map<String, String> teacherLegendMap = new LinkedHashMap<>();
        Map<String, Integer> subjectColorMap = getSubjectColorMap(timetableList);

        for (String timeslot : sortedTimeslots) {
            TableRow row = new TableRow(getContext());
            row.addView(createTimeCellTextView(timeslot));

            Map<String, CourseModel> coursesForTime = groupedByTime.get(timeslot);

            for (String day : daysOrder) {
                CourseModel course = coursesForTime != null ? coursesForTime.get(day) : null;
                addCellToRow(row, course, subjectColorMap, subjectLegendMap, teacherLegendMap);
            }
            tableLayout.addView(row);
        }

        updateLegend(subjectLegendMap, teacherLegendMap, subjectColorMap);
    }

    private void drawHorizontalTimetable(ArrayList<CourseModel> timetableList, Map<String, Map<String, CourseModel>> groupedByTime, List<String> sortedTimeslots) {
        TableRow headerRow = new TableRow(getContext());
        headerRow.addView(createHeaderTextView("DAY"));
        for (String timeslot : sortedTimeslots) {
            headerRow.addView(createHeaderTextView(timeslot));
        }
        tableLayout.addView(headerRow);

        Map<String, String> subjectLegendMap = new LinkedHashMap<>();
        Map<String, String> teacherLegendMap = new LinkedHashMap<>();
        Map<String, Integer> subjectColorMap = getSubjectColorMap(timetableList);

        for (String day : daysOrder) {
            TableRow row = new TableRow(getContext());
            row.addView(createTimeCellTextView(day));

            for (String timeslot : sortedTimeslots) {
                CourseModel course = groupedByTime.get(timeslot) != null ? groupedByTime.get(timeslot).get(day) : null;
                addCellToRow(row, course, subjectColorMap, subjectLegendMap, teacherLegendMap);
            }
            tableLayout.addView(row);
        }

        updateLegend(subjectLegendMap, teacherLegendMap, subjectColorMap);
    }

    private void addCellToRow(TableRow row, CourseModel course, Map<String, Integer> subjectColorMap, Map<String, String> subjectLegendMap, Map<String, String> teacherLegendMap) {
        TextView classCell;
        if (course != null) {
            String statusPrefix = getStatusPrefix(course.getStatus());
            if ("Recess".equalsIgnoreCase(course.getSubjectName()) || "Free".equalsIgnoreCase(course.getSubjectName())) {
                classCell = createStyledTextView(statusPrefix + course.getSubjectName(), 0, ContextCompat.getColor(getContext(), R.color.recess_neutral_bg));
            } else {
                String subjectAbbr = getAbbreviation(course.getSubjectName(), 10);
                String teacherAbbr = getAbbreviation(course.getTeacherName(), 12);

                if (!subjectAbbr.equals(course.getSubjectName())) subjectLegendMap.put(subjectAbbr, course.getSubjectName());
                if (!teacherAbbr.equals(course.getTeacherName())) teacherLegendMap.put(teacherAbbr, course.getTeacherName());

                SpannableString cellText = new SpannableString(statusPrefix + subjectAbbr + "\n" + teacherAbbr);
                cellText.setSpan(new StyleSpan(Typeface.BOLD), statusPrefix.length(), statusPrefix.length() + subjectAbbr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                Integer cellColor = subjectColorMap.get(course.getSubjectName());
                if (cellColor == null) cellColor = Color.WHITE;
                classCell = createStyledTextView(cellText, 0, cellColor);
                classCell.setOnClickListener(v -> showActionDialog(course));
            }
        } else {
            classCell = createStyledTextView("", 0, Color.WHITE);
        }
        row.addView(classCell);
    }

    private void updateLegend(Map<String, String> subjectLegendMap, Map<String, String> teacherLegendMap, Map<String, Integer> subjectColorMap) {
        if (subjectLegendMap.isEmpty() && teacherLegendMap.isEmpty()) {
            legendCard.setVisibility(View.GONE);
        } else {
            legendCard.setVisibility(View.VISIBLE);
            legendGrid.removeAllViews();

            for (Map.Entry<String, String> entry : subjectLegendMap.entrySet()) {
                Integer color = subjectColorMap.get(entry.getValue());
                if (color == null) color = Color.LTGRAY;
                addLegendEntry(legendGrid, color, entry.getKey() + ": " + entry.getValue());
            }
            for (Map.Entry<String, String> entry : teacherLegendMap.entrySet()) {
                addLegendEntry(legendGrid, Color.GRAY, entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    private void addLegendEntry(GridLayout grid, int color, String text) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View legendItemView = inflater.inflate(R.layout.item_legend, grid, false);

        ImageView colorDot = legendItemView.findViewById(R.id.legend_color_dot);
        TextView legendText = legendItemView.findViewById(R.id.legend_text);

        GradientDrawable dotDrawable = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.legend_color_dot).mutate();
        dotDrawable.setColor(color);
        colorDot.setImageDrawable(dotDrawable);

        legendText.setText(text);
        grid.addView(legendItemView);
    }

    private String getStatusPrefix(String status) {
        if (status == null) return "";
        switch (status) {
            case "Attended": return "✅ ";
            case "Skipped": return "❌ ";
            default: return "";
        }
    }

    private void showActionDialog(final CourseModel course) {
        if (getContext() == null) return;
        final String[] options = {"Mark as Attended", "Mark as Skipped", "Edit Details", "Clear Status"};

        new AlertDialog.Builder(getContext())
                .setTitle("Choose Action")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: 
                            dbHelper.updateLectureStatus(course.getCourseid(), "Attended");
                            loadTimetable();
                            break;
                        case 1: 
                            dbHelper.updateLectureStatus(course.getCourseid(), "Skipped");
                            loadTimetable();
                            break;
                        case 2: 
                            showEditDialog(course);
                            break;
                        case 3: 
                            dbHelper.updateLectureStatus(course.getCourseid(), null);
                            loadTimetable();
                            break;
                    }
                })
                .show();
    }

    private void showEditDialog(CourseModel course) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Entry");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_timetable, null);
        final EditText inputSubject = dialogView.findViewById(R.id.etEditSubject);
        final EditText inputTeacher = dialogView.findViewById(R.id.etEditTeacher);
        builder.setView(dialogView);
        inputSubject.setText(course.getSubjectName());
        inputTeacher.setText(course.getTeacherName());
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newSubject = inputSubject.getText().toString().trim();
            String newTeacher = inputTeacher.getText().toString().trim();
            if (!newSubject.isEmpty() && !newTeacher.isEmpty()) {
                course.setSubjectName(newSubject);
                course.setTeacherName(newTeacher);
                dbHelper.updateTimetableEntry(course);
                loadTimetable();
                Toast.makeText(getContext(), "Entry Updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private int[] getColorArray() {
        SharedPreferences preferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
        int theme = preferences.getInt("timetable_theme", 9); // Default to new muted pastels
        int arrayId;
        switch (theme) {
            case 1: arrayId = R.array.vibrant_colors; break;
            case 2: arrayId = R.array.pastel_colors; break;
            case 3: arrayId = R.array.corporate_blue_colors; break;
            case 4: arrayId = R.array.autumn_harvest_colors; break;
            case 5: arrayId = R.array.sunrise_colors; break;
            case 6: arrayId = R.array.emerald_colors; break;
            case 7: arrayId = R.array.lavender_bliss_colors; break;
            case 8: arrayId = R.array.minty_fresh_colors; break;
            case 9: arrayId = R.array.muted_pastels; break;
            default: arrayId = R.array.vibrant_colors;
        }
        TypedArray ta = getResources().obtainTypedArray(arrayId);
        int[] colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();
        return colors;
    }

    private Map<String, Integer> getSubjectColorMap(ArrayList<CourseModel> timetableList) {
        Map<String, Integer> subjectColorMap = new HashMap<>();
        int colorIndex = 0;
        int[] colorArray = getColorArray();
        if (colorArray.length == 0) return subjectColorMap;

        for (CourseModel course : timetableList) {
            if (!"Recess".equalsIgnoreCase(course.getSubjectName()) && !"Free".equalsIgnoreCase(course.getSubjectName()) && !subjectColorMap.containsKey(course.getSubjectName())) {
                subjectColorMap.put(course.getSubjectName(), colorArray[colorIndex % colorArray.length]);
                colorIndex++;
            }
        }
        return subjectColorMap;
    }

    private String getAbbreviation(String name, int threshold) {
        if (name == null || name.isEmpty()) return "";
        if (name.length() <= threshold && !name.contains(" ")) {
            return name;
        }
        StringBuilder abbreviation = new StringBuilder();
        for (String word : name.split("\\s+")) {
            if (!word.isEmpty()) {
                abbreviation.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return abbreviation.toString();
    }

    private TextView createStyledTextView(CharSequence text, int styleResId, int backgroundColor) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        if (styleResId != 0) {
            textView.setTextAppearance(styleResId);
        }

        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(backgroundColor);
        background.setStroke(dpToPx(1), ContextCompat.getColor(getContext(), R.color.grid_divider));
        textView.setBackground(background);

        int padding = dpToPx(8);
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = createStyledTextView(text, 0, ContextCompat.getColor(getContext(), R.color.professional_header_bg));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.professional_header_text));
        return textView;
    }

    private TextView createTimeCellTextView(String text) {
        return createStyledTextView(text, 0, ContextCompat.getColor(getContext(), R.color.time_column_bg));
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void generateAndSharePdf() {
        if (getContext() == null) return;
        ArrayList<CourseModel> timetableList = dbHelper.getAllTimetableEntries();
        if (timetableList.isEmpty()) {
            Toast.makeText(getContext(), "Timetable is empty, nothing to share.", Toast.LENGTH_SHORT).show();
            return;
        }

        String courseName = timetableList.get(0).getCourseName();
        File pdfFile = createPdfFile(courseName);
        if (pdfFile == null) {
            Toast.makeText(getContext(), "Failed to create PDF file.", Toast.LENGTH_SHORT).show();
            return;
        }

        try (PdfWriter writer = new PdfWriter(pdfFile); PdfDocument pdf = new PdfDocument(writer); Document document = new Document(pdf)) {
            document.add(new Paragraph(courseName).setBold().setFontSize(20));

            SharedPreferences preferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
            int orientation = preferences.getInt("timetable_orientation", 0);
            Map<String, Integer> subjectColorMap = getSubjectColorMap(timetableList);

            Map<String, Map<String, CourseModel>> groupedByTime = new LinkedHashMap<>();
            for (CourseModel course : timetableList) {
                groupedByTime.computeIfAbsent(course.getTimeslot(), k -> new LinkedHashMap<>());
                groupedByTime.get(course.getTimeslot()).put(course.getDay().toUpperCase(), course);
            }

            List<String> sortedTimeslots = new ArrayList<>(groupedByTime.keySet());

            if (orientation == 1) { // Horizontal
                drawHorizontalPdfForPdf(document, subjectColorMap, groupedByTime, sortedTimeslots);
            } else { // Classic
                Collections.sort(sortedTimeslots, Comparator.comparingInt(this::extractStartTime));
                drawClassicPdfForPdf(document, subjectColorMap, groupedByTime, sortedTimeslots);
            }

            TimetableHistory history = new TimetableHistory(0, courseName, pdfFile.getAbsolutePath(), System.currentTimeMillis());
            dbHelper.addTimetableHistory(history);

            sharePdf(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawClassicPdfForPdf(Document document, Map<String, Integer> subjectColorMap, Map<String, Map<String, CourseModel>> groupedByTime, List<String> sortedTimeslots) {
        float[] columnWidths = {2f, 3f, 3f, 3f, 3f, 3f, 3f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));

        DeviceRgb headerColor = new DeviceRgb(232, 234, 246);
        table.addHeaderCell(new Cell().add(new Paragraph("TIME")).setBackgroundColor(headerColor));
        for (String day : daysOrder) {
            table.addHeaderCell(new Cell().add(new Paragraph(day)).setBackgroundColor(headerColor));
        }

        for (String timeslot : sortedTimeslots) {
            table.addCell(new Cell().add(new Paragraph(timeslot)).setBackgroundColor(new DeviceRgb(245, 245, 245)));
            Map<String, CourseModel> coursesForTime = groupedByTime.get(timeslot);
            for (String day : daysOrder) {
                CourseModel course = coursesForTime != null ? coursesForTime.get(day) : null;
                Cell classCell;
                if (course != null) {
                     if ("Recess".equalsIgnoreCase(course.getSubjectName())) {
                        classCell = new Cell().add(new Paragraph("Recess"));
                        classCell.setBackgroundColor(new DeviceRgb(245, 245, 245));
                    } else {
                        String statusPrefix = getStatusPrefix(course.getStatus());
                        classCell = new Cell().add(new Paragraph(statusPrefix + course.getSubjectName() + "\n" + course.getTeacherName()));
                        Integer androidColor = subjectColorMap.get(course.getSubjectName());
                        if (androidColor == null) androidColor = Color.WHITE;
                        DeviceRgb cellColor = new DeviceRgb(Color.red(androidColor), Color.green(androidColor), Color.blue(androidColor));
                        classCell.setBackgroundColor(cellColor);
                    }
                } else {
                    classCell = new Cell().add(new Paragraph(""));
                }
                table.addCell(classCell);
            }
        }
        document.add(table);
    }
    
    private void drawHorizontalPdfForPdf(Document document, Map<String, Integer> subjectColorMap, Map<String, Map<String, CourseModel>> groupedByTime, List<String> sortedTimeslots) {
        float[] columnWidths = new float[sortedTimeslots.size() + 1];
        Arrays.fill(columnWidths, 3f);
        columnWidths[0] = 2f;
        Table table = new Table(UnitValue.createPercentArray(columnWidths));

        DeviceRgb headerColor = new DeviceRgb(232, 234, 246);
        table.addHeaderCell(new Cell().add(new Paragraph("DAY")).setBackgroundColor(headerColor));
        for (String timeslot : sortedTimeslots) {
            table.addHeaderCell(new Cell().add(new Paragraph(timeslot)).setBackgroundColor(headerColor));
        }

        for (String day : daysOrder) {
            table.addCell(new Cell().add(new Paragraph(day)).setBackgroundColor(new DeviceRgb(245, 245, 245)));
            for (String timeslot : sortedTimeslots) {
                CourseModel course = groupedByTime.get(timeslot) != null ? groupedByTime.get(timeslot).get(day) : null;
                Cell classCell;
                if (course != null) {
                     if ("Recess".equalsIgnoreCase(course.getSubjectName())) {
                        classCell = new Cell().add(new Paragraph("Recess"));
                        classCell.setBackgroundColor(new DeviceRgb(245, 245, 245));
                    } else {
                        String statusPrefix = getStatusPrefix(course.getStatus());
                        classCell = new Cell().add(new Paragraph(statusPrefix + course.getSubjectName() + "\n" + course.getTeacherName()));
                        Integer androidColor = subjectColorMap.get(course.getSubjectName());
                        if (androidColor == null) androidColor = Color.WHITE;
                        DeviceRgb cellColor = new DeviceRgb(Color.red(androidColor), Color.green(androidColor), Color.blue(androidColor));
                        classCell.setBackgroundColor(cellColor);
                    }
                } else {
                    classCell = new Cell().add(new Paragraph(""));
                }
                table.addCell(classCell);
            }
        }
        document.add(table);
    }

    private File createPdfFile(String courseName) {
        String fileName = courseName.replaceAll("[^a-zA-Z0-9]", "") + "_" + System.currentTimeMillis() + ".pdf";
        File pdfPath = getContext().getExternalFilesDir("pdfs");
        if (pdfPath == null) return null;
        if (!pdfPath.exists()) pdfPath.mkdirs();
        return new File(pdfPath, fileName);
    }

    private void sharePdf(File file) {
        if (getContext() == null) return;
        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(shareIntent, "Share Timetable PDF"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No app found to share PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
