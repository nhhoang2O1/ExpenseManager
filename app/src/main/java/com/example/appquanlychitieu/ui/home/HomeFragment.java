package com.example.appquanlychitieu.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.ui.transaction.AddEditTransactionActivity;
import com.example.appquanlychitieu.ui.transaction.TransactionAdapter;
import com.example.appquanlychitieu.util.CurrencyFormatter;
import com.example.appquanlychitieu.util.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private TransactionAdapter adapter;
    private TextView tvBalance, tvIncome, tvExpense, tvMonthYear, tvEmpty;
    private RecyclerView rvRecentTransactions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init views
        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        tvEmpty = view.findViewById(R.id.tv_empty);
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        TextView tvSeeAll = view.findViewById(R.id.tv_see_all);

        // Set month
        tvMonthYear.setText(DateUtils.formatDisplayMonth(System.currentTimeMillis()));

        // Setup RecyclerView
        adapter = new TransactionAdapter();
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentTransactions.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe category data for cache
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            Map<Long, Category> cache = new HashMap<>();
            for (Category c : categories) {
                cache.put(c.getId(), c);
            }
            adapter.setCategoryCache(cache);
            adapter.notifyDataSetChanged();
        });

        // Observe data
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            double inc = income != null ? income : 0;
            tvIncome.setText(CurrencyFormatter.format(inc));
            updateBalance();
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            double exp = expense != null ? expense : 0;
            tvExpense.setText(CurrencyFormatter.format(exp));
            updateBalance();
        });

        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setTransactions(transactions);
                rvRecentTransactions.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                rvRecentTransactions.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Click listeners
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
            startActivity(intent);
        });

        tvSeeAll.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.navigation_transactions);
        });

        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onTransactionLongClick(Transaction transaction) {
                // Will handle delete later
            }
        });
    }

    private void updateBalance() {
        Double income = viewModel.getTotalIncome().getValue();
        Double expense = viewModel.getTotalExpense().getValue();
        double inc = income != null ? income : 0;
        double exp = expense != null ? expense : 0;
        tvBalance.setText(CurrencyFormatter.format(inc - exp));
    }
}
