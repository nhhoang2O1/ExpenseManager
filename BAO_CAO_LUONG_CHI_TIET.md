# BÁO CÁO CHI TIẾT CÁC LUỒNG NGHIỆP VỤ

> Tài liệu bổ sung cho `BAO_CAO_HE_THONG.md`, đi sâu từng luồng: các bước, class tham gia, đoạn code thật, xử lý đa luồng và các trường hợp đặc biệt.

---

## Quy ước chung trước khi đọc

- **Luồng nền**: mọi thao tác đọc/ghi database "đồng bộ" đều chạy trên `AppDatabase.databaseWriteExecutor` (thread pool 4 luồng). Sau khi xong, kết quả được đưa về luồng UI bằng `runOnUiThread(...)`.
- **Luồng phản ứng**: dữ liệu hiển thị danh sách/tổng tiền dùng `LiveData`. View chỉ `observe`, không tự gọi lại; khi DB đổi, Room **tự phát** giá trị mới.
- **Định danh người dùng**: `userId` luôn lấy từ `SessionManager` (đã lưu khi đăng nhập), được truyền vào mọi truy vấn để tách dữ liệu giữa các tài khoản.

```
View (Activity/Fragment)  →  ViewModel  →  Repository  →  DAO  →  Room/SQLite
        ▲                                                              │
        └───────────────── LiveData phát dữ liệu ─────────────────────┘
```

---

## LUỒNG 1 — Đăng ký tài khoản

### Mục đích
Tạo người dùng mới, đảm bảo email không trùng và mật khẩu được lưu an toàn.

### Các bước chi tiết
1. **Mở màn hình**: `LoginActivity` → bấm "Đăng ký" → `Intent` mở `RegisterActivity`.
2. **Nhập liệu**: họ tên, email, mật khẩu, xác nhận mật khẩu (`TextInputEditText`).
3. **Kiểm tra hợp lệ (trên luồng UI)** trong `register()`:
   - Tên không rỗng.
   - Email không rỗng và đúng định dạng `Patterns.EMAIL_ADDRESS`.
   - Mật khẩu ≥ 6 ký tự.
   - Xác nhận mật khẩu khớp.
4. **Kiểm tra trùng + lưu (trên luồng nền)**:
   ```java
   AppDatabase.databaseWriteExecutor.execute(() -> {
       int exists = db.userDao().checkEmailExists(email);   // SELECT COUNT(*)
       if (exists > 0) { runOnUiThread(() -> etEmail.setError("Email đã được sử dụng")); return; }

       String hashedPassword = PasswordUtils.hash(password); // SHA-256
       User user = new User(name, email, hashedPassword);
       db.userDao().insert(user);
       runOnUiThread(() -> { Toast(...); finish(); });        // quay lại Login
   });
   ```

### Sơ đồ tuần tự
```
RegisterActivity → (validate) → executor: checkEmailExists()
   ├─ trùng  → setError trên UI → dừng
   └─ chưa có → hash mật khẩu → userDao.insert() → Toast + finish()
```

### Class tham gia
`RegisterActivity`, `PasswordUtils`, `UserDao`, `User`, `AppDatabase`.

### Trường hợp đặc biệt
- Email trùng được chặn 2 lớp: kiểm tra `checkEmailExists` **và** ràng buộc `@Index(unique = true)` trên cột `email`.
- Mật khẩu được hash **trước khi** rời khỏi luồng nền, plaintext không bao giờ ghi xuống DB.

---

## LUỒNG 2 — Đăng nhập & giữ phiên

### Mục đích
Xác thực người dùng và duy trì trạng thái đăng nhập qua các lần mở app.

### Các bước chi tiết
1. **Kiểm tra phiên ngay khi mở app** (trong `onCreate` của `LoginActivity`):
   ```java
   if (sessionManager.isLoggedIn()) { navigateToMain(); return; }
   ```
   Nếu đã đăng nhập trước đó → vào thẳng `MainActivity`, không hiển thị form.
