package com.example.appquanlychitieu.ui.goals;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.Goal;
import com.example.appquanlychitieu.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class GoalListAdapter extends BaseAdapter {
    private final Context context;
    private List<Goal> goals = new ArrayList<>();
    private OnGoalInteractionListener listener;

    public interface OnGoalInteractionListener {
        void onAddFundsClick(Goal goal);
        void onGoalLongClick(Goal goal);
    }

    public GoalListAdapter(Context context, OnGoalInteractionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return goals.size();
    }

    @Override
    public Goal getItem(int position) {
        return goals.get(position);
    }

    @Override
    public long getItemId(int position) {
        return goals.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tv_goal_name);
            holder.tvCurrentAmount = convertView.findViewById(R.id.tv_current_amount);
            holder.tvTargetAmount = convertView.findViewById(R.id.tv_target_amount);
            holder.tvPercentage = convertView.findViewById(R.id.tv_percentage);
            holder.pbProgress = convertView.findViewById(R.id.pb_goal_progress);
            holder.btnAddFunds = convertView.findViewById(R.id.btn_add_funds);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Goal goal = getItem(position);
        
        holder.tvName.setText(goal.getName());
        holder.tvCurrentAmount.setText(CurrencyFormatter.format(goal.getCurrentAmount()));
        holder.tvTargetAmount.setText(CurrencyFormatter.format(goal.getTargetAmount()));

        int percentage = 0;
        if (goal.getTargetAmount() > 0) {
            percentage = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
            if (percentage > 100) percentage = 100;
        }

        holder.tvPercentage.setText(percentage + "%");
        holder.pbProgress.setProgress(percentage);

        holder.btnAddFunds.setOnClickListener(v -> {
            if (listener != null) listener.onAddFundsClick(goal);
        });

        convertView.setOnLongClickListener(v -> {
            if (listener != null) listener.onGoalLongClick(goal);
            return true;
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvCurrentAmount;
        TextView tvTargetAmount;
        TextView tvPercentage;
        ProgressBar pbProgress;
        ImageButton btnAddFunds;
    }
}
