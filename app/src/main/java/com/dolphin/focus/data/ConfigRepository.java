package com.dolphin.focus.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.dolphin.focus.model.SleepSchedule;
import com.dolphin.focus.model.StrictnessMode;
import com.dolphin.focus.model.UserConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConfigRepository {
    private static final String PREFS_NAME = "dolphin_config";
    private static final String KEY_USER_CONFIG = "user_config";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";
    
    private SharedPreferences prefs;
    private Gson gson;

    public ConfigRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public UserConfig getUserConfig() {
        String configJson = prefs.getString(KEY_USER_CONFIG, null);
        if (configJson == null) {
            return new UserConfig();
        }
        
        try {
            return gson.fromJson(configJson, UserConfig.class);
        } catch (Exception e) {
            return new UserConfig();
        }
    }

    public void saveUserConfig(UserConfig config) {
        String configJson = gson.toJson(config);
        prefs.edit().putString(KEY_USER_CONFIG, configJson).apply();
    }

    public boolean isOnboardingComplete() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    public void setOnboardingComplete(boolean complete) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply();
    }
}






