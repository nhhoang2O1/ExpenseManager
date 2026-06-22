package com.example.appquanlychitieu.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Budget;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.CategorySpent;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.data.repository.TransactionRepository;
import com.example.appquanlychitieu.ui.budget.BudgetListAdapter;
import com.example.appquanlychitieu.ui.budget.BudgetViewModel;
import com.example.appquanlychitieu.util.DateUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionListFragment extends Fragment {
    // Transaction
    private TransactionListViewModel viewModel;
    private TransactionListAdapter adapter;


    // Budget
    private BudgetViewModel budgetViewModel;
    private BudgetListAdapter budgetAdapter;
    private TransactionRepository transactionRepository;
    private List<Category> expenseCategories = new ArrayList<>();
    private LiveData<List<CategorySpent>> spentLiveData;
    private Observer<List<CategorySpent>> spentObserver;
    private LinearLayout llBudgetsContainer;
    private View layoutEmptyBudget;
    
    // UI Variables
    private ListView lvTransactions;
    private View layoutEmptyState;
    private android.widget.Button btnEmptyCta;
    private TextView tvEmptyTitle, tvEmptyDesc;
    private Chip chipAll, chipExpense, chipIncome;
    private View btnFilterDate;
    private TextView tvCurrentMonth;
    private ImageButton btnPrev, btnNext, btnAddBudget;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ==== TRANSACTION SETUP ====
        lvTransactions = view.findViewById(R.id.rv_transactions);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnEmptyCta = view.findViewById(R.id.btn_empty_cta);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptyDesc = view.findViewById(R.id.tv_empty_desc);
        tvEmptyTitle.setText(R.string.empty_transaction_title);
        tvEmptyDesc.setText(R.string.empty_transaction_desc);
        btnEmptyCta.setText(R.string.empty_transaction_cta);
        
        chipAll = view.findViewById(R.id.chip_all);
        chipExpense = view.findViewById(R.id.chip_expense);
        chipIncome = view.findViewById(R.id.chip_income);
        
        btnFilterDate = view.findViewById(R.id.btn_filter_date);

        adapter = new TransactionListAdapter(requireContext());
        lvTransactions.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TransactionListViewModel.class);

        // ==== BUDGET SETUP ====
        llBudgetsContainer = view.findViewById(R.id.ll_budgets_container);
        layoutEmptyBudget = view.findViewById(R.id.layout_empty_budget);
        tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        btnPrev = view.findViewById(R.id.btn_prev_month);
        btnNext = view.findViewById(R.id.btn_next_month);
        btnAddBudget = view.findViewById(R.id.btn_add_budget);

        budgetAdapter = new BudgetListAdapter(requireContext());
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        transactionRepository = new TransactionRepository(requireActivity().getApplication());

        // Sync adapter with LinearLayout
        budgetAdapter.registerDataSetObserver(new android.database.DataSetObserver() {
            @Override
            public void onChanged() {
                llBudgetsContainer.removeAllViews();
                for (int i = 0; i < budgetAdapter.getCount(); i++) {
                    View v = budgetAdapter.getView(i, null, llBudgetsContainer);
                    llBudgetsContainer.addView(v);
                }
            }
        });

        // Category cache
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            Map<Long, Category> cache = new HashMap<>();
            expenseCategories.clear();
            for (Category c : categories) {
                cache.put(c.getId(), c);
                if (c.getType() == TransactionType.EXPENSE) expenseCategories.add(c);
            }
            adapter.setCategoryCache(cache);
            budgetAdapter.setCategoryCache(cache);
        });

        // Observe danh sách giao dịch
        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setTransactions(transactions);
                lvTransactions.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            } else {
                lvTransactions.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            }
        });

        // Filter chips
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilterType("ALL");
        });
        chipExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilterType("EXPENSE");
        });
        chipIncome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilterType("INCOME");
        });

        // Date Filter
        btnFilterDate.setOnClickListener(v -> showDateRangePicker());

        // CTA empty state
        btnEmptyCta.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
            if (chipIncome.isChecked()) {
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TransactionType.INCOME.name());
            } else if (chipExpense.isChecked()) {
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TransactionType.EXPENSE.name());
            }
            startActivity(intent);
        });

        // Click item transaction
        adapter.setOnItemClickListener(new TransactionListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Transaction transaction) {
                Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(Transaction transaction) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm_delete_title)
                        .setMessage(R.string.confirm_delete)
                        .setPositiveButton(R.string.delete, (dialog, which) ->
                                viewModel.deleteTransaction(transaction))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        // ==== BUDGET OBSERVERS & ACTIONS ====
        budgetViewModel.getBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null && !budgets.isEmpty()) {
                budgetAdapter.setBudgets(budgets);
                layoutEmptyBudget.setVisibility(View.GONE);
                llBudgetsContainer.setVisibility(View.VISIBLE);
            } else {
                budgetAdapter.setBudgets(new ArrayList<>());
                layoutEmptyBudget.setVisibility(View.VISIBLE);
                llBudgetsContainer.setVisibility(View.GONE);
            }
        });

        spentObserver = spentList -> {
            Map<Long, Double> spentMap = new HashMap<>();
            if (spentList != null) {
                for (CategorySpent item : spentList)
                    spentMap.put(item.categoryId, item.spent);
            }
            budgetAdapter.setSpentMap(spentMap);
        };

        budgetViewModel.getSelectedMonthYear().observe(getViewLifecycleOwner(), monthYear -> {
            tvCurrentMonth.setText(DateUtils.formatDisplayMonth(DateUtils.getStartOfMonth(monthYear[0], monthYear[1])));
            
            if (spentLiveData != null) {
                spentLiveData.removeObserver(spentObserver);
            }
            
            long start = DateUtils.getStartOfMonth(monthYear[0], monthYear[1]);
            long end = DateUtils.getEndOfMonth(monthYear[0], monthYear[1]);
            spentLiveData = transactionRepository.getSpentPerCategory(budgetViewModel.getUserId(), start, end);
            spentLiveData.observe(getViewLifecycleOwner(), spentObserver);
        });

        btnPrev.setOnClickListener(v -> budgetViewModel.previousMonth());
        btnNext.setOnClickListener(v -> budgetViewModel.nextMonth());
        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Chọn ngày (tối đa 15 ngày)");
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        
        picker.addOnPositiveButtonClickListener(selection -> {
            long start = selection.first;
            long end = selection.second;
            // MaterialDatePicker returns UTC timestamps for midnight. Let's fix time to cover full days if needed,
            // but for simple calculation:
            long days = (end - start) / (1000 * 60 * 60 * 24);
            if (days > 15) {
                Toast.makeText(requireContext(), "Chỉ được chọn tối đa 15 ngày", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.setDateRange(start, end);

            }
        });
        
        picker.show(getChildFragmentManager(), "date_picker");
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
        etAmount.setKeyListener(android.text.method.DigitsKeyListener.getInstance("0123456789.,"));
        etAmount.addTextChangedListener(new com.example.appquanlychitieu.util.NumberTextWatcher(etAmount));
        layout.addView(etAmount);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_budget)
                .setView(layout)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    if (amountStr.isEmpty()) return;
                    amountStr = amountStr.replace(".", "");
                    Category selectedCategory = expenseCategories.get(spinner.getSelectedItemPosition());
                    int[] my = budgetViewModel.getSelectedMonthYear().getValue();
                    if (my != null) {
                        String monthYear = String.format("%04d-%02d", my[0], my[1] + 1);
                        Budget budget = new Budget(selectedCategory.getId(),
                                Double.parseDouble(amountStr), monthYear, budgetViewModel.getUserId());
                        budgetViewModel.insertBudget(budget);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab_add_transaction);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                View fragmentView = getView();
                if (fragmentView != null) {
                    Chip chipIncome = fragmentView.findViewById(R.id.chip_income);
                    Chip chipExpense = fragmentView.findViewById(R.id.chip_expense);
                    if (chipIncome != null && chipIncome.isChecked()) {
                        intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TransactionType.INCOME.name());
                    } else if (chipExpense != null && chipExpense.isChecked()) {
                        intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TransactionType.EXPENSE.name());
                    }
                }
                startActivity(intent);
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab_add_transaction);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), AddEditTransactionActivity.class)));
        }
    }
}
