package com.example.ringrelaygui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        // Initialize UI elements
        timePicker = view.findViewById(R.id.timePicker);
        setAlarmButton = view.findViewById(R.id.setAlarmButton);
        savedAlarmsList = view.findViewById(R.id.savedAlarmsList);

        // Initialize Room Database
        alarmDatabase = AlarmDatabase.getInstance(requireContext());

        adapter = new AlarmAdapter(requireContext(), savedAlarms);
        savedAlarmsList.setAdapter(adapter);

        // Load saved alarms from the database
        loadSavedAlarms();

        // Set Alarm Button Click
        setAlarmButton.setOnClickListener(v -> {
            String alarmTime = getSelectedTime();
            AlarmEntity newAlarm = new AlarmEntity(alarmTime, true);
            saveAlarm(newAlarm);
            scheduleSystemAlarm(requireContext(),newAlarm);
        });

        return view;
    }
    private String getSelectedTime() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Determine AM/PM
        String amPm = (hour >= 12) ? "PM" : "AM";

        // Convert the hour to a 12-hour format
        if (hour > 12) {
            hour -= 12;
        } else if (hour == 0) {
            hour = 12; // Handle midnight case
        }

        String time = hour + ":" + String.format("%02d", minute) + " " + amPm;

        // Log the time to Logcat
        Log.d("TimePicker", "Selected time: " + time);
        int longTime = convertTo24Hour(time);
        Log.d("TimePicker", "24 hour variant: " + longTime);

        return time;
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
    public void removeAlarmFromList(int position) {
        requireActivity().runOnUiThread(() -> {
            savedAlarms.remove(position);
            adapter.notifyDataSetChanged();
        });
    }
    private void loadSavedAlarms() {
        AsyncTask.execute(() -> {
            List<AlarmEntity> alarms = alarmDatabase.alarmDao().getAllAlarms();
            for (AlarmEntity alarm : alarms) {
                Log.d("AlarmDebug", "Loaded alarm: " + alarm.getTime());
            }

            // Sort manually
            Collections.sort(alarms, (a, b) -> convertTo24Hour(a.getTime()) - convertTo24Hour(b.getTime()));

            requireActivity().runOnUiThread(() -> {
                savedAlarms.clear();
                savedAlarms.addAll(alarms);
                adapter.notifyDataSetChanged();
            });
        });
    }
    private int convertTo24Hour(String time) {
        String[] parts = time.split(" "); // Split into "hh:mm" and "AM/PM"
        String[] hm = parts[0].split(":"); // Split "hh:mm" into "hh" and "mm"

        int hour = Integer.parseInt(hm[0]);
        int minute = Integer.parseInt(hm[1]);
        boolean isPM = parts[1].equalsIgnoreCase("PM");

        // Convert to 24-hour format
        if (isPM && hour != 12) {
            hour += 12; // Convert PM hours (except 12 PM) to 24-hour format
        } else if (!isPM && hour == 12) {
            hour = 0; // Midnight case: "12 AM" should become "00"
        }

        return (hour * 60) + minute; // Convert to total minutes since 00:00
    }
    public void toggleAlarm(AlarmEntity alarm, boolean isEnabled) {
        if (isEnabled) {
            scheduleSystemAlarm(requireContext(), alarm);
        } else {
            alarm.isEnabled = false;
        }

        // Update database entry
        AsyncTask.execute(() -> {
            alarm.setEnabled(isEnabled);
            alarmDatabase.alarmDao().update(alarm);
        });
    }
    private void scheduleSystemAlarm(Context context, AlarmEntity alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e("AlarmManager", "AlarmManager is null");
            return;
        }

        // Check if the app can schedule exact alarms
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Prompt the user to grant permission
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return; // Exit early to avoid the security exception
            }
        }

        // Calculate the trigger time
        Calendar calendar = Calendar.getInstance();
        String[] parts = alarm.getTime().split(" ");
        String[] timeParts = parts[0].split(":");

        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        boolean isPM = parts[1].equalsIgnoreCase("PM");

        if (isPM && hour != 12) {
            hour += 12;
        } else if (!isPM && hour == 12) {
            hour = 0;
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerTime = calendar.getTimeInMillis();

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmTime", alarm.getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            // Schedule the alarm
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d("AlarmManager", "Alarm scheduled at: " + triggerTime);
        } catch (SecurityException e) {
            Log.e("AlarmManager", "SecurityException: Unable to schedule exact alarm", e);
        }
    }

}
