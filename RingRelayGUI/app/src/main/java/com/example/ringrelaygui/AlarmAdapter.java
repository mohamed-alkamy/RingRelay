package com.example.ringrelaygui;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class AlarmAdapter extends ArrayAdapter<AlarmEntity> {
    private AlarmDatabase alarmDatabase;

    public AlarmAdapter(Context context, ArrayList<AlarmEntity> alarms) {
        super(context, 0, alarms);
        alarmDatabase = AlarmDatabase.getInstance(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_item, parent, false);
        }

        AlarmEntity alarm = getItem(position);
        TextView alarmTime = convertView.findViewById(R.id.alarmTimeText);
        Switch alarmSwitch = convertView.findViewById(R.id.alarmSwitch);
        Button alarmDelete = convertView.findViewById(R.id.alarmDelete);

        if (alarm != null) {
            alarmTime.setText(alarm.getTime());
            alarmSwitch.setChecked(alarm.isEnabled());

            // Handle Delete Button Click
            alarmDelete.setOnClickListener(v -> deleteAlarm(position, alarm));
        }

        return convertView;
    }

    private void deleteAlarm(int position, AlarmEntity alarm) {
        AsyncTask.execute(() -> {
            alarmDatabase.alarmDao().delete(alarm);
            ((AlarmsFragment) ((MainActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.fragment_container)).removeAlarmFromList(position);
        });
    }
}



