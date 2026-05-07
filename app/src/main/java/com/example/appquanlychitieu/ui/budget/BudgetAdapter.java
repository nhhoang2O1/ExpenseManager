package com.example.appquanlychitieu.ui.budget;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.Budget;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.ui.transaction.TransactionAdapter;
import com.example.appquanlychitieu.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {
    private List<Budget> budgets = new ArrayList<>();
    private Map<Long, Category> categoryCache;
    private Map<Long, Double> spentMap;

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    public void setCategoryCache(Map<Long, Category> cache) {
        this.categoryCache = cache;
    }

    public void setSpentMap(Map<Long, Double> spentMap) {
        this.spentMap = spentMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        Category category = categoryCache != null ? categoryCache.get(budget.getCategoryId()) : null;
        double spent = spentMap != null && spentMap.containsKey(budget.getCategoryId())
                ? spentMap.get(budget.getCategoryId()) : 0;

        // Category info
        if (category != null) {
            holder.tvCategoryName.setText(category.getName());

            int iconRes = TransactionAdapter.getIconResource(holder.itemView.getContext(), category.getIcon());
            if (iconRes != 0) holder.ivCategoryIcon.setImageResource(iconRes);

            try {
                int color = Color.parseColor(category.getColor());
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(color);
                holder.viewIconBg.setBackground(bg);
            } catch (Exception ignored) {}
        }

        // Budget info
        holder.tvBudgetLabel.setText(String.format("Hạn mức: %s", CurrencyFormatter.format(budget.getAmount())));
        holder.tvSpent.setText(CurrencyFormatter.format(spent));

        double remaining = budget.getAmount() - spent;
        int percentage = budget.getAmount() > 0 ? (int) ((spent / budget.getAmount()) * 100) : 0;

        holder.progressBudget.setProgress(Math.min(percentage, 100));
        holder.tvPercentage.setText(percentage + "%");

        if (remaining >= 0) {
            holder.tvRemaining.setText("Còn lại: " + CurrencyFormatter.format(remaining));
            holder.tvRemaining.setTextColor(holder.itemView.getContext().getColor(R.color.income_color));
            holder.tvPercentage.setTextColor(holder.itemView.getContext().getColor(R.color.primary));
        } else {
            holder.tvRemaining.setText("Vượt: " + CurrencyFormatter.format(Math.abs(remaining)));
            holder.tvRemaining.setTextColor(holder.itemView.getContext().getColor(R.color.expense_color));
            holder.tvPercentage.setTextColor(holder.itemView.getContext().getColor(R.color.expense_color));
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewIconBg;
        ImageView ivCategoryIcon;
        TextView tvCategoryName, tvBudgetLabel, tvSpent, tvRemaining, tvPercentage;
        ProgressBar progressBudget;

        ViewHolder(View itemView) {
            super(itemView);
            viewIconBg = itemView.findViewById(R.id.view_icon_bg);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvBudgetLabel = itemView.findViewById(R.id.tv_budget_label);
            tvSpent = itemView.findViewById(R.id.tv_spent);
            tvRemaining = itemView.findViewById(R.id.tv_remaining);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
            progressBudget = itemView.findViewById(R.id.progress_budget);
        }
    }
}
