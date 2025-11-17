package com.dolphin.focus.ui.onboarding;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.dolphin.focus.R;
import com.dolphin.focus.data.ConfigRepository;
import com.dolphin.focus.model.SleepSchedule;
import com.dolphin.focus.model.StrictnessMode;
import com.dolphin.focus.model.UserConfig;
import com.dolphin.focus.ui.main.MainActivity;
import com.dolphin.focus.util.AppUtils;
import com.dolphin.focus.util.PermissionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button btnNext;
    private Button btnBack;
    private OnboardingAdapter adapter;
    private ConfigRepository configRepository;
    private UserConfig tempConfig;
    private Set<String> allowedApps = new HashSet<>();
    private int sleepHour = 23, sleepMinute = 0;
    private int wakeHour = 7, wakeMinute = 0;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        configRepository = new ConfigRepository(this);
        tempConfig = new UserConfig();

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < adapter.getItemCount() - 1) {
                if (validateCurrentPage(current)) {
                    viewPager.setCurrentItem(current + 1);
                }
            } else {
                finishOnboarding();
            }
        });

        btnBack.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1);
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateButtons();
            }
        });

        updateButtons();
    }

    private void updateButtons() {
        int current = viewPager.getCurrentItem();
        btnBack.setVisibility(current > 0 ? View.VISIBLE : View.GONE);
        btnNext.setText(current == adapter.getItemCount() - 1 ? getString(R.string.finish) : getString(R.string.next));
    }

    public void setupPage(View view, int position) {
        switch (position) {
            case 0: // Welcome
                break;
            case 1: // Goal
                setupGoalPage(view);
                break;
            case 2: // Future Self
                setupFutureSelfPage(view);
                break;
            case 3: // Allowed Apps
                setupAllowedAppsPage(view);
                break;
            case 4: // Strictness Mode
                setupStrictnessPage(view);
                break;
            case 5: // Sleep Schedule
                setupSleepSchedulePage(view);
                break;
            case 6: // Permissions
                setupPermissionsPage(view);
                break;
        }
    }

    private void setupGoalPage(View view) {
        TextInputEditText etGoal = view.findViewById(R.id.etGoal);
        if (etGoal != null && tempConfig.getGoalText() != null) {
            etGoal.setText(tempConfig.getGoalText());
        }
    }

    private void setupFutureSelfPage(View view) {
        TextInputEditText etFutureSelf = view.findViewById(R.id.etFutureSelf);
        if (etFutureSelf != null && tempConfig.getFutureSelfText() != null) {
            etFutureSelf.setText(tempConfig.getFutureSelfText());
        }
    }

    private void setupAllowedAppsPage(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewApps);
        if (recyclerView != null) {
            List<AppUtils.AppInfo> apps = AppUtils.getAllInstalledApps(this);
            AppSelectionAdapter appAdapter = new AppSelectionAdapter(apps, allowedApps);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(appAdapter);
        }
    }

    private void setupStrictnessPage(View view) {
        MaterialCardView cardGentle = view.findViewById(R.id.cardGentle);
        MaterialCardView cardFirm = view.findViewById(R.id.cardFirm);
        MaterialCardView cardHardcore = view.findViewById(R.id.cardHardcore);

        if (cardGentle != null) {
            cardGentle.setOnClickListener(v -> selectStrictnessMode(view, StrictnessMode.GENTLE));
            cardFirm.setOnClickListener(v -> selectStrictnessMode(view, StrictnessMode.FIRM));
            cardHardcore.setOnClickListener(v -> selectStrictnessMode(view, StrictnessMode.HARDCORE));
        }
    }

    private void selectStrictnessMode(View view, StrictnessMode mode) {
        tempConfig.setStrictnessMode(mode);
        MaterialCardView cardGentle = view.findViewById(R.id.cardGentle);
        MaterialCardView cardFirm = view.findViewById(R.id.cardFirm);
        MaterialCardView cardHardcore = view.findViewById(R.id.cardHardcore);

        cardGentle.setStrokeWidth(mode == StrictnessMode.GENTLE ? 4 : 0);
        cardFirm.setStrokeWidth(mode == StrictnessMode.FIRM ? 4 : 0);
        cardHardcore.setStrokeWidth(mode == StrictnessMode.HARDCORE ? 4 : 0);
    }

    private void setupSleepSchedulePage(View view) {
        TextView tvSleepTime = view.findViewById(R.id.tvSleepTime);
        TextView tvWakeTime = view.findViewById(R.id.tvWakeTime);
        MaterialCardView cardSleepTime = view.findViewById(R.id.cardSleepTime);
        MaterialCardView cardWakeTime = view.findViewById(R.id.cardWakeTime);

        if (tvSleepTime != null) {
            tvSleepTime.setText(formatTime(sleepHour, sleepMinute));
            tvWakeTime.setText(formatTime(wakeHour, wakeMinute));

            cardSleepTime.setOnClickListener(v -> showTimePicker(true));
            cardWakeTime.setOnClickListener(v -> showTimePicker(false));
        }
    }

    private void showTimePicker(boolean isSleepTime) {
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    if (isSleepTime) {
                        sleepHour = hourOfDay;
                        sleepMinute = minute;
                        updateSleepTimeDisplay();
                    } else {
                        wakeHour = hourOfDay;
                        wakeMinute = minute;
                        updateWakeTimeDisplay();
                    }
                },
                isSleepTime ? sleepHour : wakeHour,
                isSleepTime ? sleepMinute : wakeMinute,
                false);
        dialog.show();
    }

    private void updateSleepTimeDisplay() {
        View currentView = getCurrentPageView();
        if (currentView != null) {
            TextView tvSleepTime = currentView.findViewById(R.id.tvSleepTime);
            if (tvSleepTime != null) {
                tvSleepTime.setText(formatTime(sleepHour, sleepMinute));
            }
        }
    }

    private void updateWakeTimeDisplay() {
        View currentView = getCurrentPageView();
        if (currentView != null) {
            TextView tvWakeTime = currentView.findViewById(R.id.tvWakeTime);
            if (tvWakeTime != null) {
                tvWakeTime.setText(formatTime(wakeHour, wakeMinute));
            }
        }
    }

    private View getCurrentPageView() {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        if (recyclerView != null) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(viewPager.getCurrentItem());
            if (holder != null) {
                return holder.itemView;
            }
        }
        return null;
    }

    private String formatTime(int hour, int minute) {
        String period = hour >= 12 ? "PM" : "AM";
        int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
        return String.format("%d:%02d %s", displayHour, minute, period);
    }

    private void setupPermissionsPage(View view) {
        Button btnUsageAccess = view.findViewById(R.id.btnUsageAccess);
        Button btnNotification = view.findViewById(R.id.btnNotification);
        Button btnBattery = view.findViewById(R.id.btnBatteryOptimization);

        btnUsageAccess.setOnClickListener(v -> PermissionUtils.openUsageAccessSettings(this));
        btnNotification.setOnClickListener(v -> PermissionUtils.requestNotificationPermission(this, REQUEST_NOTIFICATION_PERMISSION));
        btnBattery.setOnClickListener(v -> {
            PermissionUtils.requestBatteryOptimizationException(this);
            updatePermissionStatus(view);
        });

        updatePermissionStatus(view);
    }

    private void updatePermissionStatus(View view) {
        TextView tvUsageStatus = view.findViewById(R.id.tvUsageAccessStatus);
        TextView tvNotificationStatus = view.findViewById(R.id.tvNotificationStatus);
        TextView tvBatteryStatus = view.findViewById(R.id.tvBatteryStatus);

        tvUsageStatus.setVisibility(PermissionUtils.hasUsageAccess(this) ? View.VISIBLE : View.GONE);
        tvNotificationStatus.setVisibility(PermissionUtils.hasNotificationPermission(this) ? View.VISIBLE : View.GONE);
        tvBatteryStatus.setVisibility(PermissionUtils.hasBatteryOptimizationException(this) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            View currentView = getCurrentPageView();
            if (currentView != null) {
                updatePermissionStatus(currentView);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View currentView = getCurrentPageView();
        if (currentView != null && viewPager.getCurrentItem() == 6) {
            updatePermissionStatus(currentView);
        }
    }

    private boolean validateCurrentPage(int position) {
        switch (position) {
            case 1: // Goal
                View goalView = getCurrentPageView();
                if (goalView != null) {
                    TextInputEditText etGoal = goalView.findViewById(R.id.etGoal);
                    if (etGoal != null) {
                        String goal = etGoal.getText().toString().trim();
                        if (goal.isEmpty()) {
                            Toast.makeText(this, "Please enter your goal", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        tempConfig.setGoalText(goal);
                    }
                }
                break;
            case 2: // Future Self
                View futureSelfView = getCurrentPageView();
                if (futureSelfView != null) {
                    TextInputEditText etFutureSelf = futureSelfView.findViewById(R.id.etFutureSelf);
                    if (etFutureSelf != null) {
                        String futureSelf = etFutureSelf.getText().toString().trim();
                        if (futureSelf.isEmpty()) {
                            Toast.makeText(this, "Please describe your future self", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        tempConfig.setFutureSelfText(futureSelf);
                    }
                }
                break;
            case 3: // Allowed Apps
                if (allowedApps.isEmpty()) {
                    Toast.makeText(this, getString(R.string.allowed_apps_error), Toast.LENGTH_SHORT).show();
                    return false;
                }
                tempConfig.setAllowedApps(new ArrayList<>(allowedApps));
                break;
        }
        return true;
    }

    private void finishOnboarding() {
        if (!validateCurrentPage(viewPager.getCurrentItem())) {
            return;
        }

        if (!PermissionUtils.hasUsageAccess(this)
                || !PermissionUtils.hasNotificationPermission(this)
                || !PermissionUtils.hasBatteryOptimizationException(this)) {
            Toast.makeText(this, "Please grant all required permissions", Toast.LENGTH_LONG).show();
            return;
        }

        SleepSchedule schedule = new SleepSchedule(sleepHour, sleepMinute, wakeHour, wakeMinute);
        tempConfig.setSleepSchedule(schedule);

        configRepository.saveUserConfig(tempConfig);
        configRepository.setOnboardingComplete(true);

        startActivity(new Intent(this, MainActivity.class));
        finish();
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

