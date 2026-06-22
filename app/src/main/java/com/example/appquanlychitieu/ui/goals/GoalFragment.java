package com.example.appquanlychitieu.ui.goals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.Goal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GoalFragment extends Fragment implements GoalListAdapter.OnGoalInteractionListener {
    private GoalViewModel viewModel;
    private GoalListAdapter adapter;

    private ListView lvGoals;
    private View layoutEmptyState;
    private FloatingActionButton fabAddGoal;
    
    private LinearLayout dialogLayout;
    private EditText etGoalName, etGoalAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvGoals = view.findViewById(R.id.rv_goals);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        fabAddGoal = view.findViewById(R.id.fab_add_goal);

        adapter = new GoalListAdapter(requireContext(), this);
        lvGoals.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        viewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            if (goals != null && !goals.isEmpty()) {
                adapter.setGoals(goals);
                lvGoals.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            } else {
                lvGoals.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            }
        });

        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    private void showAddGoalDialog() {
        dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 24, 48, 24);

        etGoalName = new EditText(requireContext());
        etGoalName.setHint("Tên mục tiêu (VD: Mua xe, Du lịch...)");
        dialogLayout.addView(etGoalName);

        etGoalAmount = new EditText(requireContext());
        etGoalAmount.setHint("Số tiền mục tiêu (VNĐ)");
        etGoalAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etGoalAmount.setKeyListener(android.text.method.DigitsKeyListener.getInstance("0123456789.,"));
        etGoalAmount.addTextChangedListener(new com.example.appquanlychitieu.util.NumberTextWatcher(etGoalAmount));
        dialogLayout.addView(etGoalAmount);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm mục tiêu mới")
                .setView(dialogLayout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etGoalName.getText().toString().trim();
                    String amountStr = etGoalAmount.getText().toString().trim();
                    if (name.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    amountStr = amountStr.replace(".", "");
                    double targetAmount = Double.parseDouble(amountStr);
                    Goal goal = new Goal(name, targetAmount, 0, viewModel.getUserId());
                    viewModel.insertGoal(goal);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onAddFundsClick(Goal goal) {
        dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 24, 48, 24);

        etGoalAmount = new EditText(requireContext());
        etGoalAmount.setHint("Số tiền nạp thêm (VNĐ)");
        etGoalAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etGoalAmount.setKeyListener(android.text.method.DigitsKeyListener.getInstance("0123456789.,"));
        etGoalAmount.addTextChangedListener(new com.example.appquanlychitieu.util.NumberTextWatcher(etGoalAmount));
        dialogLayout.addView(etGoalAmount);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật tiến độ: " + goal.getName())
                .setView(dialogLayout)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String amountStr = etGoalAmount.getText().toString().trim();
                    if (amountStr.isEmpty()) return;
                    amountStr = amountStr.replace(".", "");
                    double addedAmount = Double.parseDouble(amountStr);
                    
                    double oldAmount = goal.getCurrentAmount();
                    double newAmount = oldAmount + addedAmount;
                    
                    goal.setCurrentAmount(newAmount);
                    viewModel.updateGoal(goal);
                    
                    com.example.appquanlychitieu.data.model.GoalHistory history = new com.example.appquanlychitieu.data.model.GoalHistory(goal.getId(), addedAmount, System.currentTimeMillis());
                    viewModel.insertGoalHistory(history);
                    
                    if (oldAmount < goal.getTargetAmount() && newAmount >= goal.getTargetAmount()) {
                        Toast.makeText(requireContext(), "🎉 Chúc mừng! Bạn đã hoàn thành mục tiêu: " + goal.getName(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onGoalClick(Goal goal) {
        Bundle bundle = new Bundle();
        bundle.putLong("goalId", goal.getId());
        bundle.putString("goalName", goal.getName());
        
        androidx.navigation.Navigation.findNavController(requireView())
            .navigate(R.id.action_goals_to_goal_history, bundle);
    }

    @Override
    public void onGoalLongClick(Goal goal) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa mục tiêu")
                .setMessage("Bạn có chắc muốn xóa mục tiêu này không?")
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteGoal(goal))
                .setNegativeButton("Hủy", null)
                .show();
    }
}
