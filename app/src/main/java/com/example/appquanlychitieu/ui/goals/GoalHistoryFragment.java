package com.example.appquanlychitieu.ui.goals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlychitieu.R;

public class GoalHistoryFragment extends Fragment {

    private GoalViewModel viewModel;
    private GoalHistoryAdapter adapter;

    // UI Variables
    private android.widget.TextView tvTitle;
    private android.widget.ImageButton btnBack;
    private android.widget.ListView rvHistory;
    private LinearLayout layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goal_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long goalId = 0;
        String goalName = "Lịch sử nạp";
        
        if (getArguments() != null) {
            goalId = getArguments().getLong("goalId", 0);
            goalName = getArguments().getString("goalName", "Lịch sử nạp");
        }

        tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText("Lịch sử nạp: " + goalName);
        
        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        rvHistory = view.findViewById(R.id.rv_goal_history);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        adapter = new GoalHistoryAdapter(requireContext());
        rvHistory.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        if (goalId != 0) {
            viewModel.getHistoryForGoal(goalId).observe(getViewLifecycleOwner(), historyList -> {
                if (historyList != null && !historyList.isEmpty()) {
                    adapter.setHistoryList(historyList);
                    rvHistory.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                } else {
                    rvHistory.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
