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
import com.example.appquanlychitieu.ui.transaction.AddEditTransactionActivity;
import com.example.appquanlychitieu.ui.transaction.TransactionListAdapter;
import com.example.appquanlychitieu.util.CurrencyFormatter;
import com.example.appquanlychitieu.util.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private TransactionListAdapter adapter;
    private TextView tvBalance, tvIncome, tvExpense, tvMonthYear, tvEmpty;
    private ListView lvRecentTransactions;

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
        tvEmpty = view.findViewById(R.id.tv_empty);
        lvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        TextView tvSeeAll = view.findViewById(R.id.tv_see_all);

        tvMonthYear.setText(DateUtils.formatDisplayMonth(System.currentTimeMillis()));

        // Setup ListView với BaseAdapter
        adapter = new TransactionListAdapter(requireContext());
        lvRecentTransactions.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe category cache
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            Map<Long, Category> cache = new HashMap<>();
            for (Category c : categories) cache.put(c.getId(), c);
            adapter.setCategoryCache(cache);
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
        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setTransactions(transactions);
                lvRecentTransactions.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                lvRecentTransactions.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Click item trong ListView
        adapter.setOnItemClickListener(new TransactionListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Transaction transaction) {
                Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }
            @Override
            public void onLongClick(Transaction transaction) { }
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditTransactionActivity.class)));

        tvSeeAll.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.navigation_transactions));
    }
}
