package com.example.appquanlychitieu.ui.transaction;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactions = new ArrayList<>();
    private Map<Long, Category> categoryCache = new HashMap<>();
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void setCategoryCache(Map<Long, Category> cache) {
        this.categoryCache = cache;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        Category category = categoryCache.get(transaction.getCategoryId());

        // Note
        String note = transaction.getNote();
        holder.tvNote.setText(note != null && !note.isEmpty() ? note : (category != null ? category.getName() : ""));

        // Category name
        holder.tvCategory.setText(category != null ? category.getName() : "");

        // Amount
        boolean isExpense = transaction.getType() == TransactionType.EXPENSE;
        holder.tvAmount.setText(CurrencyFormatter.formatWithSign(transaction.getAmount(), isExpense));
        holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(
                isExpense ? R.color.expense_color : R.color.income_color));

        // Category icon & color
        if (category != null) {
            Context ctx = holder.itemView.getContext();
            int iconResId = getIconResource(ctx, category.getIcon());
            if (iconResId != 0) {
                holder.ivCategoryIcon.setImageResource(iconResId);
            }

            try {
                int color = Color.parseColor(category.getColor());
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(color);
                holder.viewIconBg.setBackground(bg);
            } catch (Exception ignored) {}
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(transaction);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onTransactionLongClick(transaction);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static int getIconResource(Context context, String iconName) {
        if (iconName == null) return 0;
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewIconBg;
        ImageView ivCategoryIcon;
        TextView tvNote, tvCategory, tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIconBg = itemView.findViewById(R.id.view_icon_bg);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