2. **Nhập email + mật khẩu**, validate rỗng trên UI.
3. **Xác thực trên luồng nền**:
   ```java
   User user = db.userDao().getUserByEmailForLogin(email);   // lấy theo email
   boolean ok = user != null && PasswordUtils.verify(password, user.getPassword());
   ```
   `verify` thực chất là `hash(input).equals(hashLưu)` → so sánh trên **bản hash**, không phải plaintext.
4. **Tạo phiên & điều hướng**:
   ```java
   sessionManager.createLoginSession(user.getId(), user.getName(), user.getEmail());
   navigateToMain(); // Intent có FLAG_ACTIVITY_NEW_TASK | CLEAR_TASK
   ```

### Vai trò SharedPreferences (`SessionManager`)
Lưu 4 khóa: `is_logged_in`, `user_id`, `user_name`, `user_email`. `getUserId()` trả về `-1` nếu chưa đăng nhập. Đây là "nguồn sự thật" về người dùng hiện hành cho toàn bộ ViewModel.

### Trường hợp đặc biệt
- Email không tồn tại và sai mật khẩu được gộp chung một thông báo "Email hoặc mật khẩu không đúng!" — tránh lộ thông tin email nào đã đăng ký.
- Cờ `CLEAR_TASK` xóa back-stack để người dùng không "Back" ngược về màn đăng nhập sau khi vào app.

---

## LUỒNG 3 — Thêm / Sửa giao dịch

### Mục đích
Ghi nhận một khoản thu hoặc chi, hoặc cập nhật giao dịch đã có.

### Các bước chi tiết
1. **Mở màn hình** `AddEditTransactionActivity`:
   - Từ FAB (thêm mới) → không có extra.
   - Từ click một dòng giao dịch → `Intent` kèm `transaction_id`.
   ```java
   editTransactionId = getIntent().getLongExtra("transaction_id", -1);
   if (editTransactionId != -1) { tvTitle.setText(R.string.edit_transaction); loadTransaction(); }
   ```
2. **Chọn loại Thu/Chi** bằng `MaterialButtonToggleGroup`. Mỗi lần đổi loại → `loadCategories()` nạp lại danh mục đúng loại:
   ```java
   db.categoryDao().getCategoriesByType(selectedType)
     .observe(this, categories -> categoryAdapter.setCategories(categories));
   ```
3. **Chọn danh mục** trong **GridView** (`CategoryGridViewAdapter`); item được chọn phóng to + rõ nét (alpha/scale), lưu `selectedCategoryId`.
4. **Chọn ngày** bằng `DatePickerDialog` → cập nhật `selectedDate` (timestamp millis).
5. **Lưu** (`saveTransaction()`):
   - Validate: số tiền không rỗng, parse được, `> 0`; đã chọn danh mục (`!= -1`).
   - Phân nhánh thêm/sửa trên luồng nền:
   ```java
   if (editTransactionId != -1) {
       Transaction t = db.transactionDao().getTransactionById(editTransactionId);
       t.setAmount(amount); t.setNote(note); t.setDate(selectedDate);
       t.setCategoryId(selectedCategoryId); t.setType(selectedType);
       db.transactionDao().update(t);
   } else {
       db.transactionDao().insert(new Transaction(amount, note, selectedDate,
                                                  selectedCategoryId, selectedType, userId));
   }
   runOnUiThread(() -> { Toast(...); finish(); });
   ```
6. **Cập nhật ngược về các màn hình**: Trang chủ và Danh sách đang `observe` LiveData nên **tự refresh** mà không cần truyền kết quả về.

### Sơ đồ tuần tự
```
FAB/Click → AddEditTransactionActivity
   ├─ (sửa) loadTransaction(): điền sẵn dữ liệu
   ├─ chọn loại → loadCategories() → GridView
   ├─ chọn danh mục / ngày / nhập tiền
   └─ Lưu → validate → insert|update (executor) → finish
                                   │
                                   ▼ (Room phát LiveData)
              HomeFragment & TransactionListFragment tự cập nhật
```

