package com.dolphin.focus.util;

import com.dolphin.focus.model.SleepSchedule;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    public static boolean isInSleepWindow(LocalTime now, SleepSchedule schedule) {
        int nowMinutes = now.getHour() * 60 + now.getMinute();
        int sleepMinutes = schedule.getSleepHour() * 60 + schedule.getSleepMinute();
        int wakeMinutes = schedule.getWakeHour() * 60 + schedule.getWakeMinute();

        // Handle case where sleep time is after wake time (e.g., 11 PM to 7 AM)
        if (sleepMinutes > wakeMinutes) {
            // Sleep window spans midnight
            return nowMinutes >= sleepMinutes || nowMinutes < wakeMinutes;
        } else {
            // Sleep window is within the same day
            return nowMinutes >= sleepMinutes && nowMinutes < wakeMinutes;
        }
    }

    public static String formatTime(int hour, int minute) {
        LocalTime time = LocalTime.of(hour, minute);
        return time.format(TIME_FORMATTER);
    }

    public static String formatTime24h(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }
}






