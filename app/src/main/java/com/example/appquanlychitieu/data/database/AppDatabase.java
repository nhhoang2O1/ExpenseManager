package com.example.appquanlychitieu.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.appquanlychitieu.data.database.dao.BudgetDao;
import com.example.appquanlychitieu.data.database.dao.CategoryDao;
import com.example.appquanlychitieu.data.database.dao.TransactionDao;
import com.example.appquanlychitieu.data.database.dao.UserDao;
import com.example.appquanlychitieu.data.model.Budget;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.appquanlychitieu.data.database.dao.GoalDao;
import com.example.appquanlychitieu.data.database.dao.ReminderDao;
import com.example.appquanlychitieu.data.model.Goal;
import com.example.appquanlychitieu.data.model.Reminder;

import androidx.room.migration.Migration;
import com.example.appquanlychitieu.data.database.dao.GoalHistoryDao;
import com.example.appquanlychitieu.data.model.GoalHistory;

@Database(entities = {Transaction.class, Category.class, Budget.class, User.class, Goal.class, Reminder.class, GoalHistory.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetDao budgetDao();
    public abstract UserDao userDao();
    public abstract GoalDao goalDao();
    public abstract ReminderDao reminderDao();
    public abstract GoalHistoryDao goalHistoryDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `goal_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `amountAdded` REAL NOT NULL, `date` INTEGER NOT NULL, FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_goal_history_goalId` ON `goal_history` (`goalId`)");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "expense_manager_db")
                            .addMigrations(MIGRATION_5_6)
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Prepopulate default categories
                                    databaseWriteExecutor.execute(() -> {
                                        CategoryDao dao = INSTANCE.categoryDao();
                                        if (dao.getCategoryCount() == 0) {
                                            dao.insertAll(getDefaultCategories());
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static List<Category> getDefaultCategories() {
        List<Category> categories = new ArrayList<>();

        // Expense categories
        categories.add(new Category("Ăn uống", "ic_food", "#FF5722", TransactionType.EXPENSE, true));
        categories.add(new Category("Di chuyển", "ic_transport", "#2196F3", TransactionType.EXPENSE, true));
        categories.add(new Category("Mua sắm", "ic_shopping", "#E91E63", TransactionType.EXPENSE, true));
        categories.add(new Category("Nhà ở", "ic_house", "#795548", TransactionType.EXPENSE, true));
        categories.add(new Category("Giải trí", "ic_entertainment", "#9C27B0", TransactionType.EXPENSE, true));
        categories.add(new Category("Sức khỏe", "ic_health", "#F44336", TransactionType.EXPENSE, true));
        categories.add(new Category("Giáo dục", "ic_education", "#3F51B5", TransactionType.EXPENSE, true));
        categories.add(new Category("Hóa đơn", "ic_bill", "#FF9800", TransactionType.EXPENSE, true));
        categories.add(new Category("Khác", "ic_other", "#607D8B", TransactionType.EXPENSE, true));

        // Income categories
        categories.add(new Category("Lương", "ic_salary", "#4CAF50", TransactionType.INCOME, true));
        categories.add(new Category("Quà tặng", "ic_gift", "#E91E63", TransactionType.INCOME, true));
        categories.add(new Category("Đầu tư", "ic_invest", "#00BCD4", TransactionType.INCOME, true));
        categories.add(new Category("Làm thêm", "ic_freelance", "#8BC34A", TransactionType.INCOME, true));
        categories.add(new Category("Khác", "ic_other", "#607D8B", TransactionType.INCOME, true));

        return categories;
    }
}
