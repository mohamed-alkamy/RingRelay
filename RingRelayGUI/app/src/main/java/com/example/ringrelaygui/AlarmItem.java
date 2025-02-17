package com.example.ringrelaygui;

public class AlarmItem {
    private String time;
    private boolean isEnabled;

    public AlarmItem(String time, boolean isEnabled) {
        this.time = time;
        this.isEnabled = isEnabled;
    }

    public String getTime() {
        return time;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}

