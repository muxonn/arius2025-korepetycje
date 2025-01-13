package com.example.myapplication.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleAdapter.DayViewHolder> {
    private final Context context;
    private final List<String> days;
    private final List<List<String>> timeSlots;
    private String selectedDay = null;
    private String selectedTimeSlot = null;

    public DayScheduleAdapter(Context context, List<String> days, List<List<String>> timeSlots) {
        this.context = context;
        this.days = days;
        this.timeSlots = timeSlots;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.calendar_item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        String day = days.get(position);
        List<String> slots = timeSlots.get(position);
        holder.dayTextView.setText(day);
        holder.timeSlotContainer.removeAllViews();

        // Loop through each time slot for the day
        for (String slot : slots) {
            TextView timeSlotView = new TextView(context);
            timeSlotView.setText(slot);
            timeSlotView.setPadding(8, 8, 8, 8);
            timeSlotView.setBackgroundColor(day.equals(selectedDay) && slot.equals(selectedTimeSlot) ? Color.LTGRAY : Color.TRANSPARENT);
            timeSlotView.setOnClickListener(v -> {
                // Set the selected day and time slot
                selectedDay = day;
                selectedTimeSlot = slot;
                notifyDataSetChanged();
            });
            holder.timeSlotContainer.addView(timeSlotView);
        }
    }

    // Method to get the selected slot in a specific format
    public String getSelectedSlot() {
        return selectedTimeSlot != null ? selectedDay.split(" ")[0] + " " + selectedTimeSlot.split(" - ")[0] : null;
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        ViewGroup timeSlotContainer;

        DayViewHolder(View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            timeSlotContainer = itemView.findViewById(R.id.timeSlotContainer);
        }
    }
}
