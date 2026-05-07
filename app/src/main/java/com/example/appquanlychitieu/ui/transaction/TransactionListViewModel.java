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
    private final MutableLiveData<String> filterType = new MutableLiveData<>("ALL");
    private final LiveData<List<Transaction>> transactions;

    public TransactionListViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        SessionManager session = new SessionManager(application);
        userId = session.getUserId();

        transactions = Transformations.switchMap(filterType, type -> {
            switch (type) {
                case "EXPENSE":
                    return repository.getTransactionsByType(userId, TransactionType.EXPENSE);
                case "INCOME":
                    return repository.getTransactionsByType(userId, TransactionType.INCOME);
                default:
                    return repository.getAllTransactions(userId);
            }
        });
    }

    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    public void setFilter(String type) { filterType.setValue(type); }

    public void deleteTransaction(Transaction transaction) {
        repository.delete(transaction);
    }
}
