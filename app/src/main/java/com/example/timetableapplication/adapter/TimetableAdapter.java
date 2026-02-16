package com.example.timetableapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetableapplication.R;
import com.example.timetableapplication.ModelClass.CourseModel;

import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<Object> items;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TIME = 1;
    private static final int TYPE_CELL = 2;
    private static final int TYPE_EMPTY = 3;

    public TimetableAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String && ((String) item).equals("HEADER")) {
            return TYPE_HEADER;
        } else if (item instanceof String) {
            return TYPE_TIME;
        } else if (item instanceof CourseModel) {
            return TYPE_CELL;
        } else {
            return TYPE_EMPTY;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_HEADER:
                view = LayoutInflater.from(context).inflate(R.layout.item_timetable_header, parent, false);
                return new HeaderViewHolder(view);
            case TYPE_TIME:
                view = LayoutInflater.from(context).inflate(R.layout.item_timetable_time, parent, false);
                return new TimeViewHolder(view);
            case TYPE_CELL:
                view = LayoutInflater.from(context).inflate(R.layout.item_timetable_cell, parent, false);
                return new CellViewHolder(view);
            case TYPE_EMPTY:
            default:
                view = LayoutInflater.from(context).inflate(R.layout.item_timetable_empty, parent, false);
                return new EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_HEADER:
                break;
            case TYPE_TIME:
                ((TimeViewHolder) holder).time.setText((String) items.get(position));
                break;
            case TYPE_CELL:
                CourseModel course = (CourseModel) items.get(position);
                ((CellViewHolder) holder).subject.setText(course.getSubjectName());
                break;
            case TYPE_EMPTY:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            header = (TextView) itemView;
        }
    }

    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            time = (TextView) itemView;
        }
    }

    public static class CellViewHolder extends RecyclerView.ViewHolder {
        TextView subject;
        public CellViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = (TextView) itemView;
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
