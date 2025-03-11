package com.example.ringrelaygui;

public class Relay {
    private int stepGoal;
    private int currentSteps;
    private String alarmTime;
    private int snoozesUsed;
    private boolean isActive;

    public Relay(int stepGoal, String alarmTime) {
        this.stepGoal = stepGoal;
        this.alarmTime = alarmTime;
        this.currentSteps = 0;
        this.snoozesUsed = 0;
        this.isActive = true;
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

    public void completeRelay() {
        isActive = false;
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
}
