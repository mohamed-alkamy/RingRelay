package com.example.RingRelayv3;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
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
    private ArrayList<AlarmEntity> alarmList;

    public AlarmAdapter(Context context, ArrayList<AlarmEntity> alarms) {
        super(context, 0, alarms);
        alarmDatabase = AlarmDatabase.getInstance(context);
        this.alarmList = alarms;
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
        Button ringtoneButton = convertView.findViewById(R.id.ringtoneButton);

        if (alarm != null) {
            alarmTime.setText(alarm.getTime());
            alarmSwitch.setOnCheckedChangeListener(null);
            alarmSwitch.setChecked(alarm.isEnabled());

            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alarm.setEnabled(isChecked);
                updateAlarmInDatabase(alarm);
                Log.d("AlarmAdapter", "Made Alarm activation " + alarm.isEnabled());
            });

            alarmDelete.setOnClickListener(v -> deleteAlarm(position, alarm));

            ringtoneButton.setOnClickListener(v -> {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, 
                    Uri.parse(alarm.getRingtoneUri()));
                ((Activity) getContext()).startActivityForResult(intent, position);
            });
        }

        return convertView;
    }

    public void updateAlarmRingtone(int position, Uri ringtoneUri) {
        AlarmEntity alarm = getItem(position);
        if (alarm != null && ringtoneUri != null) {
            alarm.setRingtoneUri(ringtoneUri.toString());
            updateAlarmInDatabase(alarm);
        }
    }

    private void deleteAlarm(int position, AlarmEntity alarm) {
        AsyncTask.execute(() -> {
            alarmDatabase.alarmDao().delete(alarm);
            alarmList.remove(position);
            ((MainActivity) getContext()).runOnUiThread(this::notifyDataSetChanged);
        });
    }

    private void updateAlarmInDatabase(AlarmEntity alarm) {
        AsyncTask.execute(() -> {
            alarmDatabase.alarmDao().update(alarm);
        });
    }
}
