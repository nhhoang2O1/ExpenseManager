package com.example.appquanlychitieu.ui.transaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.util.DateUtils;
import com.example.appquanlychitieu.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;

public class AddEditTransactionActivity extends AppCompatActivity {
    private TextInputEditText etAmount, etNote, etDate;
    private MaterialButtonToggleGroup toggleType;
    private GridView gvCategories;
    private MaterialButton btnSave;
    private AppDatabase db;

    private TransactionType selectedType = TransactionType.EXPENSE;
    private long selectedDate = System.currentTimeMillis();
    private long selectedCategoryId = -1;
    private long editTransactionId = -1;
    private long userId;
    private CategoryGridViewAdapter categoryAdapter;
    private LiveData<List<Category>> categoryLiveData;
    private Observer<List<Category>> categoryObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_transaction);

        db = AppDatabase.getDatabase(this);
        SessionManager session = new SessionManager(this);
        userId = session.getUserId();

        // Init views
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        etDate = findViewById(R.id.et_date);
        toggleType = findViewById(R.id.toggle_type);
        gvCategories = findViewById(R.id.rv_categories);
        btnSave = findViewById(R.id.btn_save);
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvTitle = findViewById(R.id.tv_title);

        // Ngày mặc định
        etDate.setText(DateUtils.formatDate(selectedDate));

        // Setup GridView với BaseAdapter
        categoryAdapter = new CategoryGridViewAdapter(this);
        gvCategories.setAdapter(categoryAdapter);

        // Xử lý click chọn danh mục
        categoryAdapter.setOnCategoryClickListener((category, position) -> {
            selectedCategoryId = category.getId();
            categoryAdapter.setSelectedPosition(position);
        });

        // Kiểm tra chế độ sửa
        editTransactionId = getIntent().getLongExtra("transaction_id", -1);
        if (editTransactionId != -1) {
            tvTitle.setText(R.string.edit_transaction);
            loadTransaction();
        }

        // Toggle loại giao dịch
        toggleType.check(R.id.btn_expense);
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                selectedType = checkedId == R.id.btn_income ? TransactionType.INCOME : TransactionType.EXPENSE;
                loadCategories();
            }
        });

        etDate.setOnClickListener(v -> showDatePicker());
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());

        // Load danh mục lần đầu
        loadCategories();
    }

    private void loadCategories() {
        // Remove observer cũ nếu có
        if (categoryLiveData != null && categoryObserver != null) {
            categoryLiveData.removeObserver(categoryObserver);
        }

        // Tạo observer mới và observe
        categoryLiveData = db.categoryDao().getCategoriesByType(selectedType);
        categoryObserver = categories -> categoryAdapter.setCategories(categories);
        categoryLiveData.observe(this, categoryObserver);
    }

    private void loadTransaction() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Transaction transaction = db.transactionDao().getTransactionById(editTransactionId);
            if (transaction != null) {
                runOnUiThread(() -> {
                    etAmount.setText(String.valueOf((long) transaction.getAmount()));
                    etNote.setText(transaction.getNote());
                    selectedDate = transaction.getDate();
                    etDate.setText(DateUtils.formatDate(selectedDate));
                    selectedType = transaction.getType();
                    selectedCategoryId = transaction.getCategoryId() != null ? transaction.getCategoryId() : -1;

                    if (selectedType == TransactionType.INCOME) {
                        toggleType.check(R.id.btn_income);
                    } else {
                        toggleType.check(R.id.btn_expense);
                    }
                });
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDate);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            selectedDate = selected.getTimeInMillis();
            etDate.setText(DateUtils.formatDate(selectedDate));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.please_enter_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryId == -1) {
            Toast.makeText(this, R.string.please_select_category, Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (editTransactionId != -1) {
                Transaction transaction = db.transactionDao().getTransactionById(editTransactionId);
                if (transaction != null) {
                    transaction.setAmount(amount);
                    transaction.setNote(note);
                    transaction.setDate(selectedDate);
                    transaction.setCategoryId(selectedCategoryId);
                    transaction.setType(selectedType);
                    db.transactionDao().update(transaction);
                }
            } else {
                Transaction transaction = new Transaction(amount, note, selectedDate,
                        selectedCategoryId, selectedType, userId);
                db.transactionDao().insert(transaction);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.transaction_saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
