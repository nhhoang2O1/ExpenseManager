package com.example.appquanlychitieu.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.ui.transaction.AddEditTransactionActivity;
import com.example.appquanlychitieu.ui.transaction.TransactionListAdapter;
import com.example.appquanlychitieu.util.CurrencyFormatter;
import com.example.appquanlychitieu.util.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private TransactionListAdapter adapter;
    private TextView tvBalance, tvIncome, tvExpense, tvMonthYear;
    private android.widget.LinearLayout layoutRecentTransactions;
    private View layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        android.widget.Button btnEmptyCta = view.findViewById(R.id.btn_empty_cta);
        layoutRecentTransactions = view.findViewById(R.id.layout_recent_transactions);
        MaterialButton btnQuickIncome = view.findViewById(R.id.btn_quick_income);
        MaterialButton btnQuickExpense = view.findViewById(R.id.btn_quick_expense);
        MaterialButton btnQuickReminder = view.findViewById(R.id.btn_quick_reminder);
        TextView tvSeeAll = view.findViewById(R.id.tv_see_all);

        String currentDateLabel = "Hôm nay, " + DateUtils.formatDate(System.currentTimeMillis());
        tvMonthYear.setText(currentDateLabel);

        ((TextView) view.findViewById(R.id.tv_empty_title)).setText(R.string.empty_home_title);
        ((TextView) view.findViewById(R.id.tv_empty_desc)).setText(R.string.empty_home_desc);
        btnEmptyCta.setText(R.string.empty_home_cta);

        // Setup adapter
        adapter = new TransactionListAdapter(requireContext());

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe category cache
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            Map<Long, Category> cache = new HashMap<>();
            for (Category c : categories) cache.put(c.getId(), c);
            adapter.setCategoryCache(cache);
            updateRecentTransactionsList(viewModel.getRecentTransactions().getValue());
        });

        // Observe thu nhập
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            tvIncome.setText(CurrencyFormatter.format(income != null ? income : 0));
        });

        // Observe chi tiêu
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            tvExpense.setText(CurrencyFormatter.format(expense != null ? expense : 0));
        });

        // Observe số dư
        viewModel.getBalance().observe(getViewLifecycleOwner(), bal -> {
            tvBalance.setText(CurrencyFormatter.format(bal != null ? bal : 0));
        });

        // Observe giao dịch gần nhất
        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), this::updateRecentTransactionsList);

        // Daily Statistics UI Setup
        androidx.cardview.widget.CardView cardDailyStats = view.findViewById(R.id.card_daily_stats);
        TextView tvDailyDate = view.findViewById(R.id.tv_daily_date);
        TextView tvDailyIncome = view.findViewById(R.id.tv_daily_income);
        TextView tvDailyExpense = view.findViewById(R.id.tv_daily_expense);

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                tvDailyDate.setText("Ngày " + DateUtils.formatDate(date));
            }
        });

        viewModel.getDailyIncome().observe(getViewLifecycleOwner(), income -> {
            tvDailyIncome.setText("+ " + CurrencyFormatter.format(income != null ? income : 0));
        });

        viewModel.getDailyExpense().observe(getViewLifecycleOwner(), expense -> {
            tvDailyExpense.setText("- " + CurrencyFormatter.format(expense != null ? expense : 0));
        });

        cardDailyStats.setOnClickListener(v -> {
            com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker =
                    com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Chọn ngày thống kê")
                            .setSelection(viewModel.getSelectedDate().getValue())
                            .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                viewModel.setSelectedDate(selection);
            });

            datePicker.show(getParentFragmentManager(), "DAILY_STATS_DATE_PICKER");
        });

        btnQuickIncome.setOnClickListener(v -> openAddTransaction(TransactionType.INCOME));
        btnQuickExpense.setOnClickListener(v -> openAddTransaction(TransactionType.EXPENSE));
        btnQuickReminder.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.appquanlychitieu.ui.reminder.ReminderActivity.class);
            startActivity(intent);
        });
        btnEmptyCta.setOnClickListener(v -> openAddTransaction(TransactionType.EXPENSE));

        tvSeeAll.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_transactions);
            }
        });
    }

    private void updateRecentTransactionsList(List<Transaction> transactions) {
        if (layoutRecentTransactions == null) return;
        adapter.setTransactions(transactions != null ? transactions : new java.util.ArrayList<>());
        layoutRecentTransactions.removeAllViews();
        if (transactions != null && !transactions.isEmpty()) {
            for (int i = 0; i < transactions.size(); i++) {
                View itemView = adapter.getView(i, null, layoutRecentTransactions);
                final Transaction transaction = transactions.get(i);
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                    intent.putExtra("transaction_id", transaction.getId());
                    startActivity(intent);
                });
                itemView.setOnLongClickListener(v -> {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle(R.string.confirm_delete_title)
                            .setMessage(R.string.confirm_delete)
                            .setPositiveButton(R.string.delete, (dialog, which) ->
                                    viewModel.deleteTransaction(transaction))
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                    return true;
                });
                layoutRecentTransactions.addView(itemView);
            }
            layoutRecentTransactions.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        } else {
            layoutRecentTransactions.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void openAddTransaction(TransactionType type) {
        Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
        intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, type.name());
        startActivity(intent);
    }
}
