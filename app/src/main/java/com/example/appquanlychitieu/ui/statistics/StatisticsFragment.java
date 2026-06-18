package com.example.appquanlychitieu.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.CategorySummary;
import com.example.appquanlychitieu.data.model.MonthlySummary;
import com.example.appquanlychitieu.util.CurrencyFormatter;
import com.example.appquanlychitieu.util.DateUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private StatisticsViewModel viewModel;
    private PieChart pieChart;
    private TextView tvCurrentMonth, tvEmpty, tvHistoryEmpty;
    private android.widget.LinearLayout layoutCategorySummary, layoutMonthlyHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pieChart = view.findViewById(R.id.pie_chart);
        tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvHistoryEmpty = view.findViewById(R.id.tv_history_empty);
        layoutCategorySummary = view.findViewById(R.id.layout_category_summary);
        layoutMonthlyHistory = view.findViewById(R.id.layout_monthly_history);
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_month);
        ImageButton btnNext = view.findViewById(R.id.btn_next_month);

        setupPieChart();

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // Observe tháng
        viewModel.getSelectedMonthYear().observe(getViewLifecycleOwner(), monthYear -> {
            Calendar cal = Calendar.getInstance();
            cal.set(monthYear[0], monthYear[1], 1);
            tvCurrentMonth.setText(DateUtils.formatDisplayMonth(cal.getTimeInMillis()));
        });

        // Observe thống kê danh mục
        viewModel.getCategorySummary().observe(getViewLifecycleOwner(), summaries -> {
            if (summaries != null && !summaries.isEmpty()) {
                updatePieChart(summaries);
                
                CategorySummaryAdapter adapter = new CategorySummaryAdapter(summaries);
                layoutCategorySummary.removeAllViews();
                for (int i = 0; i < adapter.getCount(); i++) {
                    View itemView = adapter.getView(i, null, layoutCategorySummary);
                    layoutCategorySummary.addView(itemView);
                }
                
                pieChart.setVisibility(View.VISIBLE);
                layoutCategorySummary.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                pieChart.setVisibility(View.GONE);
                layoutCategorySummary.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Observe lịch sử tài chính
        viewModel.getMonthlySummary().observe(getViewLifecycleOwner(), summaries -> {
            if (summaries != null && !summaries.isEmpty()) {
                MonthlySummaryAdapter adapter = new MonthlySummaryAdapter(summaries);
                layoutMonthlyHistory.removeAllViews();
                for (int i = 0; i < adapter.getCount(); i++) {
                    View itemView = adapter.getView(i, null, layoutMonthlyHistory);
                    layoutMonthlyHistory.addView(itemView);
                }
                
                layoutMonthlyHistory.setVisibility(View.VISIBLE);
                tvHistoryEmpty.setVisibility(View.GONE);
            } else {
                layoutMonthlyHistory.setVisibility(View.GONE);
                tvHistoryEmpty.setVisibility(View.VISIBLE);
            }
        });

        btnPrev.setOnClickListener(v -> viewModel.previousMonth());
        btnNext.setOnClickListener(v -> viewModel.nextMonth());
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.animateY(800);
    }

    private void updatePieChart(List<CategorySummary> summaries) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (CategorySummary s : summaries) {
            entries.add(new PieEntry((float) s.getTotalAmount(), s.getCategoryName()));
            try { colors.add(Color.parseColor(s.getCategoryColor())); }
            catch (Exception e) { colors.add(Color.GRAY); }
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }

    // BaseAdapter cho ListView thống kê danh mục
    class CategorySummaryAdapter extends BaseAdapter {
        private final List<CategorySummary> summaries;

        CategorySummaryAdapter(List<CategorySummary> summaries) { this.summaries = summaries; }

        @Override public int getCount() { return summaries.size(); }
        @Override public CategorySummary getItem(int pos) { return summaries.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(requireContext()).inflate(R.layout.item_transaction, parent, false);

            CategorySummary s = summaries.get(position);
            TextView tvNote = convertView.findViewById(R.id.tv_note);
            TextView tvCategory = convertView.findViewById(R.id.tv_category);
            TextView tvAmount = convertView.findViewById(R.id.tv_amount);
            View viewIconBg = convertView.findViewById(R.id.view_icon_bg);
            android.widget.ImageView ivIcon = convertView.findViewById(R.id.iv_category_icon);

            tvNote.setText(s.getCategoryName());
            tvCategory.setText(String.format("%d giao dịch", s.getTransactionCount()));
            tvAmount.setText(CurrencyFormatter.format(s.getTotalAmount()));
            tvAmount.setTextColor(requireContext().getColor(R.color.expense_color));

            try {
                int color = Color.parseColor(s.getCategoryColor());
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                bg.setColor(color);
                viewIconBg.setBackground(bg);
            } catch (Exception ignored) {}

            int iconRes = getIconResource(s.getCategoryIcon());
            if (iconRes != 0) ivIcon.setImageResource(iconRes);

            return convertView;
        }
    }

    private int getIconResource(String iconName) {
        if (iconName == null) return 0;
        return requireContext().getResources().getIdentifier(iconName, "drawable", requireContext().getPackageName());
    }

    // BaseAdapter cho ListView lịch sử tài chính
    class MonthlySummaryAdapter extends BaseAdapter {
        private final List<MonthlySummary> summaries;

        MonthlySummaryAdapter(List<MonthlySummary> summaries) { this.summaries = summaries; }

        @Override public int getCount() { return summaries.size(); }
        @Override public MonthlySummary getItem(int pos) { return summaries.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(requireContext()).inflate(R.layout.item_monthly_summary, parent, false);

            MonthlySummary s = summaries.get(position);
            TextView tvMonthYear = convertView.findViewById(R.id.tv_month_year);
            TextView tvIncome = convertView.findViewById(R.id.tv_income);
            TextView tvExpense = convertView.findViewById(R.id.tv_expense);
            TextView tvBalance = convertView.findViewById(R.id.tv_balance);

            try {
                String[] parts = s.getMonthYear().split("-");
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, 1);
                tvMonthYear.setText(DateUtils.formatDisplayMonth(cal.getTimeInMillis()));
            } catch (Exception e) { tvMonthYear.setText(s.getMonthYear()); }

            tvIncome.setText("+ " + CurrencyFormatter.format(s.getTotalIncome()));
            tvExpense.setText("- " + CurrencyFormatter.format(s.getTotalExpense()));

            double balance = s.getBalance();
            tvBalance.setText(CurrencyFormatter.format(balance));
            tvBalance.setTextColor(requireContext().getColor(
                    balance >= 0 ? R.color.income_color : R.color.expense_color));

            return convertView;
        }
    }
}
