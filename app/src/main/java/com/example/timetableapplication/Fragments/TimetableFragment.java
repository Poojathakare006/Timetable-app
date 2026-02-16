package com.example.timetableapplication.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

    FloatingActionButton fab_add_timetable;
    DBHelper dbHelper;
    TableLayout tableLayout;
    Button btnShare;
    TextView tvCourseNameHeader, tvLegend;
    CardView legendCard;
    List<String> daysOrder = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        dbHelper = new DBHelper(getContext());
        tableLayout = view.findViewById(R.id.tableTimetable);
        fab_add_timetable = view.findViewById(R.id.fab_add_timetable);
        btnShare = view.findViewById(R.id.btnShareTimetable);
        tvCourseNameHeader = view.findViewById(R.id.tvCourseNameHeader);
        tvLegend = view.findViewById(R.id.tvLegend);
        legendCard = view.findViewById(R.id.legend_card);

        fab_add_timetable.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTimetableActivity.class);
            startActivity(intent);
        });

        btnShare.setOnClickListener(v -> generateAndSharePdf());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
        loadTimetable();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }

    private void loadTimetable() {
        if (getContext() == null) return;

        ArrayList<CourseModel> timetableList = dbHelper.getAllTimetableEntries();
        while (tableLayout.getChildCount() > 1) {
            tableLayout.removeView(tableLayout.getChildAt(1));
        }

        if (timetableList.isEmpty()) {
            tvCourseNameHeader.setText("No Timetable Generated");
            legendCard.setVisibility(View.GONE);
            return;
        }

        tvCourseNameHeader.setText(timetableList.get(0).getCourseName());

        Map<String, Map<String, CourseModel>> groupedByTime = new LinkedHashMap<>();
        for (CourseModel course : timetableList) {
            groupedByTime.computeIfAbsent(course.getTimeslot(), k -> new LinkedHashMap<>());
            groupedByTime.get(course.getTimeslot()).put(course.getDay().toUpperCase(), course);
        }

        List<String> sortedTimeslots = new ArrayList<>(groupedByTime.keySet());

        Map<String, String> subjectLegendMap = new LinkedHashMap<>();
        Map<String, String> teacherLegendMap = new LinkedHashMap<>();
        int[] colorArray = getColorArray();
        Map<String, Integer> subjectColorMap = getSubjectColorMap(timetableList, colorArray);

        for (String timeslot : sortedTimeslots) {
            TableRow row = new TableRow(getContext());
            TextView timeCell = createStyledTextView(timeslot, R.style.timeCell, ContextCompat.getColor(getContext(), R.color.lightred));
            row.addView(timeCell);

            Map<String, CourseModel> coursesForTime = groupedByTime.get(timeslot);

            for (String day : daysOrder) {
                CourseModel course = coursesForTime.get(day);
                TextView classCell;
                if (course != null) {
                     if ("Recess".equalsIgnoreCase(course.getSubjectName())) {
                        classCell = createStyledTextView("Recess", R.style.classCell, Color.parseColor("#E0E0E0"));
                    } else {
                        String subjectAbbr = getAbbreviation(course.getSubjectName(), 10);
                        String teacherAbbr = getAbbreviation(course.getTeacherName(), 12);

                        if (!subjectAbbr.equals(course.getSubjectName())) subjectLegendMap.put(subjectAbbr, course.getSubjectName());
                        if (!teacherAbbr.equals(course.getTeacherName())) teacherLegendMap.put(teacherAbbr, course.getTeacherName());

                        String cellText = subjectAbbr + "\n" + teacherAbbr;
                        int cellColor = subjectColorMap.get(course.getSubjectName());
                        classCell = createStyledTextView(cellText, R.style.classCell, cellColor);
                        classCell.setOnClickListener(v -> showEditDialog(course));
                    }
                } else {
                    classCell = createStyledTextView("", R.style.classCell, Color.WHITE);
                }
                row.addView(classCell);
            }
            tableLayout.addView(row);
        }

        if (subjectLegendMap.isEmpty() && teacherLegendMap.isEmpty()) {
            legendCard.setVisibility(View.GONE);
        } else {
            legendCard.setVisibility(View.VISIBLE);
            SpannableStringBuilder legendBuilder = new SpannableStringBuilder();
            if (!subjectLegendMap.isEmpty()) {
                addLegendSection(legendBuilder, "Subjects: ", subjectLegendMap);
            }
            if (!teacherLegendMap.isEmpty()) {
                if (legendBuilder.length() > 0) legendBuilder.append("\n");
                addLegendSection(legendBuilder, "Teachers: ", teacherLegendMap);
            }
            tvLegend.setText(legendBuilder);
        }
    }

    private int[] getColorArray() {
        SharedPreferences preferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
        int theme = preferences.getInt("timetable_theme", 0);
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
            default: return new int[]{ContextCompat.getColor(getContext(), R.color.lightred)};
        }
        TypedArray ta = getResources().obtainTypedArray(arrayId);
        int[] colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();
        return colors;
    }

    private Map<String, Integer> getSubjectColorMap(ArrayList<CourseModel> timetableList, int[] colorArray) {
        Map<String, Integer> subjectColorMap = new HashMap<>();
        int colorIndex = 0;
        for (CourseModel course : timetableList) {
            if (!"Recess".equalsIgnoreCase(course.getSubjectName()) && !subjectColorMap.containsKey(course.getSubjectName())) {
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

    private void addLegendSection(SpannableStringBuilder builder, String title, Map<String, String> items) {
        int start = builder.length();
        builder.append(title);
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        boolean first = true;
        for (Map.Entry<String, String> entry : items.entrySet()) {
            if (!first) builder.append(";  ");
            builder.append(entry.getKey() + " - " + entry.getValue());
            first = false;
        }
    }

    private TextView createStyledTextView(String text, int styleResId, int backgroundColor) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextAppearance(getContext(), styleResId);

        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(backgroundColor);
        background.setStroke(dpToPx(1), Color.parseColor("#DCDCDC"));
        textView.setBackground(background);

        int padding = dpToPx(12);
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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

        try {
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph(courseName).setBold().setFontSize(20));

            SharedPreferences preferences = getContext().getSharedPreferences("user_details", Context.MODE_PRIVATE);
            int orientation = preferences.getInt("timetable_orientation", 0);
            Map<String, Integer> subjectColorMap = getSubjectColorMap(timetableList, getColorArray());

            Map<String, Map<String, CourseModel>> groupedByTime = new LinkedHashMap<>();
            for (CourseModel course : timetableList) {
                groupedByTime.computeIfAbsent(course.getTimeslot(), k -> new LinkedHashMap<>());
                groupedByTime.get(course.getTimeslot()).put(course.getDay().toUpperCase(), course);
            }

            List<String> sortedTimeslots = new ArrayList<>(groupedByTime.keySet());
            Collections.sort(sortedTimeslots, (ts1, ts2) -> {
                 try {
                    Pattern p = Pattern.compile("^(\\d+)");
                    Matcher m1 = p.matcher(ts1);
                    Matcher m2 = p.matcher(ts2);
                    if (m1.find() && m2.find()) {
                        return Integer.parseInt(m1.group(1)) - Integer.parseInt(m2.group(1));
                    }
                    return ts1.compareTo(ts2);
                } catch (Exception e) {
                    return ts1.compareTo(ts2);
                }
            });

            if (orientation == 1) { // Horizontal
                drawHorizontalPdf(document, subjectColorMap, groupedByTime, sortedTimeslots);
            } else { // Classic
                drawClassicPdf(document, subjectColorMap, groupedByTime, sortedTimeslots);
            }

            document.close();

            TimetableHistory history = new TimetableHistory(0, courseName, pdfFile.getAbsolutePath(), System.currentTimeMillis());
            dbHelper.addTimetableHistory(history);

            sharePdf(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawClassicPdf(Document document, Map<String, Integer> subjectColorMap, Map<String, Map<String, CourseModel>> groupedByTime, List<String> sortedTimeslots) {
        float[] columnWidths = {2f, 3f, 3f, 3f, 3f, 3f, 3f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));

        DeviceRgb headerColor = new DeviceRgb(216, 181, 181);
        table.addHeaderCell(new Cell().add(new Paragraph("TIME")).setBackgroundColor(headerColor));
        for (String day : daysOrder) {
            table.addHeaderCell(new Cell().add(new Paragraph(day)).setBackgroundColor(headerColor));
        }

        for (String timeslot : sortedTimeslots) {
            table.addCell(new Cell().add(new Paragraph(timeslot)).setBackgroundColor(new DeviceRgb(230, 207, 207)));
            Map<String, CourseModel> coursesForTime = groupedByTime.get(timeslot);
            for (String day : daysOrder) {
                CourseModel course = coursesForTime.get(day);
                Cell classCell;
                if (course != null) {
                     if ("Recess".equalsIgnoreCase(course.getSubjectName())) {
                        classCell = new Cell().add(new Paragraph("Recess"));
                        classCell.setBackgroundColor(new DeviceRgb(224, 224, 224));
                    } else {
                        classCell = new Cell().add(new Paragraph(course.getSubjectName() + "\n" + course.getTeacherName()));
                        int androidColor = subjectColorMap.get(course.getSubjectName());
                        DeviceRgb cellColor = new DeviceRgb(Color.red(androidColor), Color.green(androidColor), Color.blue(androidColor));
                        classCell.setBackgroundColor(cellColor);
                    }
                } else {
                    classCell = new Cell().add(new Paragraph(""));
                    classCell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.WHITE);
                }
                table.addCell(classCell);
            }
        }
        document.add(table);
    }
    
    private void drawHorizontalPdf(Document document, Map<String, Integer> subjectColorMap, Map<String, Map<String, CourseModel>> groupedByTime, List<String> sortedTimeslots) {
        float[] columnWidths = new float[sortedTimeslots.size() + 1];
        Arrays.fill(columnWidths, 3f);
        columnWidths[0] = 2f;
        Table table = new Table(UnitValue.createPercentArray(columnWidths));

        DeviceRgb headerColor = new DeviceRgb(216, 181, 181);
        table.addHeaderCell(new Cell().add(new Paragraph("DAY")).setBackgroundColor(headerColor));
        for (String timeslot : sortedTimeslots) {
            table.addHeaderCell(new Cell().add(new Paragraph(timeslot)).setBackgroundColor(headerColor));
        }

        for (String day : daysOrder) {
            table.addCell(new Cell().add(new Paragraph(day)).setBackgroundColor(new DeviceRgb(230, 207, 207)));
            for (String timeslot : sortedTimeslots) {
                CourseModel course = groupedByTime.get(timeslot) != null ? groupedByTime.get(timeslot).get(day) : null;
                Cell classCell;
                if (course != null) {
                     if ("Recess".equalsIgnoreCase(course.getSubjectName())) {
                        classCell = new Cell().add(new Paragraph("Recess"));
                        classCell.setBackgroundColor(new DeviceRgb(224, 224, 224));
                    } else {
                        classCell = new Cell().add(new Paragraph(course.getSubjectName() + "\n" + course.getTeacherName()));
                        int androidColor = subjectColorMap.get(course.getSubjectName());
                        DeviceRgb cellColor = new DeviceRgb(Color.red(androidColor), Color.green(androidColor), Color.blue(androidColor));
                        classCell.setBackgroundColor(cellColor);
                    }
                } else {
                    classCell = new Cell().add(new Paragraph(""));
                    classCell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.WHITE);
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
