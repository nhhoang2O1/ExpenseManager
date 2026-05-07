package com.example.appquanlychitieu.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.appquanlychitieu.data.model.CategorySummary;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.data.repository.TransactionRepository;
import com.example.appquanlychitieu.util.DateUtils;
import com.example.appquanlychitieu.util.SessionManager;

import java.util.Calendar;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {
    private final TransactionRepository repository;
    private final long userId;
    private final MutableLiveData<int[]> selectedMonthYear = new MutableLiveData<>();
    private final LiveData<List<CategorySummary>> categorySummary;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        SessionManager session = new SessionManager(application);
        userId = session.getUserId();

        Calendar cal = Calendar.getInstance();
        selectedMonthYear.setValue(new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)});

        categorySummary = Transformations.switchMap(selectedMonthYear, monthYear -> {
            long start = DateUtils.getStartOfMonth(monthYear[0], monthYear[1]);
            long end = DateUtils.getEndOfMonth(monthYear[0], monthYear[1]);
            return repository.getCategorySummary(userId, TransactionType.EXPENSE, start, end);
        });
    }

    public LiveData<List<CategorySummary>> getCategorySummary() { return categorySummary; }
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
}
