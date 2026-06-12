package com.example.appquanlychitieu.ui.budget;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Budget;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.CategorySpent;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.data.repository.TransactionRepository;
import com.example.appquanlychitieu.util.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {
    private BudgetViewModel viewModel;
    private BudgetListAdapter adapter;
    private TransactionRepository transactionRepository;
    private List<Category> expenseCategories = new ArrayList<>();
    private Observer<List<Category>> categoriesObserver;
    private LiveData<List<CategorySpent>> spentLiveData;
    private Observer<List<CategorySpent>> spentObserver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView lvBudgets = view.findViewById(R.id.rv_budgets);
        TextView tvEmpty = view.findViewById(R.id.tv_empty);
        TextView tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_month);
        ImageButton btnNext = view.findViewById(R.id.btn_next_month);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_budget);

        // Setup ListView với BaseAdapter
        adapter = new BudgetListAdapter(requireContext());
        lvBudgets.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        transactionRepository = new TransactionRepository(requireActivity().getApplication());

        // Category cache - lưu observer để có thể remove
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        categoriesObserver = categories -> {
            Map<Long, Category> cache = new HashMap<>();
            expenseCategories.clear();
            for (Category c : categories) {
                cache.put(c.getId(), c);
                if (c.getType() == TransactionType.EXPENSE) expenseCategories.add(c);
            }
            adapter.setCategoryCache(cache);
        };
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categoriesObserver);

        // Danh sách ngân sách
        viewModel.getBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null && !budgets.isEmpty()) {
                adapter.setBudgets(budgets);
                lvBudgets.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                lvBudgets.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Tạo observer cho spent data
        spentObserver = spentList -> {
            Map<Long, Double> spentMap = new HashMap<>();
            if (spentList != null) {
                for (CategorySpent item : spentList)
                    spentMap.put(item.categoryId, item.spent);
            }
            adapter.setSpentMap(spentMap);
        };

        // Chi tiêu theo danh mục - observe tháng và update spent map
        viewModel.getSelectedMonthYear().observe(getViewLifecycleOwner(), monthYear -> {
            tvCurrentMonth.setText(DateUtils.formatDisplayMonth(
                    DateUtils.getStartOfMonth(monthYear[0], monthYear[1])));
            
            // Remove observer cũ nếu có
            if (spentLiveData != null) {
                spentLiveData.removeObserver(spentObserver);
            }
            
            // Tạo LiveData mới và observe
            long start = DateUtils.getStartOfMonth(monthYear[0], monthYear[1]);
            long end = DateUtils.getEndOfMonth(monthYear[0], monthYear[1]);
            spentLiveData = transactionRepository.getSpentPerCategory(viewModel.getUserId(), start, end);
            spentLiveData.observe(getViewLifecycleOwner(), spentObserver);
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

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_budget)
                .setView(layout)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    if (amountStr.isEmpty()) return;
                    Category selectedCategory = expenseCategories.get(spinner.getSelectedItemPosition());
                    int[] my = viewModel.getSelectedMonthYear().getValue();
                    if (my != null) {
                        String monthYear = String.format("%04d-%02d", my[0], my[1] + 1);
                        Budget budget = new Budget(selectedCategory.getId(),
                                Double.parseDouble(amountStr), monthYear, viewModel.getUserId());
                        viewModel.insertBudget(budget);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
