package com.example.appquanlychitieu.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.data.repository.TransactionRepository;
import com.example.appquanlychitieu.util.DateUtils;
import com.example.appquanlychitieu.util.SessionManager;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final TransactionRepository repository;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final LiveData<List<Transaction>> recentTransactions;
    private final MediatorLiveData<Double> balance = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        SessionManager session = new SessionManager(application);
        long userId = session.getUserId();

        long startOfMonth = DateUtils.getStartOfCurrentMonth();
        long endOfMonth = DateUtils.getEndOfCurrentMonth();

        totalIncome = repository.getTotalByTypeAndDateRange(userId, TransactionType.INCOME, startOfMonth, endOfMonth);
        totalExpense = repository.getTotalByTypeAndDateRange(userId, TransactionType.EXPENSE, startOfMonth, endOfMonth);
        recentTransactions = repository.getRecentTransactions(userId, startOfMonth, endOfMonth, 5);

        balance.addSource(totalIncome, income -> {
            double inc = income != null ? income : 0;
            Double expense = totalExpense.getValue();
            double exp = expense != null ? expense : 0;
            balance.setValue(inc - exp);
        });
        balance.addSource(totalExpense, expense -> {
            Double income = totalIncome.getValue();
            double inc = income != null ? income : 0;
            double exp = expense != null ? expense : 0;
            balance.setValue(inc - exp);
        });
    }

    public LiveData<Double> getTotalIncome() { return totalIncome; }
    public LiveData<Double> getTotalExpense() { return totalExpense; }
    public LiveData<List<Transaction>> getRecentTransactions() { return recentTransactions; }
    public LiveData<Double> getBalance() { return balance; }
}
