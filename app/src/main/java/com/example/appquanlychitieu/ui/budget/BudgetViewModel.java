package com.example.appquanlychitieu.ui.budget;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.appquanlychitieu.data.model.Budget;
import com.example.appquanlychitieu.data.repository.BudgetRepository;
import com.example.appquanlychitieu.util.SessionManager;

import java.util.Calendar;
import java.util.List;

public class BudgetViewModel extends AndroidViewModel {
    private final BudgetRepository repository;
    private final long userId;
    private final MutableLiveData<int[]> selectedMonthYear = new MutableLiveData<>();
    private final LiveData<List<Budget>> budgets;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new BudgetRepository(application);
        SessionManager session = new SessionManager(application);
        userId = session.getUserId();

        Calendar cal = Calendar.getInstance();
        selectedMonthYear.setValue(new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)});

        budgets = Transformations.switchMap(selectedMonthYear, monthYear -> {
            String key = String.format("%04d-%02d", monthYear[0], monthYear[1] + 1);
            return repository.getBudgetsByMonth(userId, key);
        });
    }

    public long getUserId() { return userId; }
    public LiveData<List<Budget>> getBudgets() { return budgets; }
    public MutableLiveData<int[]> getSelectedMonthYear() { return selectedMonthYear; }

    public void previousMonth() {
        int[] current = selectedMonthYear.getValue();
        if (current != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(current[0], current[1], 1);
            cal.add(Calendar.MONTH, -1);
            selectedMonthYear.setValue(new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)});
        }
    }

    public void nextMonth() {
        int[] current = selectedMonthYear.getValue();
        if (current != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(current[0], current[1], 1);
            cal.add(Calendar.MONTH, 1);
            selectedMonthYear.setValue(new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)});
        }
    }

    public void insertBudget(Budget budget) {
        repository.insert(budget);
    }

    public void deleteBudget(Budget budget) {
        repository.delete(budget);
    }
}
