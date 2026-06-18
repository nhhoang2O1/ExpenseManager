package com.example.appquanlychitieu.ui.transaction;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.data.repository.TransactionRepository;
import com.example.appquanlychitieu.util.SessionManager;

import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {
    private final TransactionRepository repository;
    private final long userId;
    
    public static class FilterOptions {
        public String type;
        public long startDate;
        public long endDate;

        public FilterOptions(String type, long startDate, long endDate) {
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    private final MutableLiveData<FilterOptions> filterOptions = new MutableLiveData<>();
    private final LiveData<List<Transaction>> transactions;

    public TransactionListViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        SessionManager session = new SessionManager(application);
        userId = session.getUserId();

        // Default: ALL, no date filter (0 to MAX)
        filterOptions.setValue(new FilterOptions("ALL", 0L, Long.MAX_VALUE));

        transactions = Transformations.switchMap(filterOptions, options -> {
            if (options.startDate == 0L && options.endDate == Long.MAX_VALUE) {
                // No date filter
                switch (options.type) {
                    case "EXPENSE": return repository.getTransactionsByType(userId, TransactionType.EXPENSE);
                    case "INCOME": return repository.getTransactionsByType(userId, TransactionType.INCOME);
                    default: return repository.getAllTransactions(userId);
                }
            } else {
                // Date filter active
                switch (options.type) {
                    case "EXPENSE":
                        return repository.getTransactionsByTypeAndDateRange(userId, TransactionType.EXPENSE, options.startDate, options.endDate);
                    case "INCOME":
                        return repository.getTransactionsByTypeAndDateRange(userId, TransactionType.INCOME, options.startDate, options.endDate);
                    default:
                        return repository.getTransactionsByDateRange(userId, options.startDate, options.endDate);
                }
            }
        });
    }

    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    
    public void setFilterType(String type) {
        FilterOptions current = filterOptions.getValue();
        if (current != null) {
            filterOptions.setValue(new FilterOptions(type, current.startDate, current.endDate));
        }
    }

    public void setDateRange(long start, long end) {
        FilterOptions current = filterOptions.getValue();
        if (current != null) {
            filterOptions.setValue(new FilterOptions(current.type, start, end));
        }
    }

    public void deleteTransaction(Transaction transaction) {
        repository.delete(transaction);
    }
}
