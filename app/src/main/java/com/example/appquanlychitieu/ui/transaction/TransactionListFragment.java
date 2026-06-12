package com.example.appquanlychitieu.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

public class TransactionListFragment extends Fragment {
    private TransactionListViewModel viewModel;
    private TransactionListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView lvTransactions = view.findViewById(R.id.rv_transactions);
        TextView tvEmpty = view.findViewById(R.id.tv_empty);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        Chip chipAll = view.findViewById(R.id.chip_all);
        Chip chipExpense = view.findViewById(R.id.chip_expense);
        Chip chipIncome = view.findViewById(R.id.chip_income);

        // Setup ListView với BaseAdapter
        adapter = new TransactionListAdapter(requireContext());
        lvTransactions.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TransactionListViewModel.class);

        // Category cache
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            Map<Long, Category> cache = new HashMap<>();
            for (Category c : categories) cache.put(c.getId(), c);
            adapter.setCategoryCache(cache);
        });

        // Observe danh sách giao dịch
        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setTransactions(transactions);
                lvTransactions.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                lvTransactions.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Filter chips - check isChecked để tránh trigger thừa
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter("ALL");
        });
        chipExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter("EXPENSE");
        });
        chipIncome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter("INCOME");
        });

        // FAB thêm giao dịch
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditTransactionActivity.class)));

        // Click item
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
    }
}
