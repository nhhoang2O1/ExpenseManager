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
    public static final String EXTRA_TRANSACTION_TYPE = "transaction_type";

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
            if ("Khác".equalsIgnoreCase(category.getName())) {
                showCustomCategoryDialog();
            } else {
                selectedCategoryId = category.getId();
                categoryAdapter.setSelectedPosition(position);
            }
        });

        // Kiểm tra chế độ sửa
        String requestedType = getIntent().getStringExtra(EXTRA_TRANSACTION_TYPE);
        if (requestedType != null) {
            try {
                selectedType = TransactionType.valueOf(requestedType);
            } catch (IllegalArgumentException ignored) {
                selectedType = TransactionType.EXPENSE;
            }
        }

        editTransactionId = getIntent().getLongExtra("transaction_id", -1);
        if (editTransactionId != -1) {
            tvTitle.setText(R.string.edit_transaction);
            loadTransaction();
        }

        // Toggle loại giao dịch
        toggleType.check(selectedType == TransactionType.INCOME ? R.id.btn_income : R.id.btn_expense);
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
        categoryObserver = categories -> {
            if (categories != null && !categories.isEmpty()) {
                // Sắp xếp các danh mục, đưa các mục phổ biến lên đầu
                java.util.Collections.sort(categories, (c1, c2) -> {
                    int weight1 = getCategoryWeight(c1.getName());
                    int weight2 = getCategoryWeight(c2.getName());
                    if (weight1 != weight2) {
                        return Integer.compare(weight1, weight2);
                    }
                    return c1.getName().compareToIgnoreCase(c2.getName());
                });

                categoryAdapter.setCategories(categories);
                if (selectedCategoryId != -1) {
                    categoryAdapter.setSelectedCategoryId(selectedCategoryId);
                }
            } else {
                // Nếu không có categories, khởi tạo lại
                Toast.makeText(this, "Đang khởi tạo danh mục...", Toast.LENGTH_SHORT).show();
                initializeDefaultCategories();
            }
        };
        categoryLiveData.observe(this, categoryObserver);
    }

    private int getCategoryWeight(String name) {
        if (name == null) return 100;
        switch (name) {
            // Expense
            case "Ăn uống": return 1;
            case "Di chuyển": return 2;
            case "Hóa đơn": return 3;
            case "Mua sắm": return 4;
            case "Nhà ở": return 5;
            case "Sức khỏe": return 6;
            case "Giáo dục": return 7;
            case "Giải trí": return 8;
            
            // Income
            case "Lương": return 1;
            case "Làm thêm": return 2;
            case "Đầu tư": return 3;
            case "Quà tặng": return 4;
            
            case "Khác": return 999;
            default: return 100; // Custom categories 
        }
    }

    private void initializeDefaultCategories() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = db.categoryDao().getCategoryCount();
            if (count == 0) {
                // Thêm categories mặc định
                java.util.List<Category> defaultCategories = new java.util.ArrayList<>();
                
                // Expense categories
                defaultCategories.add(new Category("Ăn uống", "ic_food", "#FF5722", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Di chuyển", "ic_transport", "#2196F3", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Mua sắm", "ic_shopping", "#E91E63", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Nhà ở", "ic_house", "#795548", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Giải trí", "ic_entertainment", "#9C27B0", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Sức khỏe", "ic_health", "#F44336", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Giáo dục", "ic_education", "#3F51B5", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Hóa đơn", "ic_bill", "#FF9800", TransactionType.EXPENSE, true));
                defaultCategories.add(new Category("Khác", "ic_other", "#607D8B", TransactionType.EXPENSE, true));

                // Income categories
                defaultCategories.add(new Category("Lương", "ic_salary", "#4CAF50", TransactionType.INCOME, true));
                defaultCategories.add(new Category("Quà tặng", "ic_gift", "#E91E63", TransactionType.INCOME, true));
                defaultCategories.add(new Category("Đầu tư", "ic_invest", "#00BCD4", TransactionType.INCOME, true));
                defaultCategories.add(new Category("Làm thêm", "ic_freelance", "#8BC34A", TransactionType.INCOME, true));
                defaultCategories.add(new Category("Khác", "ic_other", "#607D8B", TransactionType.INCOME, true));

                db.categoryDao().insertAll(defaultCategories);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã khởi tạo danh mục!", Toast.LENGTH_SHORT).show();
                    loadCategories();
                });
            }
        });
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

    private void showCustomCategoryDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Nhập danh mục khác");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Tên danh mục");
        
        // Thêm margin cho EditText
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.spacing_md);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.spacing_md);
        input.setLayoutParams(params);
        container.addView(input);
        
        builder.setView(container);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newCategoryName = input.getText().toString().trim();
            if (!newCategoryName.isEmpty()) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    boolean exists = false;
                    List<Category> existingCats = db.categoryDao().getCategoriesByTypeSync(selectedType);
                    long existingId = -1;
                    for (Category c : existingCats) {
                        if (c.getName().equalsIgnoreCase(newCategoryName)) {
                            exists = true;
                            existingId = c.getId();
                            break;
                        }
                    }

                    final long newId;
                    if (!exists) {
                        Category newCat = new Category(newCategoryName, "ic_other", "#607D8B", selectedType, false);
                        newId = db.categoryDao().insert(newCat);
                    } else {
                        newId = existingId;
                    }

                    final boolean finalExists = exists;
                    runOnUiThread(() -> {
                        selectedCategoryId = newId;
                        if (finalExists) {
                            categoryAdapter.setSelectedCategoryId(selectedCategoryId);
                        }
                        // Nếu chưa tồn tại, DB sẽ update LiveData và adapter sẽ được render lại
                        // Tại loadCategories, selectedCategoryId sẽ được set lại
                    });
                });
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
