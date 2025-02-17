package com.example.ringrelaygui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class AlarmAdapter extends ArrayAdapter<AlarmItem> {
    private Context context;
    private ArrayList<AlarmItem> alarms;

    public AlarmAdapter(Context context, ArrayList<AlarmItem> alarms) {
        super(context, R.layout.alarm_list_item, alarms);
        this.context = context;
        this.alarms = alarms;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_list_item, parent, false);
        }

        // Get current alarm item
        AlarmItem alarm = getItem(position);

        // Find UI elements
        TextView alarmTime = convertView.findViewById(R.id.alarmTime);
        Switch alarmSwitch = convertView.findViewById(R.id.alarmSwitch);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        // Set values
        alarmTime.setText(alarm.getTime());
        alarmSwitch.setChecked(alarm.isEnabled());

        // Toggle switch functionality
        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> alarm.setEnabled(isChecked));

        // Delete button functionality
        deleteButton.setOnClickListener(v -> {
            alarms.remove(position);
            notifyDataSetChanged();
        });

        return convertView;
    }
}

