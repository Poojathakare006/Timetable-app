package com.example.timetableapplication.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.AnimationTypes;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.timetableapplication.R;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    ImageSlider imageSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imageSlider = view.findViewById(R.id.imageSlider);

        ArrayList<SlideModel> SlideModelArrayList = new ArrayList<>();

        SlideModelArrayList.add(new SlideModel(R.drawable.timetable1, "Timetable1",ScaleTypes.CENTER_CROP));
        SlideModelArrayList.add(new SlideModel(R.drawable.timetable2,"Timetable2", ScaleTypes.CENTER_CROP));
        SlideModelArrayList.add(new SlideModel(R.drawable.timetable3,"Timetable3",ScaleTypes.CENTER_CROP));
        SlideModelArrayList.add(new SlideModel(R.drawable.timetable4,"Timetable4",ScaleTypes.CENTER_CROP));
        SlideModelArrayList.add(new SlideModel(R.drawable.timetable5,"Timetable5",ScaleTypes.CENTER_CROP));
        SlideModelArrayList.add(new SlideModel(R.drawable.timetable6,"Timetable6",ScaleTypes.CENTER_CROP));

        imageSlider.setImageList(SlideModelArrayList);
        imageSlider.setSlideAnimation(AnimationTypes.CUBE_IN);
        imageSlider.startSliding(2000);

        return view;


    }
}