### Class tham gia
`AddEditTransactionActivity`, `CategoryGridViewAdapter`, `TransactionDao`, `CategoryDao`, `Transaction`, `DateUtils`, `SessionManager`.

### Trường hợp đặc biệt
- Khi sửa, các trường được nạp lại từ DB và set lại đúng toggle Thu/Chi.
- Giao dịch luôn gắn `userId` của phiên hiện tại để đảm bảo tách dữ liệu.

---

## LUỒNG 4 — Danh sách giao dịch & Lọc & Xóa

### Mục đích
Xem toàn bộ giao dịch, lọc theo loại, xóa khi cần.

### Các bước chi tiết
1. `TransactionListFragment` gắn `TransactionListAdapter` vào **ListView**.
2. **Cache danh mục**: observe toàn bộ category → tạo `Map<Long,Category>` để adapter tra cứu icon/màu/tên nhanh, tránh truy vấn lặp từng dòng.
3. **Lọc** bằng 3 `Chip` (Tất cả / Chi / Thu):
   ```java
   chipExpense.setOnClickListener(v -> viewModel.setFilter("EXPENSE"));
   ```
   Trong `TransactionListViewModel`, `switchMap` đổi nguồn LiveData theo bộ lọc:
   ```java
   transactions = Transformations.switchMap(filterType, type -> {
       case "EXPENSE": return repository.getTransactionsByType(userId, EXPENSE);
       case "INCOME" : return repository.getTransactionsByType(userId, INCOME);
       default       : return repository.getAllTransactions(userId);
   });
   ```
4. **Hiển thị/ẩn rỗng**: nếu danh sách trống → hiện `tvEmpty`, ẩn ListView.
5. **Xóa**: long-click một dòng → `AlertDialog` xác nhận → `viewModel.deleteTransaction(t)` → `repository.delete` chạy nền → LiveData tự cập nhật lại danh sách.

### Vì sao dùng `switchMap`
Bộ lọc (`MutableLiveData<String>`) thay đổi → `switchMap` **hủy theo dõi nguồn cũ và chuyển sang nguồn mới** một cách tự động, View chỉ cần observe đúng một `transactions`. Đây là điểm nhấn về LiveData nâng cao.

---

## LUỒNG 5 — Trang chủ (số dư, thu/chi tháng, gần nhất)

### Mục đích
Cung cấp cái nhìn tổng quan tài chính tháng hiện tại.

### Các bước chi tiết
1. `HomeViewModel` xác định khoảng thời gian tháng:
   ```java
   long start = DateUtils.getStartOfCurrentMonth();
   long end   = DateUtils.getEndOfCurrentMonth();
   ```
2. Lấy 3 nguồn dữ liệu:
   - `totalIncome` = tổng INCOME trong tháng.
   - `totalExpense` = tổng EXPENSE trong tháng.
   - `recentTransactions` = 5 giao dịch gần nhất (`LIMIT 5`).
3. **Tính số dư** bằng `MediatorLiveData` — kết hợp 2 nguồn:
   ```java
   balance.addSource(totalIncome, inc -> balance.setValue(safe(inc) - safe(totalExpense.getValue())));
   balance.addSource(totalExpense, exp -> balance.setValue(safe(totalIncome.getValue()) - safe(exp)));
   ```
   Bất kỳ nguồn nào đổi, số dư được tính lại ngay.
4. `HomeFragment` observe và đổ ra `tvIncome`, `tvExpense`, `tvBalance` (định dạng qua `CurrencyFormatter`) và ListView gần nhất.

### Vì sao dùng `MediatorLiveData`
Số dư = thu − chi, phụ thuộc **đồng thời** 2 LiveData. `MediatorLiveData` là cách chuẩn để "trộn" nhiều nguồn thành một giá trị phái sinh mà vẫn giữ tính phản ứng.

---

## LUỒNG 6 — Đặt & theo dõi ngân sách

