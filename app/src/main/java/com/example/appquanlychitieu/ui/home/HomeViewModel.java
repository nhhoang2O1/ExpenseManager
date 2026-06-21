package com.example.appquanlychitieu.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

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

    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>();
    private final LiveData<Double> dailyIncome;
    private final LiveData<Double> dailyExpense;

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

        selectedDate.setValue(System.currentTimeMillis());
        
        dailyIncome = Transformations.switchMap(selectedDate, date -> {
            long start = DateUtils.getStartOfDay(date);
            long end = DateUtils.getEndOfDay(date);
            return repository.getTotalByTypeAndDateRange(userId, TransactionType.INCOME, start, end);
        });
        
        dailyExpense = Transformations.switchMap(selectedDate, date -> {
            long start = DateUtils.getStartOfDay(date);
            long end = DateUtils.getEndOfDay(date);
            return repository.getTotalByTypeAndDateRange(userId, TransactionType.EXPENSE, start, end);
        });
    }

    public LiveData<Double> getTotalIncome() { return totalIncome; }
    public LiveData<Double> getTotalExpense() { return totalExpense; }
    public LiveData<List<Transaction>> getRecentTransactions() { return recentTransactions; }
    public LiveData<Double> getBalance() { return balance; }

    public LiveData<Long> getSelectedDate() { return selectedDate; }
    public LiveData<Double> getDailyIncome() { return dailyIncome; }
    public LiveData<Double> getDailyExpense() { return dailyExpense; }

    public void setSelectedDate(long date) {
        selectedDate.setValue(date);
    }

    public void deleteTransaction(Transaction transaction) {
        repository.delete(transaction);
    }
}
