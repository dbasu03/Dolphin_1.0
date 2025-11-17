package com.dolphin.focus.model;

import java.io.Serializable;

public class SleepSchedule implements Serializable {
    private int sleepHour;
    private int sleepMinute;
    private int wakeHour;
    private int wakeMinute;

    public SleepSchedule() {
        // Default: 11 PM to 7 AM
        this.sleepHour = 23;
        this.sleepMinute = 0;
        this.wakeHour = 7;
        this.wakeMinute = 0;
    }

    public SleepSchedule(int sleepHour, int sleepMinute, int wakeHour, int wakeMinute) {
        this.sleepHour = sleepHour;
        this.sleepMinute = sleepMinute;
        this.wakeHour = wakeHour;
        this.wakeMinute = wakeMinute;
    }

    public int getSleepHour() {
        return sleepHour;
    }

    public void setSleepHour(int sleepHour) {
        this.sleepHour = sleepHour;
    }

    public int getSleepMinute() {
        return sleepMinute;
    }

    public void setSleepMinute(int sleepMinute) {
        this.sleepMinute = sleepMinute;
    }

    public int getWakeHour() {
        return wakeHour;
    }

    public void setWakeHour(int wakeHour) {
        this.wakeHour = wakeHour;
    }

    public int getWakeMinute() {
        return wakeMinute;
    }

    public void setWakeMinute(int wakeMinute) {
        this.wakeMinute = wakeMinute;
    }
}






