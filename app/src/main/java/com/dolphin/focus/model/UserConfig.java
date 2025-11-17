package com.dolphin.focus.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserConfig implements Serializable {
    private String goalText;
    private String futureSelfText;
    @SerializedName(value = "allowedApps", alternate = {"distractionApps"})
    private List<String> allowedApps;
    private StrictnessMode strictnessMode;
    private SleepSchedule sleepSchedule;

    public UserConfig() {
        this.goalText = "";
        this.futureSelfText = "";
        this.allowedApps = new ArrayList<>();
        this.strictnessMode = StrictnessMode.GENTLE;
        this.sleepSchedule = new SleepSchedule();
    }

    public String getGoalText() {
        return goalText;
    }

    public void setGoalText(String goalText) {
        this.goalText = goalText;
    }

    public String getFutureSelfText() {
        return futureSelfText;
    }

    public void setFutureSelfText(String futureSelfText) {
        this.futureSelfText = futureSelfText;
    }

    public List<String> getAllowedApps() {
        if (allowedApps == null) {
            allowedApps = new ArrayList<>();
        }
        return allowedApps;
    }

    public void setAllowedApps(List<String> allowedApps) {
        this.allowedApps = allowedApps;
    }

    public StrictnessMode getStrictnessMode() {
        return strictnessMode;
    }

    public void setStrictnessMode(StrictnessMode strictnessMode) {
        this.strictnessMode = strictnessMode;
    }

    public SleepSchedule getSleepSchedule() {
        return sleepSchedule;
    }

    public void setSleepSchedule(SleepSchedule sleepSchedule) {
        this.sleepSchedule = sleepSchedule;
    }
}