### Mục đích
Đặt hạn mức chi cho từng danh mục theo tháng và theo dõi mức độ đã dùng.

### Các bước chi tiết
1. **Chọn tháng**: `BudgetViewModel` giữ `selectedMonthYear` là `int[]{year, month}`; nút trước/sau gọi `previousMonth()/nextMonth()` (dùng `Calendar` cộng/trừ tháng).
2. **Lấy ngân sách theo tháng** bằng `switchMap`:
   ```java
   budgets = switchMap(selectedMonthYear, my -> {
       String key = String.format("%04d-%02d", my[0], my[1] + 1); // "YYYY-MM"
       return repository.getBudgetsByMonth(userId, key);
   });
   ```
   > Lưu ý: `Calendar.MONTH` đếm từ 0, nên cộng `+1` khi tạo khóa `monthYear`.
3. **Tính đã chi cho mọi danh mục bằng 1 query**:
   ```java
   transactionRepository.getSpentPerCategory(userId, start, end)
       .observe(..., spentList -> {
           Map<Long,Double> spentMap = new HashMap<>();
           for (CategorySpent it : spentList) spentMap.put(it.categoryId, it.spent);
           adapter.setSpentMap(spentMap);
       });
   ```
   `getSpentPerCategory` dùng `GROUP BY categoryId` → tránh tạo N observer cho N danh mục.
4. **Hiển thị tiến độ** trong `BudgetListAdapter`:
   ```java
   double remaining = budget.getAmount() - spent;
   int percentage = budget.getAmount() > 0 ? (int)((spent/budget.getAmount())*100) : 0;
   progressBudget.setProgress(Math.min(percentage, 100));
   ```
   - `remaining >= 0` → "Còn lại …" (màu thu nhập).
   - `remaining < 0` → "Vượt …" (màu chi tiêu).
5. **Thêm ngân sách**: FAB → `AlertDialog` dựng động gồm `Spinner` (danh mục chi) + `EditText` (hạn mức) → tạo `Budget` → `insert` (`OnConflictStrategy.REPLACE`).

### Trường hợp đặc biệt
- Index UNIQUE `(categoryId, monthYear, userId)` + REPLACE → đặt lại hạn mức cùng danh mục/tháng sẽ **ghi đè**, không tạo bản trùng.
- Danh sách danh mục chi (`expenseCategories`) được lọc sẵn từ cache khi observe categories.

---

## LUỒNG 7 — Thống kê (PieChart + lịch sử tháng)

### Mục đích
Trực quan hóa cơ cấu chi tiêu và lịch sử thu/chi theo thời gian.

### Các bước chi tiết
1. **Tổng theo danh mục của tháng** (`StatisticsViewModel`):
   ```java
   categorySummary = switchMap(selectedMonthYear, my -> {
       long start = DateUtils.getStartOfMonth(my[0], my[1]);
       long end   = DateUtils.getEndOfMonth(my[0], my[1]);
       return repository.getCategorySummary(userId, EXPENSE, start, end);
   });
   ```
   Query `getCategorySummary` **JOIN** transactions với categories, `GROUP BY` danh mục, trả `CategorySummary` (tên, màu, icon, tổng tiền, số giao dịch).
2. **Vẽ PieChart** (`updatePieChart`): mỗi danh mục là một `PieEntry`, màu lấy từ `category.color`, hiển thị phần trăm bằng `PercentFormatter`.
3. **Danh sách tổng theo danh mục**: ListView dùng `CategorySummaryAdapter` (BaseAdapter lồng trong Fragment), tái dụng layout `item_transaction`.
4. **Lịch sử mọi tháng** (`getMonthlySummary`): query nhóm theo tháng bằng SQLite:
   ```sql
   SELECT strftime('%Y-%m', datetime(date/1000,'unixepoch')) AS monthYear,
          SUM(CASE WHEN type='INCOME'  THEN amount ELSE 0 END) AS totalIncome,
          SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) AS totalExpense
   FROM transactions WHERE userId=:userId GROUP BY monthYear ORDER BY monthYear DESC
   ```
   `MonthlySummary.getBalance()` = thu − chi; ListView `MonthlySummaryAdapter` tô màu số dư dương/âm.

