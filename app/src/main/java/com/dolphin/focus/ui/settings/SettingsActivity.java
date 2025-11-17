package com.dolphin.focus.ui.settings;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dolphin.focus.R;
import com.dolphin.focus.data.ConfigRepository;
import com.dolphin.focus.model.SleepSchedule;
import com.dolphin.focus.model.StrictnessMode;
import com.dolphin.focus.model.UserConfig;
import com.dolphin.focus.util.AppUtils;
import com.dolphin.focus.util.PermissionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private ConfigRepository configRepository;
    private UserConfig userConfig;
    
    private TextInputEditText etGoal;
    private TextInputEditText etFutureSelf;
    private RadioGroup rgStrictness;
    private RadioButton rbGentle, rbFirm, rbHardcore;
    private TextView tvSleepTime, tvWakeTime;
    private MaterialCardView cardSleepTime, cardWakeTime;
    private Button btnEditApps;
    private Button btnUsagePermission;
    private Button btnNotificationPermission;
    private Button btnBatteryPermission;
    private Button btnSave;
    
    private int sleepHour, sleepMinute, wakeHour, wakeMinute;
    private Set<String> allowedApps = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        configRepository = new ConfigRepository(this);
        userConfig = configRepository.getUserConfig();

        setupToolbar();
        initViews();
        loadUserConfig();
        setupListeners();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        etGoal = findViewById(R.id.etGoal);
        etFutureSelf = findViewById(R.id.etFutureSelf);
        rgStrictness = findViewById(R.id.rgStrictness);
        rbGentle = findViewById(R.id.rbGentle);
        rbFirm = findViewById(R.id.rbFirm);
        rbHardcore = findViewById(R.id.rbHardcore);
        tvSleepTime = findViewById(R.id.tvSleepTime);
        tvWakeTime = findViewById(R.id.tvWakeTime);
        cardSleepTime = findViewById(R.id.cardSleepTime);
        cardWakeTime = findViewById(R.id.cardWakeTime);
        btnEditApps = findViewById(R.id.btnEditApps);
        btnUsagePermission = findViewById(R.id.btnUsagePermission);
        btnNotificationPermission = findViewById(R.id.btnNotificationPermission);
        btnBatteryPermission = findViewById(R.id.btnBatteryPermission);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadUserConfig() {
        etGoal.setText(userConfig.getGoalText());
        etFutureSelf.setText(userConfig.getFutureSelfText());

        StrictnessMode mode = userConfig.getStrictnessMode();
        switch (mode) {
            case GENTLE:
                rbGentle.setChecked(true);
                break;
            case FIRM:
                rbFirm.setChecked(true);
                break;
            case HARDCORE:
                rbHardcore.setChecked(true);
                break;
        }

        SleepSchedule schedule = userConfig.getSleepSchedule();
        if (schedule != null) {
            sleepHour = schedule.getSleepHour();
            sleepMinute = schedule.getSleepMinute();
            wakeHour = schedule.getWakeHour();
            wakeMinute = schedule.getWakeMinute();
            updateTimeDisplays();
        }

        allowedApps = new HashSet<>(userConfig.getAllowedApps());
    }

    private void updateTimeDisplays() {
        tvSleepTime.setText(formatTime(sleepHour, sleepMinute));
        tvWakeTime.setText(formatTime(wakeHour, wakeMinute));
    }

    private String formatTime(int hour, int minute) {
        String period = hour >= 12 ? "PM" : "AM";
        int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
        return String.format("%d:%02d %s", displayHour, minute, period);
    }

    private void setupListeners() {
        cardSleepTime.setOnClickListener(v -> showTimePicker(true));
        cardWakeTime.setOnClickListener(v -> showTimePicker(false));

        btnEditApps.setOnClickListener(v -> showAppSelectionDialog());
        btnUsagePermission.setOnClickListener(v -> PermissionUtils.openUsageAccessSettings(this));
        btnNotificationPermission.setOnClickListener(v ->
                PermissionUtils.requestNotificationPermission(this, 2001));
        btnBatteryPermission.setOnClickListener(v ->
                PermissionUtils.requestBatteryOptimizationException(this));

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void showTimePicker(boolean isSleepTime) {
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    if (isSleepTime) {
                        sleepHour = hourOfDay;
                        sleepMinute = minute;
                    } else {
                        wakeHour = hourOfDay;
                        wakeMinute = minute;
                    }
                    updateTimeDisplays();
                },
                isSleepTime ? sleepHour : wakeHour,
                isSleepTime ? sleepMinute : wakeMinute,
                false);
        dialog.show();
    }

    private void saveSettings() {
        String goal = etGoal.getText().toString().trim();
        String futureSelf = etFutureSelf.getText().toString().trim();

        if (goal.isEmpty()) {
            Toast.makeText(this, "Please enter your goal", Toast.LENGTH_SHORT).show();
            return;
        }

        if (futureSelf.isEmpty()) {
            Toast.makeText(this, "Please describe your future self", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allowedApps.isEmpty()) {
            Toast.makeText(this, getString(R.string.allowed_apps_error), Toast.LENGTH_SHORT).show();
            return;
        }

        userConfig.setGoalText(goal);
        userConfig.setFutureSelfText(futureSelf);

        StrictnessMode mode = StrictnessMode.GENTLE;
        if (rbFirm.isChecked()) {
            mode = StrictnessMode.FIRM;
        } else if (rbHardcore.isChecked()) {
            mode = StrictnessMode.HARDCORE;
        }
        userConfig.setStrictnessMode(mode);

        SleepSchedule schedule = new SleepSchedule(sleepHour, sleepMinute, wakeHour, wakeMinute);
        userConfig.setSleepSchedule(schedule);

        userConfig.setAllowedApps(new ArrayList<>(allowedApps));

        configRepository.saveUserConfig(userConfig);

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showAppSelectionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_app_selection, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewApps);
        Button btnDone = dialogView.findViewById(R.id.btnDone);

        List<AppUtils.AppInfo> apps = AppUtils.getAllInstalledApps(this);
        Set<String> tempAllowedApps = new HashSet<>(allowedApps);
        AppSelectionAdapter adapter = new AppSelectionAdapter(apps, tempAllowedApps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        android.app.AlertDialog dialog = builder.create();
        btnDone.setOnClickListener(v -> {
            allowedApps = new HashSet<>(tempAllowedApps);
            dialog.dismiss();
        });
        dialog.show();
    }

    private static class AppSelectionAdapter extends RecyclerView.Adapter<AppSelectionAdapter.ViewHolder> {
        private List<AppUtils.AppInfo> apps;
        private Set<String> allowedApps;

        AppSelectionAdapter(List<AppUtils.AppInfo> apps, Set<String> allowedApps) {
            this.apps = apps;
            this.allowedApps = allowedApps;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppUtils.AppInfo app = apps.get(position);
            holder.tvAppName.setText(app.appName);
            holder.ivAppIcon.setImageDrawable(app.icon);
            holder.cbSelected.setChecked(allowedApps.contains(app.packageName));

            holder.itemView.setOnClickListener(v -> {
                boolean isChecked = !holder.cbSelected.isChecked();
                holder.cbSelected.setChecked(isChecked);
                if (isChecked) {
                    allowedApps.add(app.packageName);
                } else {
                    allowedApps.remove(app.packageName);
                }
            });
            
            holder.cbSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    allowedApps.add(app.packageName);
                } else {
                    allowedApps.remove(app.packageName);
                }
            });
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAppName;
            android.widget.ImageView ivAppIcon;
            CheckBox cbSelected;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAppName = itemView.findViewById(R.id.tvAppName);
                ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
                cbSelected = itemView.findViewById(R.id.cbSelected);
            }
        }
    }
}

