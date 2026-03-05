package com.example.timetableapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class GenerateTimetableFragment extends Fragment {

    private String teacherUsername;

    public GenerateTimetableFragment(String username) {
        this.teacherUsername = username;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate_timetable, container, false);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText("Generate Timetable Fragment for " + teacherUsername);
        return view;
    }
}