### Điểm nhấn kỹ thuật
- Toàn bộ tính toán tổng hợp được đẩy xuống **SQL** (`SUM`, `CASE WHEN`, `GROUP BY`, `strftime`) thay vì lặp trong Java → nhanh và gọn.

---

## LUỒNG 8 — Xuất CSV

### Mục đích
Kết xuất giao dịch ra file để mở bằng Excel.

### Các bước chi tiết
1. `SettingsFragment` observe toàn bộ giao dịch của người dùng, nếu rỗng → Toast "không có giao dịch".
2. Gọi `CsvExporter.exportTransactions(...)` (chạy trên luồng nền):
   ```java
   FileWriter writer = new FileWriter(file);
   writer.write('\ufeff');                       // BOM để Excel đọc đúng tiếng Việt
   writer.write("Ngày,Loại,Danh mục,Số tiền,Ghi chú\n");
   for (Transaction t : transactions) {
       Category c = db.categoryDao().getCategoryById(t.getCategoryId());
       String note = t.getNote() != null ? t.getNote().replace(",", ";") : ""; // tránh vỡ cột
       writer.write(String.format("%s,%s,%s,%.0f,%s\n", date, type, name, t.getAmount(), note));
   }
   ```
3. File lưu tên `chi_tieu_YYYY-MM.csv` trong thư mục Downloads; callback `onSuccess/onError` báo Toast trên UI.

### Trường hợp đặc biệt
- Dấu phẩy trong ghi chú được thay bằng `;` để không phá cấu trúc CSV.
- BOM `\uFEFF` giúp Excel nhận UTF-8, hiển thị đúng dấu tiếng Việt.
- Hạn chế: dùng `getExternalStoragePublicDirectory` (lỗi thời từ Android 10+ với Scoped Storage) — xem mục cải tiến ở báo cáo chính.

---

## LUỒNG 9 — Đặt lại dữ liệu & Đăng xuất

### Đặt lại dữ liệu
1. Cài đặt → "Đặt lại" → `AlertDialog` xác nhận.
2. Trên luồng nền, xóa dữ liệu **theo người dùng hiện tại**:
   ```java
   db.transactionDao().deleteAllByUser(userId);
   db.budgetDao().deleteAllByUser(userId);
   ```
   → chỉ xóa giao dịch & ngân sách của user, **không** xóa tài khoản và danh mục.

### Đăng xuất
1. `AlertDialog` xác nhận → `sessionManager.logout()` (xóa SharedPreferences).
2. `Intent` về `LoginActivity` với `NEW_TASK | CLEAR_TASK`, `finish()` Activity hiện tại → không quay lại được bằng nút Back.

---

## Bảng tổng hợp luồng ↔ kỹ thuật

| Luồng | Kỹ thuật cốt lõi | Điểm nhấn báo cáo |
|-------|------------------|-------------------|
| Đăng ký | Validate + SHA-256 + Room insert | Chống trùng email 2 lớp |
| Đăng nhập | SharedPreferences + verify hash | Tự động vào app nếu còn phiên |
| Thêm/Sửa giao dịch | GridView + DatePicker + insert/update nền | LiveData tự cập nhật màn khác |
| Danh sách & lọc | switchMap + ListView + AlertDialog | Đổi nguồn dữ liệu động |
| Trang chủ | MediatorLiveData | Số dư phái sinh từ 2 nguồn |
| Ngân sách | switchMap + GROUP BY + ProgressBar | 1 query cho mọi danh mục |
| Thống kê | JOIN/GROUP BY/strftime + PieChart | Đẩy tính toán xuống SQL |
| Xuất CSV | File I/O + BOM + luồng nền | Tương thích Excel tiếng Việt |
| Reset/Đăng xuất | delete theo userId + clear session | Cô lập dữ liệu từng người dùng |
