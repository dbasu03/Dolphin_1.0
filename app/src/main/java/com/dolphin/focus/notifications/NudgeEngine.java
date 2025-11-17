package com.dolphin.focus.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.dolphin.focus.R;
import com.dolphin.focus.model.StrictnessMode;
import com.dolphin.focus.model.UserConfig;
import com.dolphin.focus.ui.main.MainActivity;
import com.dolphin.focus.util.TimeUtils;

public class NudgeEngine {
    private static final String CHANNEL_MONITOR = "channel_monitor";
    private static final String CHANNEL_NUDGES = "channel_nudges";
    private static final int NOTIFICATION_ID_MONITOR = 1001;
    private static final int NOTIFICATION_ID_NUDGE = 1002;

    private Context context;
    private NotificationManager notificationManager;

    public NudgeEngine(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel monitorChannel = new NotificationChannel(
                    CHANNEL_MONITOR,
                    "Monitoring Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            monitorChannel.setDescription("Foreground service notification");
            monitorChannel.setShowBadge(false);

            NotificationChannel nudgeChannel = new NotificationChannel(
                    CHANNEL_NUDGES,
                    "Focus Nudges",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nudgeChannel.setDescription("Notifications to help you stay focused");
            nudgeChannel.enableVibration(true);

            notificationManager.createNotificationChannel(monitorChannel);
            notificationManager.createNotificationChannel(nudgeChannel);
        }
    }

    public Notification createMonitorNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, CHANNEL_MONITOR)
                .setContentTitle(context.getString(R.string.monitor_notification_title))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void showStrictnessNudge(UserConfig config, String appName, StrictnessMode mode, int stage, long usageMinutes) {
        TitleBody copy = buildCopy(config, appName, mode, stage, usageMinutes);

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent refocusIntent = new Intent(context, MainActivity.class);
        refocusIntent.putExtra("action", "refocus");
        PendingIntent refocusPendingIntent = PendingIntent.getActivity(
                context, 1, refocusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_NUDGES)
                .setContentTitle(copy.title)
                .setContentText(copy.body)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_menu_view, context.getString(R.string.action_refocus), refocusPendingIntent);

        if (!(mode == StrictnessMode.HARDCORE && stage == 3)) {
            Intent moreTimeIntent = new Intent(context, MainActivity.class);
            moreTimeIntent.putExtra("action", "more_time");
            PendingIntent moreTimePendingIntent = PendingIntent.getActivity(
                    context, 2, moreTimeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(android.R.drawable.ic_menu_recent_history, context.getString(R.string.action_5_more), moreTimePendingIntent);
        }

        if ((mode == StrictnessMode.FIRM && stage == 3) || mode == StrictnessMode.HARDCORE) {
            builder.setOngoing(true);
        }

        notificationManager.notify(NOTIFICATION_ID_NUDGE, builder.build());
    }

    public void showLateNightNudge(UserConfig config, String appName) {
        String sleepTime = TimeUtils.formatTime(
                config.getSleepSchedule().getSleepHour(),
                config.getSleepSchedule().getSleepMinute()
        );
        String body = String.format(context.getString(R.string.nudge_late_night_body), sleepTime);

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context, 3, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_NUDGES)
                .setContentTitle(context.getString(R.string.nudge_late_night_title))
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(NOTIFICATION_ID_NUDGE + 1, builder.build());
    }

    private TitleBody buildCopy(UserConfig config, String appName, StrictnessMode mode, int stage, long minutes) {
        switch (mode) {
            case FIRM:
                return buildFirmCopy(config, appName, stage, minutes);
            case HARDCORE:
                return buildHardcoreCopy(config, appName, stage, minutes);
            case GENTLE:
            default:
                return buildGentleCopy(appName, stage, minutes);
        }
    }

    private TitleBody buildGentleCopy(String appName, int stage, long minutes) {
        switch (stage) {
            case 1:
                return new TitleBody(
                        context.getString(R.string.nudge_gentle_stage1_title),
                        String.format(context.getString(R.string.nudge_gentle_stage1_body), appName, minutes)
                );
            case 2:
                return new TitleBody(
                        context.getString(R.string.nudge_gentle_stage2_title),
                        String.format(context.getString(R.string.nudge_gentle_stage2_body), appName, minutes)
                );
            case 3:
            default:
                return new TitleBody(
                        context.getString(R.string.nudge_gentle_stage3_title),
                        String.format(context.getString(R.string.nudge_gentle_stage3_body), appName, minutes)
                );
        }
    }

    private TitleBody buildFirmCopy(UserConfig config, String appName, int stage, long minutes) {
        switch (stage) {
            case 1:
                return new TitleBody(
                        context.getString(R.string.nudge_firm_stage1_title),
                        String.format(context.getString(R.string.nudge_firm_stage1_body), config.getGoalText(), appName, minutes)
                );
            case 2:
                return new TitleBody(
                        context.getString(R.string.nudge_firm_stage2_title),
                        String.format(context.getString(R.string.nudge_firm_stage2_body), config.getGoalText(), appName, minutes)
                );
            case 3:
            default:
                return new TitleBody(
                        context.getString(R.string.nudge_firm_stage3_title),
                        String.format(context.getString(R.string.nudge_firm_stage3_body), config.getGoalText(), appName, minutes)
                );
        }
    }

    private TitleBody buildHardcoreCopy(UserConfig config, String appName, int stage, long minutes) {
        switch (stage) {
            case 1:
                return new TitleBody(
                        context.getString(R.string.nudge_hardcore_stage1_title),
                        String.format(context.getString(R.string.nudge_hardcore_stage1_body), config.getFutureSelfText(), appName, minutes)
                );
            case 2:
                return new TitleBody(
                        context.getString(R.string.nudge_hardcore_stage2_title),
                        String.format(context.getString(R.string.nudge_hardcore_stage2_body), config.getFutureSelfText(), appName, minutes)
                );
            case 3:
            default:
                return new TitleBody(
                        context.getString(R.string.nudge_hardcore_stage3_title),
                        String.format(context.getString(R.string.nudge_hardcore_stage3_body), config.getFutureSelfText(), appName, minutes)
                );
        }
    }

    private static class TitleBody {
        final String title;
        final String body;

        TitleBody(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    public void cancelNudge() {
        notificationManager.cancel(NOTIFICATION_ID_NUDGE);
    }
}





