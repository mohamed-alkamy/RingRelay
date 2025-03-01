package com.example.ringrelaygui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class AlarmAdapter extends ArrayAdapter<AlarmEntity> {
    public AlarmAdapter(Context context, ArrayList<AlarmEntity> alarms) {
        super(context, 0, alarms);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_item, parent, false);
        }

        AlarmEntity alarm = getItem(position);
        TextView alarmTime = convertView.findViewById(R.id.alarmTimeText);
        Switch alarmSwitch = convertView.findViewById(R.id.alarmSwitch);

        if (alarm != null) {
            alarmTime.setText(alarm.getTime());
            alarmSwitch.setChecked(alarm.isEnabled());
        }

        return convertView;
    }
}


