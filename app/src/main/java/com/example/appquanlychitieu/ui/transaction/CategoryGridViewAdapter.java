package com.example.appquanlychitieu.ui.transaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * BaseAdapter cho GridView hiển thị danh mục.
 */
public class CategoryGridViewAdapter extends BaseAdapter {

    private final Context context;
    private List<Category> categories = new ArrayList<>();
    private int selectedPosition = -1;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    private OnCategoryClickListener listener;

    public CategoryGridViewAdapter(Context context) {
        this.context = context;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return categories.size(); }

    @Override
    public Category getItem(int position) { return categories.get(position); }

    @Override
    public long getItemId(int position) { return categories.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_grid, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category category = categories.get(position);
        holder.tvName.setText(category.getName());

        // Hiệu ứng chọn
        if (position == selectedPosition) {
            convertView.setBackgroundResource(R.drawable.bg_category_choice_selected);
            holder.tvName.setTextColor(context.getColor(R.color.on_primary));
        } else {
            convertView.setBackgroundResource(R.drawable.bg_category_choice);
            holder.tvName.setTextColor(context.getColor(R.color.text_primary));
        }

        // Click
        final int pos = position;
        final Category cat = category;
        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(cat, pos);
        });

        return convertView;
    }

    static class ViewHolder {
        final TextView tvName;

        ViewHolder(View view) {
            tvName = view.findViewById(R.id.tv_name);
        }
    }
}
