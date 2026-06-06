package com.example.appquanlychitieu.ui.transaction;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView rvCategories;
    private MaterialButton btnSave;
    private AppDatabase db;

    private TransactionType selectedType = TransactionType.EXPENSE;
    private long selectedDate = System.currentTimeMillis();
    private long selectedCategoryId = -1;
    private long editTransactionId = -1;
    private long userId;
    private CategoryGridAdapter categoryAdapter;

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
        rvCategories = findViewById(R.id.rv_categories);
        btnSave = findViewById(R.id.btn_save);
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvTitle = findViewById(R.id.tv_title);

        // Set default date
        etDate.setText(DateUtils.formatDate(selectedDate));

        // Setup category grid
        categoryAdapter = new CategoryGridAdapter();
        rvCategories.setLayoutManager(new GridLayoutManager(this, 4));
        rvCategories.setAdapter(categoryAdapter);

        // Check for edit mode
        editTransactionId = getIntent().getLongExtra("transaction_id", -1);
        if (editTransactionId != -1) {
            tvTitle.setText(R.string.edit_transaction);
            loadTransaction();
        }

        // Type toggle
        toggleType.check(R.id.btn_expense);
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                selectedType = checkedId == R.id.btn_income ? TransactionType.INCOME : TransactionType.EXPENSE;
                loadCategories();
            }
        });

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> saveTransaction());

        // Load categories
        loadCategories();
    }

    private void loadCategories() {
        db.categoryDao().getCategoriesByType(selectedType).observe(this, categories -> {
            categoryAdapter.setCategories(categories);
            categoryAdapter.setOnCategoryClickListener((category, position) -> {
                selectedCategoryId = category.getId();
                categoryAdapter.setSelectedPosition(position);
            });
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
                    selectedCategoryId = transaction.getCategoryId();

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

        // Không cho phép nhập số âm hoặc bằng 0
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
                Transaction transaction = new Transaction(amount, note, selectedDate, selectedCategoryId, selectedType, userId);
                db.transactionDao().insert(transaction);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.transaction_saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    // Inner adapter for category grid
    static class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.ViewHolder> {
        private List<Category> categories;
        private int selectedPosition = -1;
        private OnCategoryClickListener listener;

        interface OnCategoryClickListener {
            void onCategoryClick(Category category, int position);
        }

        void setOnCategoryClickListener(OnCategoryClickListener listener) {
            this.listener = listener;
        }

        void setCategories(List<Category> categories) {
            this.categories = categories;
            selectedPosition = -1;
            notifyDataSetChanged();
        }

        void setSelectedPosition(int position) {
            int old = selectedPosition;
            selectedPosition = position;
            if (old != -1) notifyItemChanged(old);
            notifyItemChanged(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_grid, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.tvName.setText(category.getName());

            // Icon
            int iconRes = TransactionAdapter.getIconResource(holder.itemView.getContext(), category.getIcon());
            if (iconRes != 0) {
                holder.ivIcon.setImageResource(iconRes);
            }

            // Background color
            try {
                int color = Color.parseColor(category.getColor());
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(color);
                holder.viewIconBg.setBackground(bg);
            } catch (Exception ignored) {}

            // Selection indicator
            if (position == selectedPosition) {
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setScaleX(1.1f);
                holder.itemView.setScaleY(1.1f);
            } else {
                holder.itemView.setAlpha(0.7f);
                holder.itemView.setScaleX(1.0f);
                holder.itemView.setScaleY(1.0f);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCategoryClick(category, position);
            });
        }

        @Override
        public int getItemCount() {
            return categories != null ? categories.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View viewIconBg;
            ImageView ivIcon;
            TextView tvName;

            ViewHolder(View itemView) {
                super(itemView);
                viewIconBg = itemView.findViewById(R.id.view_icon_bg);
                ivIcon = itemView.findViewById(R.id.iv_icon);
                tvName = itemView.findViewById(R.id.tv_name);
            }
        }
    }
}
