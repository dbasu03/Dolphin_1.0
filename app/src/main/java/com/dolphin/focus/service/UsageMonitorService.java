package com.dolphin.focus.service;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import com.dolphin.focus.data.ConfigRepository;
import com.dolphin.focus.model.StrictnessMode;
import com.dolphin.focus.model.UserConfig;
import com.dolphin.focus.notifications.NudgeEngine;
import com.dolphin.focus.util.AppUtils;
import com.dolphin.focus.util.TimeUtils;
import java.time.LocalTime;
import java.util.List;

public class UsageMonitorService extends Service {
    private static final long POLL_INTERVAL_MS = 15000; // 15 seconds

    private Handler handler;
    private Runnable monitorRunnable;
    private UsageStatsManager usageStatsManager;
    private ConfigRepository configRepository;
    private NudgeEngine nudgeEngine;
    
    private String currentForegroundApp = null;
    private long appStartTime = 0;
    private int currentNudgeStage = 0;
    private boolean lateNightAlertSent = false;
    private boolean isMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        configRepository = new ConfigRepository(this);
        nudgeEngine = new NudgeEngine(this);
        
        startForeground(1001, nudgeEngine.createMonitorNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isMonitoring) {
            isMonitoring = true;
            startMonitoring();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMonitoring() {
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkForegroundApp();
                handler.postDelayed(this, POLL_INTERVAL_MS);
            }
        };
        handler.post(monitorRunnable);
    }

    private void stopMonitoring() {
        if (monitorRunnable != null) {
            handler.removeCallbacks(monitorRunnable);
        }
        isMonitoring = false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), UsageMonitorService.class);
        restartServiceIntent.setPackage(getPackageName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartServiceIntent);
        } else {
            startService(restartServiceIntent);
        }
        super.onTaskRemoved(rootIntent);
    }

    private void checkForegroundApp() {
        long currentTime = System.currentTimeMillis();
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 60000,
                currentTime
        );

        String foregroundApp = null;
        long lastTimeUsed = 0;

        for (UsageStats usageStats : stats) {
            if (usageStats.getLastTimeUsed() > lastTimeUsed) {
                lastTimeUsed = usageStats.getLastTimeUsed();
                foregroundApp = usageStats.getPackageName();
            }
        }

        if (foregroundApp == null) {
            return;
        }

        UserConfig config = configRepository.getUserConfig();
        List<String> allowedApps = config.getAllowedApps();

        if (isAppAllowed(allowedApps, foregroundApp)) {
            clearCurrentTracking();
            return;
        }

        if (!foregroundApp.equals(currentForegroundApp)) {
            currentForegroundApp = foregroundApp;
            appStartTime = SystemClock.elapsedRealtime();
            currentNudgeStage = 0;
            lateNightAlertSent = false;
        }

        long usageMillis = SystemClock.elapsedRealtime() - appStartTime;
        long usageMinutes = usageMillis / 60000;
        StrictnessMode mode = config.getStrictnessMode();
        long[] thresholds = getThresholdsForMode(mode);

        if (currentNudgeStage < thresholds.length && usageMillis >= thresholds[currentNudgeStage]) {
            String appName = AppUtils.getAppName(this, foregroundApp);
            nudgeEngine.showStrictnessNudge(config, appName, mode, currentNudgeStage + 1, usageMinutes);
            currentNudgeStage++;
        }

        boolean isLateNight = TimeUtils.isInSleepWindow(LocalTime.now(), config.getSleepSchedule());
        if (isLateNight && !lateNightAlertSent) {
            String appName = AppUtils.getAppName(this, foregroundApp);
            nudgeEngine.showLateNightNudge(config, appName);
            lateNightAlertSent = true;
        }
    }

    private void clearCurrentTracking() {
        currentForegroundApp = null;
        appStartTime = 0;
        currentNudgeStage = 0;
        lateNightAlertSent = false;
    }

    private long[] getThresholdsForMode(StrictnessMode mode) {
        switch (mode) {
            case HARDCORE:
                return new long[]{2 * 60_000L, 3 * 60_000L, 5 * 60_000L};
            case FIRM:
                return new long[]{5 * 60_000L, 7 * 60_000L, 10 * 60_000L};
            case GENTLE:
            default:
                return new long[]{10 * 60_000L, 15 * 60_000L, 20 * 60_000L};
        }
    }

    private boolean isAppAllowed(List<String> allowedApps, String packageName) {
        if (packageName == null) {
            return true;
        }
        if (packageName.equals(getPackageName()) || isSystemApp(packageName)) {
            return true;
        }
        return allowedApps != null && allowedApps.contains(packageName);
    }

    private boolean isSystemApp(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}




