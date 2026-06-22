package com.example.appquanlychitieu.ui.budget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.Budget;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetListAdapter extends BaseAdapter {

    private final Context context;
    private List<Budget> budgets = new ArrayList<>();
    private Map<Long, Category> categoryCache = new HashMap<>();
    private Map<Long, Double> spentMap = new HashMap<>();

    public BudgetListAdapter(Context context) {
        this.context = context;
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    public void setCategoryCache(Map<Long, Category> cache) {
        this.categoryCache = cache;
        notifyDataSetChanged();
    }

    public void setSpentMap(Map<Long, Double> spentMap) {
        this.spentMap = spentMap;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return budgets.size(); }

    @Override
    public Budget getItem(int position) { return budgets.get(position); }

    @Override
    public long getItemId(int position) { return budgets.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Budget budget = budgets.get(position);
        Category category = categoryCache.get(budget.getCategoryId());
        double spent = spentMap.getOrDefault(budget.getCategoryId(), 0.0);

        if (category != null) {
            holder.tvCategoryName.setText(category.getName());
            int iconRes = getIconResource(category.getIcon());
            if (iconRes != 0) holder.ivCategoryIcon.setImageResource(iconRes);

            try {
                int color = Color.parseColor(category.getColor());
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(color);
                holder.viewIconBg.setBackground(bg);
            } catch (Exception ignored) {}
        }

        holder.tvBudgetLabel.setText("Hạn mức: " + CurrencyFormatter.format(budget.getAmount()));
        holder.tvSpent.setText(CurrencyFormatter.format(spent));

        double remaining = budget.getAmount() - spent;
        int percentage = budget.getAmount() > 0 ? (int) ((spent / budget.getAmount()) * 100) : 0;
        holder.progressBudget.setProgress(Math.min(percentage, 100));
        holder.tvPercentage.setText(percentage + "%");

        if (remaining >= 0) {
            holder.tvRemaining.setText("Còn lại: " + CurrencyFormatter.format(remaining));
            holder.tvRemaining.setTextColor(context.getColor(R.color.income_color));
            holder.tvPercentage.setTextColor(context.getColor(R.color.primary));
        } else {
            holder.tvRemaining.setText("Vượt: " + CurrencyFormatter.format(Math.abs(remaining)));
            holder.tvRemaining.setTextColor(context.getColor(R.color.expense_color));
            holder.tvPercentage.setTextColor(context.getColor(R.color.expense_color));
        }

        return convertView;
    }

    private int getIconResource(String iconName) {
        if (iconName == null) return 0;
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    static class ViewHolder {
        View viewIconBg;
        ImageView ivCategoryIcon;
        TextView tvCategoryName, tvBudgetLabel, tvSpent, tvRemaining, tvPercentage;
        ProgressBar progressBudget;

        ViewHolder(View view) {
            viewIconBg = view.findViewById(R.id.view_icon_bg);
            ivCategoryIcon = view.findViewById(R.id.iv_category_icon);
            tvCategoryName = view.findViewById(R.id.tv_category_name);
            tvBudgetLabel = view.findViewById(R.id.tv_budget_label);
            tvSpent = view.findViewById(R.id.tv_spent);
            tvRemaining = view.findViewById(R.id.tv_remaining);
            tvPercentage = view.findViewById(R.id.tv_percentage);
            progressBudget = view.findViewById(R.id.progress_budget);
        }
    }
}
