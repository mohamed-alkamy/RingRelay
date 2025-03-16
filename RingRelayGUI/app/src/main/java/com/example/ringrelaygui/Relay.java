package com.example.ringrelaygui;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class Relay {
    private int stepGoal;
    private int currentSteps;
    private String alarmTime;
    private int snoozesUsed;
    private boolean isActive;
    private long startTime;
    private long endTime;

    public Relay(int stepGoal, String alarmTime) {
        this.stepGoal = stepGoal;
        this.alarmTime = alarmTime;
        this.currentSteps = 0;
        this.snoozesUsed = 0;
        this.isActive = true;
        this.startTime = System.currentTimeMillis();  // Capture start time

    }
    public void completeRelay() {
        isActive = false;
        this.endTime = System.currentTimeMillis();  // Capture end time
    }

    public void incrementSteps() {
        if (isActive) {
            currentSteps++;
        }
    }

    public boolean isStepGoalMet() {
        return currentSteps >= stepGoal;
    }

    public void snooze() {
        snoozesUsed++;
        currentSteps = 0; // Reset steps on snooze
    }

    public int getStepGoal() {
        return stepGoal;
    }

    public int getCurrentSteps() {
        return currentSteps;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public int getSnoozesUsed() {
        return snoozesUsed;
    }

    public boolean isActive() {
        return isActive;
    }
    public void setCurrentSteps(int currentSteps){
        this.currentSteps  = currentSteps;
    }

    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public String getFormattedStartTime() {
        return formatTime(startTime);
    }

    public String getFormattedEndTime() {
        return formatTime(endTime);
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
