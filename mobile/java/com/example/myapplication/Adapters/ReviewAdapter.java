package com.example.myapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.HttpModels.TeacherReview;
import com.example.myapplication.R;

import java.util.List;

public class ReviewAdapter extends ArrayAdapter<TeacherReview> {

    public ReviewAdapter(@NonNull Context context, @NonNull List<TeacherReview> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate layout if needed
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        }

        TeacherReview teacherReview = getItem(position);

        // Find views in the layout
        TextView rating = convertView.findViewById(R.id.rating);
        TextView comment = convertView.findViewById(R.id.comment);
        TextView createdAt = convertView.findViewById(R.id.createdAt);

        // Populate the views with the review data
        if (teacherReview != null) {
            rating.setText("Rating: " + teacherReview.getRating() + "/5");
            comment.setText("Comment: " + teacherReview.getComment());
            createdAt.setText("Created at: " + teacherReview.getCreatedAt());
        }

        return convertView;
    }
}
