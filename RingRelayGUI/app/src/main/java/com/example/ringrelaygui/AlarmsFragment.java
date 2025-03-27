package com.example.ringrelaygui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class AlarmsFragment extends Fragment {
    private Button setAlarmButton;
    private ListView savedAlarmsList;
    private ArrayList<AlarmEntity> savedAlarms = new ArrayList<>();
    private AlarmAdapter adapter;
    private AlarmDatabase alarmDatabase;
    private TimePicker timePicker;
    private EditText stepGoalInput;
    private static final int RINGTONE_REQUEST_CODE = 100;
    private int selectedAlarmPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        timePicker = view.findViewById(R.id.timePicker);
        setAlarmButton = view.findViewById(R.id.setAlarmButton);
        savedAlarmsList = view.findViewById(R.id.savedAlarmsList);
        stepGoalInput = view.findViewById(R.id.stepGoalInput);

        alarmDatabase = AlarmDatabase.getInstance(requireContext());
        adapter = new AlarmAdapter(requireContext(), savedAlarms);
        savedAlarmsList.setAdapter(adapter);
        loadSavedAlarms();

        setAlarmButton.setOnClickListener(v -> {
            String alarmTime = getSelectedTime();
            AlarmEntity newAlarm = new AlarmEntity(alarmTime, true);
            
            try {
                int stepGoal = Integer.parseInt(stepGoalInput.getText().toString());
                newAlarm.setStepGoal(stepGoal);
            } catch (NumberFormatException e) {
                // Use default value
            }
            
            saveAlarm(newAlarm);
            scheduleSystemAlarm(requireContext(), newAlarm);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtoneUri != null && selectedAlarmPosition != -1) {
                adapter.updateAlarmRingtone(selectedAlarmPosition, ringtoneUri);
            }
        }
    }

    private String getSelectedTime() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String amPm = (hour >= 12) ? "PM" : "AM";

        if (hour > 12) hour -= 12;
        else if (hour == 0) hour = 12;

        return hour + ":" + String.format("%02d", minute) + " " + amPm;
    }

    private void saveAlarm(AlarmEntity alarm) {
        AsyncTask.execute(() -> {
            alarmDatabase.alarmDao().insert(alarm);
            requireActivity().runOnUiThread(() -> {
                savedAlarms.add(alarm);
                adapter.notifyDataSetChanged();
                loadSavedAlarms();
                Toast.makeText(getActivity(), "Alarm Saved: " + alarm.getTime(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadSavedAlarms() {
        AsyncTask.execute(() -> {
            List<AlarmEntity> alarms = alarmDatabase.alarmDao().getAllAlarms();
            Collections.sort(alarms, (a, b) -> convertTo24Hour(a.getTime()) - convertTo24Hour(b.getTime()));
            requireActivity().runOnUiThread(() -> {
                savedAlarms.clear();
                savedAlarms.addAll(alarms);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private int convertTo24Hour(String time) {
        String[] parts = time.split(" ");
        String[] hm = parts[0].split(":");
        int hour = Integer.parseInt(hm[0]);
        int minute = Integer.parseInt(hm[1]);
        boolean isPM = parts[1].equalsIgnoreCase("PM");

        if (isPM && hour != 12) hour += 12;
        else if (!isPM && hour == 12) hour = 0;

        return (hour * 60) + minute;
    }

    private void scheduleSystemAlarm(Context context, AlarmEntity alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return;
            }
        }

        Calendar calendar = Calendar.getInstance();
        String[] parts = alarm.getTime().split(" ");
        String[] timeParts = parts[0].split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        boolean isPM = parts[1].equalsIgnoreCase("PM");

        if (isPM && hour != 12) hour += 12;
        else if (!isPM && hour == 12) hour = 0;

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmTime", alarm.getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (SecurityException e) {
            Log.e("AlarmManager", "SecurityException", e);
        }
    }
}