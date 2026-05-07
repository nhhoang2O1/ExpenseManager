package com.example.appquanlychitieu.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {

    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String message);
    }

    public static void exportTransactions(Context context, List<Transaction> transactions, ExportCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String fileName = "chi_tieu_" + DateUtils.getCurrentMonthYear() + ".csv";
                File file = new File(downloadsDir, fileName);

                FileWriter writer = new FileWriter(file);
                // BOM for Excel Vietnamese support
                writer.write('\ufeff');
                writer.write("Ngày,Loại,Danh mục,Số tiền,Ghi chú\n");

                for (Transaction t : transactions) {
                    Category category = db.categoryDao().getCategoryById(t.getCategoryId());
                    String categoryName = category != null ? category.getName() : "Không xác định";
                    String type = t.getType().name().equals("INCOME") ? "Thu nhập" : "Chi tiêu";
                    String date = DateUtils.formatDate(t.getDate());
                    String note = t.getNote() != null ? t.getNote().replace(",", ";") : "";

                    writer.write(String.format("%s,%s,%s,%.0f,%s\n",
                            date, type, categoryName, t.getAmount(), note));
                }

                writer.flush();
                writer.close();

                if (callback != null) {
                    callback.onSuccess(file.getAbsolutePath());
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
}
