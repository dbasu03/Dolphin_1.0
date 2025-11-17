package com.dolphin.focus.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dolphin.focus.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
    private static final int PAGE_WELCOME = 0;
    private static final int PAGE_GOAL = 1;
    private static final int PAGE_FUTURE_SELF = 2;
    private static final int PAGE_ALLOWED_APPS = 3;
    private static final int PAGE_STRICTNESS = 4;
    private static final int PAGE_SLEEP = 5;
    private static final int PAGE_PERMISSIONS = 6;
    private static final int TOTAL_PAGES = 7;

    private OnboardingActivity activity;

    public OnboardingAdapter(OnboardingActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case PAGE_WELCOME:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_welcome, parent, false);
                break;
            case PAGE_GOAL:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_goal, parent, false);
                break;
            case PAGE_FUTURE_SELF:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_future_self, parent, false);
                break;
            case PAGE_ALLOWED_APPS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allowed_apps, parent, false);
                break;
            case PAGE_STRICTNESS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_strictness_mode, parent, false);
                break;
            case PAGE_SLEEP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_sleep_schedule, parent, false);
                break;
            case PAGE_PERMISSIONS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_permissions, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_welcome, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        activity.setupPage(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return TOTAL_PAGES;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
        }
    }
}





