package com.dolphin.focus.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.dolphin.focus.R;
import com.dolphin.focus.data.ConfigRepository;
import com.dolphin.focus.model.StrictnessMode;
import com.dolphin.focus.model.UserConfig;
import com.dolphin.focus.service.UsageMonitorService;
import com.dolphin.focus.ui.settings.SettingsActivity;
import com.dolphin.focus.util.TimeUtils;

public class MainActivity extends AppCompatActivity {
    private ConfigRepository configRepository;
    private TextView tvGoal;
    private TextView tvStrictnessMode;
    private TextView tvSleepSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        configRepository = new ConfigRepository(this);
        
        // Check if onboarding is complete
        if (!configRepository.isOnboardingComplete()) {
            startActivity(new Intent(this, com.dolphin.focus.ui.onboarding.OnboardingActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        tvGoal = findViewById(R.id.tvGoal);
        tvStrictnessMode = findViewById(R.id.tvStrictnessMode);
        tvSleepSchedule = findViewById(R.id.tvSleepSchedule);
        Button btnEditSettings = findViewById(R.id.btnEditSettings);

        btnEditSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // Start the monitoring service
        Intent serviceIntent = new Intent(this, UsageMonitorService.class);
        startForegroundService(serviceIntent);

        loadUserConfig();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserConfig();
    }

    private void loadUserConfig() {
        UserConfig config = configRepository.getUserConfig();

        if (config.getGoalText() != null && !config.getGoalText().isEmpty()) {
            tvGoal.setText(config.getGoalText());
        } else {
            tvGoal.setText("No goal set");
        }

        StrictnessMode mode = config.getStrictnessMode();
        String modeText;
        switch (mode) {
            case GENTLE:
                modeText = getString(R.string.strictness_gentle);
                break;
            case FIRM:
                modeText = getString(R.string.strictness_firm);
                break;
            case HARDCORE:
                modeText = getString(R.string.strictness_hardcore);
                break;
            default:
                modeText = getString(R.string.strictness_gentle);
        }
        tvStrictnessMode.setText(modeText);

        if (config.getSleepSchedule() != null) {
            String sleepTime = TimeUtils.formatTime(
                    config.getSleepSchedule().getSleepHour(),
                    config.getSleepSchedule().getSleepMinute()
            );
            String wakeTime = TimeUtils.formatTime(
                    config.getSleepSchedule().getWakeHour(),
                    config.getSleepSchedule().getWakeMinute()
            );
            tvSleepSchedule.setText(String.format("%s - %s", sleepTime, wakeTime));
        } else {
            tvSleepSchedule.setText("Not set");
        }
    }
}

