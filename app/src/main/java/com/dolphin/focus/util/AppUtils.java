package com.dolphin.focus.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {
    
    public static class AppInfo {
        public String packageName;
        public String appName;
        public Drawable icon;
        
        public AppInfo(String packageName, String appName, Drawable icon) {
            this.packageName = packageName;
            this.appName = appName;
            this.icon = icon;
        }
    }

    public static List<AppInfo> getAllInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> appList = new ArrayList<>();

        for (ApplicationInfo packageInfo : packages) {
            // Skip system apps and Dolphin itself
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 
                    && !packageInfo.packageName.equals(context.getPackageName())) {
                try {
                    String appName = pm.getApplicationLabel(packageInfo).toString();
                    Drawable icon = pm.getApplicationIcon(packageInfo);
                    appList.add(new AppInfo(packageInfo.packageName, appName, icon));
                } catch (Exception e) {
                    // Skip apps that can't be loaded
                }
            }
        }

        // Sort by app name
        appList.sort((a, b) -> a.appName.compareToIgnoreCase(b.appName));
        return appList;
    }

    public static String getAppName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(appInfo).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationIcon(packageName);
        } catch (Exception e) {
            return context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        }
    }
}






