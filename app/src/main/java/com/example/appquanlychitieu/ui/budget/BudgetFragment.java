package com.example.appquanlychitieu.ui.budget;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Budget;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.util.CurrencyFormatter;
import com.example.appquanlychitieu.util.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {
    private BudgetViewModel viewModel;
    private BudgetAdapter adapter;
    private List<Category> expenseCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvBudgets = view.findViewById(R.id.rv_budgets);
        TextView tvEmpty = view.findViewById(R.id.tv_empty);
        TextView tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_month);
        ImageButton btnNext = view.findViewById(R.id.btn_next_month);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_budget);

        adapter = new BudgetAdapter();
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgets.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Category cache
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            Map<Long, Category> cache = new HashMap<>();
            expenseCategories.clear();
            for (Category c : categories) {
                cache.put(c.getId(), c);
                if (c.getType() == TransactionType.EXPENSE) {
                    expenseCategories.add(c);
                }
            }
            adapter.setCategoryCache(cache);
        });

        // Month label
        viewModel.getSelectedMonthYear().observe(getViewLifecycleOwner(), monthYear -> {
            Calendar cal = Calendar.getInstance();
            cal.set(monthYear[0], monthYear[1], 1);
            tvCurrentMonth.setText(DateUtils.formatDisplayMonth(cal.getTimeInMillis()));
        });

        // Budgets
        viewModel.getBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null && !budgets.isEmpty()) {
                adapter.setBudgets(budgets);
                rvBudgets.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);

                // Calculate spent for each budget
                int[] my = viewModel.getSelectedMonthYear().getValue();
                if (my != null) {
                    long start = DateUtils.getStartOfMonth(my[0], my[1]);
                    long end = DateUtils.getEndOfMonth(my[0], my[1]);
                    Map<Long, Double> spentMap = new HashMap<>();
                    for (Budget b : budgets) {
                        db.transactionDao().getSpentByCategory(viewModel.getUserId(), b.getCategoryId(), start, end)
                                .observe(getViewLifecycleOwner(), spent -> {
                                    spentMap.put(b.getCategoryId(), spent != null ? spent : 0);
                                    adapter.setSpentMap(spentMap);
                                });
                    }
                }
            } else {
                rvBudgets.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        btnPrev.setOnClickListener(v -> viewModel.previousMonth());
        btnNext.setOnClickListener(v -> viewModel.nextMonth());
        fabAdd.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void showAddBudgetDialog() {
        if (expenseCategories.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có danh mục chi tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.simple_list_item_1, null);
        // Build a simple dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.add_budget);

        View customView = LayoutInflater.from(requireContext()).inflate(
                android.R.layout.simple_spinner_dropdown_item, null);

        // Create custom layout programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        Spinner spinner = new Spinner(requireContext());
        List<String> categoryNames = new ArrayList<>();
        for (Category c : expenseCategories) categoryNames.add(c.getName());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        layout.addView(spinner);

        EditText etAmount = new EditText(requireContext());
        etAmount.setHint("Hạn mức (VNĐ)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etAmount);

        builder.setView(layout);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) return;

            int selectedIndex = spinner.getSelectedItemPosition();
            Category selectedCategory = expenseCategories.get(selectedIndex);

            int[] my = viewModel.getSelectedMonthYear().getValue();
            if (my != null) {
                String monthYear = String.format("%04d-%02d", my[0], my[1] + 1);
                Budget budget = new Budget(selectedCategory.getId(), Double.parseDouble(amountStr), monthYear, viewModel.getUserId());
                viewModel.insertBudget(budget);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}
