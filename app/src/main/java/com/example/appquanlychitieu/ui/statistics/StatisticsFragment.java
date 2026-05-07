package com.example.appquanlychitieu.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.CategorySummary;
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
    private TextView tvCurrentMonth, tvEmpty;
    private RecyclerView rvCategorySummary;

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
        rvCategorySummary = view.findViewById(R.id.rv_category_summary);
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_month);
        ImageButton btnNext = view.findViewById(R.id.btn_next_month);

        setupPieChart();

        rvCategorySummary.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // Observe month changes
        viewModel.getSelectedMonthYear().observe(getViewLifecycleOwner(), monthYear -> {
            Calendar cal = Calendar.getInstance();
            cal.set(monthYear[0], monthYear[1], 1);
            tvCurrentMonth.setText(DateUtils.formatDisplayMonth(cal.getTimeInMillis()));
        });

        // Observe category summary
        viewModel.getCategorySummary().observe(getViewLifecycleOwner(), summaries -> {
            if (summaries != null && !summaries.isEmpty()) {
                updatePieChart(summaries);
                updateCategoryList(summaries);
                pieChart.setVisibility(View.VISIBLE);
                rvCategorySummary.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                pieChart.setVisibility(View.GONE);
                rvCategorySummary.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
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
            try {
                colors.add(Color.parseColor(s.getCategoryColor()));
            } catch (Exception e) {
                colors.add(Color.GRAY);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updateCategoryList(List<CategorySummary> summaries) {
        double total = 0;
        for (CategorySummary s : summaries) total += s.getTotalAmount();

        CategorySummaryAdapter adapter = new CategorySummaryAdapter(summaries, total);
        rvCategorySummary.setAdapter(adapter);
    }

    // Inner adapter for category summary list
    static class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder> {
        private final List<CategorySummary> summaries;
        private final double total;

        CategorySummaryAdapter(List<CategorySummary> summaries, double total) {
            this.summaries = summaries;
            this.total = total;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategorySummary summary = summaries.get(position);
            holder.tvNote.setText(summary.getCategoryName());
            holder.tvCategory.setText(String.format("%d giao dịch", summary.getTransactionCount()));
            holder.tvAmount.setText(CurrencyFormatter.format(summary.getTotalAmount()));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.expense_color));

            // Category icon color
            try {
                int color = Color.parseColor(summary.getCategoryColor());
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                bg.setColor(color);
                holder.viewIconBg.setBackground(bg);
            } catch (Exception ignored) {}

            // Icon
            int iconRes = com.example.appquanlychitieu.ui.transaction.TransactionAdapter.getIconResource(
                    holder.itemView.getContext(), summary.getCategoryIcon());
            if (iconRes != 0) holder.ivIcon.setImageResource(iconRes);
        }

        @Override
        public int getItemCount() { return summaries.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View viewIconBg;
            android.widget.ImageView ivIcon;
            TextView tvNote, tvCategory, tvAmount;

            ViewHolder(View itemView) {
                super(itemView);
                viewIconBg = itemView.findViewById(R.id.view_icon_bg);
                ivIcon = itemView.findViewById(R.id.iv_category_icon);
                tvNote = itemView.findViewById(R.id.tv_note);
                tvCategory = itemView.findViewById(R.id.tv_category);
                tvAmount = itemView.findViewById(R.id.tv_amount);
            }
        }
    }
}
