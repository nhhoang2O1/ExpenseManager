package com.example.appquanlychitieu.ui.transaction;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionListAdapter extends BaseAdapter {

    private final Context context;
    private List<Transaction> transactions = new ArrayList<>();
    private Map<Long, Category> categoryCache = new HashMap<>();

    public interface OnItemClickListener {
        void onClick(Transaction transaction);
        void onLongClick(Transaction transaction);
    }

    private OnItemClickListener listener;

    public TransactionListAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void setCategoryCache(Map<Long, Category> cache) {
        this.categoryCache = cache;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return transactions.size(); }

    @Override
    public Transaction getItem(int position) { return transactions.get(position); }

    @Override
    public long getItemId(int position) { return transactions.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction transaction = transactions.get(position);
        Category category = categoryCache.get(transaction.getCategoryId());

        String note = transaction.getNote();
        String categoryName = category != null ? category.getName() : "";
        String dateLabel = com.example.appquanlychitieu.util.DateUtils.getRelativeDateLabel(transaction.getDate());

        holder.tvNote.setText(categoryName);
        if (note != null && !note.isEmpty()) {
            holder.tvCategory.setText(note + " • " + dateLabel);
        } else {
            holder.tvCategory.setText(dateLabel);
        }

        boolean isExpense = transaction.getType() == TransactionType.EXPENSE;
        holder.tvAmount.setText(CurrencyFormatter.formatWithSign(transaction.getAmount(), isExpense));
        holder.tvAmount.setTextColor(context.getColor(isExpense ? R.color.expense_color : R.color.income_color));

        if (category != null) {
            int iconResId = getIconResource(category.getIcon());
            if (iconResId != 0) holder.ivCategoryIcon.setImageResource(iconResId);

            try {
                int color = Color.parseColor(category.getColor());
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(color);
                holder.viewIconBg.setBackground(bg);
            } catch (Exception ignored) {}
        }

        final Transaction t = transaction;
        convertView.setOnClickListener(v -> { if (listener != null) listener.onClick(t); });
        convertView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(t);
            return true;
        });

        return convertView;
    }

    private int getIconResource(String iconName) {
        if (iconName == null) return 0;
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    static class ViewHolder {
        View viewIconBg;
        ImageView ivCategoryIcon;
        TextView tvNote, tvCategory, tvAmount;

        ViewHolder(View view) {
            viewIconBg = view.findViewById(R.id.view_icon_bg);
            ivCategoryIcon = view.findViewById(R.id.iv_category_icon);
            tvNote = view.findViewById(R.id.tv_note);
            tvCategory = view.findViewById(R.id.tv_category);
            tvAmount = view.findViewById(R.id.tv_amount);
        }
    }
}
