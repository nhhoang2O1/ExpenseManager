# BÁO CÁO HỆ THỐNG — ỨNG DỤNG QUẢN LÝ CHI TIÊU CÁ NHÂN

> Môn học: Lập trình Mobile · Nền tảng: Android (Java thuần) · Kiến trúc: MVVM + Repository · Lưu trữ: Room (SQLite)

---

## 1. Tổng quan đồ án

### 1.1. Mục tiêu
Ứng dụng **Quản lý Chi tiêu cá nhân** giúp người dùng ghi lại các khoản **thu nhập** và **chi tiêu** hằng ngày, theo dõi **số dư**, đặt **hạn mức ngân sách** theo từng danh mục/tháng và **thống kê** chi tiêu bằng biểu đồ. Toàn bộ dữ liệu được lưu **cục bộ (offline)** trên máy bằng cơ sở dữ liệu SQLite (thông qua Room), không phụ thuộc máy chủ.

### 1.2. Đối tượng người dùng
- Cá nhân muốn kiểm soát tài chính hằng ngày một cách đơn giản.
- Ứng dụng hỗ trợ **nhiều tài khoản trên cùng một thiết bị**: mỗi người dùng đăng ký/đăng nhập riêng, dữ liệu giao dịch và ngân sách được tách biệt theo `userId`.

### 1.3. Các chức năng chính
| Nhóm | Chức năng |
|------|-----------|
| Tài khoản | Đăng ký, đăng nhập, đăng xuất, lưu phiên đăng nhập |
| Giao dịch | Thêm / sửa / xóa giao dịch thu - chi; lọc theo loại; xem giao dịch gần nhất |
| Trang chủ | Hiển thị số dư, tổng thu, tổng chi trong tháng và 5 giao dịch gần nhất |
| Ngân sách | Đặt hạn mức theo danh mục theo từng tháng, theo dõi % đã chi và phần còn lại / vượt |
| Thống kê | Biểu đồ tròn chi tiêu theo danh mục + lịch sử thu/chi theo tháng |
| Cài đặt | Bật/tắt chế độ tối, xuất dữ liệu ra CSV, đặt lại dữ liệu, đăng xuất |

---

## 2. Sơ đồ kiến trúc (MVVM + Repository)

### 2.1. Luồng dữ liệu
Ứng dụng tuân theo mô hình **MVVM** kết hợp tầng **Repository**:

```
┌─────────────┐   observe(LiveData)   ┌──────────────┐   gọi hàm   ┌──────────────┐
│    VIEW     │ ◀──────────────────── │  VIEW MODEL  │ ──────────▶ │  REPOSITORY  │
│ Activity /  │                       │ (AndroidVM)  │             │              │
│ Fragment    │ ──── sự kiện UI ─────▶ │              │             │              │
└─────────────┘                       └──────────────┘             └──────┬───────┘
                                                                          │ gọi DAO
                                                                          ▼
                                                                   ┌──────────────┐
                                                                   │     DAO      │
                                                                   │  (@Query...) │
                                                                   └──────┬───────┘
                                                                          ▼
                                                                   ┌──────────────┐
                                                                   │ ROOM (SQLite)│
                                                                   └──────────────┘
```

- **View** (`HomeFragment`, `TransactionListFragment`, `BudgetFragment`, `StatisticsFragment`, `SettingsFragment`, các Activity) chỉ chịu trách nhiệm hiển thị và bắt sự kiện người dùng. View **đăng ký (observe)** các `LiveData` của ViewModel.
- **ViewModel** (`HomeViewModel`, `TransactionListViewModel`, `BudgetViewModel`, `StatisticsViewModel`) giữ và xử lý dữ liệu phục vụ màn hình, sống độc lập với vòng đời xoay màn hình. Tất cả kế thừa `AndroidViewModel` để có `Application` context.
- **Repository** (`TransactionRepository`, `CategoryRepository`, `BudgetRepository`) là lớp trung gian che giấu nguồn dữ liệu; nó gọi DAO và chạy các thao tác ghi trên luồng nền (`databaseWriteExecutor`).
- **DAO** (`TransactionDao`, `CategoryDao`, `BudgetDao`, `UserDao`) khai báo truy vấn SQL qua annotation Room.
- **Room/SQLite** (`AppDatabase`) là nơi lưu trữ thật sự.

### 2.2. Lý do chọn kiến trúc
- **Tách biệt trách nhiệm**: UI không trực tiếp đụng tới SQL → dễ đọc, dễ bảo trì.
- **Tự động cập nhật giao diện**: nhờ `LiveData`, khi dữ liệu trong Room thay đổi, các màn hình đang quan sát sẽ được cập nhật mà không cần load lại thủ công.
- **An toàn vòng đời**: `ViewModel` + `LiveData` chỉ phát dữ liệu khi View ở trạng thái active, tránh rò rỉ bộ nhớ và crash khi xoay màn hình.
- **Đúng chuẩn Android hiện đại** (AndroidX Lifecycle), phù hợp yêu cầu thể hiện kiến thức môn học.

---

## 3. Giải thích từng thành phần quan trọng

### 3.1. Khởi tạo & điều hướng
| Class | Mục đích | Làm gì | Kiến thức áp dụng |
|-------|----------|--------|-------------------|
| `ExpenseManagerApp` | Lớp Application | Khởi tạo database khi app chạy (kích hoạt callback nạp danh mục mặc định) | `Application`, vòng đời app |
| `MainActivity` | Khung chứa chính sau đăng nhập | Gắn `BottomNavigationView` với `NavController` để chuyển giữa các Fragment | **Navigation Component**, **BottomNavigation**, Fragment |

### 3.2. Tầng xác thực (`ui/auth`)
| Class | Mục đích | Làm gì | Kiến thức áp dụng |
|-------|----------|--------|-------------------|
| `LoginActivity` | Đăng nhập | Kiểm tra phiên (nếu đã đăng nhập thì vào thẳng Main); xác thực email + mật khẩu hash; tạo session | **Activity**, **Intent**, **SharedPreferences** (qua `SessionManager`), xử lý nền bằng `ExecutorService`, `runOnUiThread` |
| `RegisterActivity` | Đăng ký | Kiểm tra hợp lệ (email, độ dài mật khẩu, xác nhận khớp), kiểm tra email trùng, **hash mật khẩu** rồi lưu | Validation form, `Patterns.EMAIL_ADDRESS`, hash SHA-256, Room insert |

Ví dụ thực tế (đăng nhập — verify mật khẩu ở tầng Java, không so sánh plaintext trong SQL):
```java
User user = db.userDao().getUserByEmailForLogin(email);
boolean ok = user != null && PasswordUtils.verify(password, user.getPassword());
```

### 3.3. Trang chủ (`ui/home`)
| Class | Mục đích | Làm gì | Kiến thức áp dụng |
|-------|----------|--------|-------------------|
| `HomeFragment` | Tổng quan tháng | Hiển thị số dư/thu/chi và 5 giao dịch gần nhất trên **ListView**; FAB thêm giao dịch | **Fragment**, **ListView + BaseAdapter**, **LiveData observe**, `FloatingActionButton`, điều hướng |
| `HomeViewModel` | Cấp dữ liệu trang chủ | Lấy tổng thu, tổng chi trong tháng; tính **số dư** bằng `MediatorLiveData` (gộp 2 nguồn thu & chi) | `AndroidViewModel`, `LiveData`, **`MediatorLiveData`** |

Số dư được tính phản ứng theo cả thu lẫn chi:
```java
balance.addSource(totalIncome, inc -> balance.setValue(safe(inc) - safe(totalExpense.getValue())));
balance.addSource(totalExpense, exp -> balance.setValue(safe(totalIncome.getValue()) - safe(exp)));
```

### 3.4. Giao dịch (`ui/transaction`)
| Class | Mục đích | Làm gì | Kiến thức áp dụng |
|-------|----------|--------|-------------------|
| `AddEditTransactionActivity` | Thêm/sửa giao dịch | Nhập số tiền, ghi chú, ngày (DatePicker), chọn loại thu/chi (toggle), chọn danh mục qua **GridView** | **Activity**, **Intent + extras**, **GridView + BaseAdapter**, `DatePickerDialog`, `MaterialButtonToggleGroup` |
| `CategoryGridViewAdapter` | Adapter danh mục | Hiển thị danh mục dạng lưới biểu tượng, có hiệu ứng chọn | **GridView**, **BaseAdapter**, **ViewHolder pattern** |
| `TransactionListAdapter` | Adapter danh sách giao dịch | Hiển thị giao dịch trong ListView, tô màu thu/chi, dùng cache danh mục | **ListView**, **BaseAdapter**, ViewHolder, callback click/long-click |
| `TransactionListFragment` | Danh sách & lọc | Hiển thị toàn bộ giao dịch, lọc theo Tất cả/Thu/Chi bằng `Chip`, long-click để xóa | Fragment, ListView, `AlertDialog`, Chip filter |
| `TransactionListViewModel` | Dữ liệu danh sách | Đổi nguồn dữ liệu theo bộ lọc bằng `switchMap` | **`Transformations.switchMap`**, `MutableLiveData` |
| `TransactionAdapter` | (RecyclerView – bản thay thế) | Adapter RecyclerView tương đương; hiện UI dùng bản ListView | RecyclerView (tham khảo) |

Lọc giao dịch theo loại (đổi nguồn LiveData động):
```java
transactions = Transformations.switchMap(filterType, type -> {
    if ("EXPENSE".equals(type)) return repository.getTransactionsByType(userId, EXPENSE);
    if ("INCOME".equals(type))  return repository.getTransactionsByType(userId, INCOME);
    return repository.getAllTransactions(userId);
});
```

### 3.5. Ngân sách (`ui/budget`)
| Class | Mục đích | Làm gì | Kiến thức áp dụng |
|-------|----------|--------|-------------------|
| `BudgetFragment` | Quản lý hạn mức | Chọn tháng (trước/sau), thêm ngân sách bằng dialog (Spinner + EditText), hiển thị tiến độ chi | Fragment, ListView, `Spinner`, `ArrayAdapter`, `AlertDialog` dựng động |
| `BudgetViewModel` | Dữ liệu ngân sách | Theo dõi tháng được chọn, lấy ngân sách theo tháng bằng `switchMap` | `switchMap`, `MutableLiveData<int[]>` |
| `BudgetListAdapter` | Adapter ngân sách | Hiển thị hạn mức, đã chi, % và còn lại/vượt bằng `ProgressBar` | **ListView + BaseAdapter**, `ProgressBar`, tô màu theo trạng thái |
| `BudgetAdapter` | (RecyclerView – bản thay thế) | Tương đương bản RecyclerView | RecyclerView (tham khảo) |

### 3.6. Thống kê (`ui/statistics`)
| Class | Mục đích | Làm gì | Kiến thức áp dụng |
|-------|----------|--------|-------------------|
| `StatisticsFragment` | Trực quan hóa | Vẽ **biểu đồ tròn** chi tiêu theo danh mục + 2 ListView (tổng theo danh mục và lịch sử theo tháng) | **MPAndroidChart (PieChart)**, ListView + BaseAdapter (lớp lồng), điều hướng tháng |
| `StatisticsViewModel` | Dữ liệu thống kê | Lấy tổng theo danh mục của tháng (`switchMap`) và lịch sử tổng thu/chi mọi tháng | `switchMap`, LiveData |

### 3.7. Cài đặt (`ui/settings`)
| Class | Mục đích | Làm gì | Kiến thức áp dụng |
|-------|----------|--------|-------------------|
| `SettingsFragment` | Tùy chọn | Bật/tắt **dark mode**, **xuất CSV** giao dịch, **đặt lại dữ liệu** người dùng, **đăng xuất** | `SwitchMaterial`, `AppCompatDelegate` (night mode), ghi file, `AlertDialog`, quản lý phiên |

### 3.8. Tiện ích (`util`)
| Class | Mục đích | Kiến thức áp dụng |
|-------|----------|-------------------|
| `PasswordUtils` | Hash & xác minh mật khẩu SHA-256 (không cần thư viện ngoài) | `MessageDigest`, bảo mật cơ bản |
| `SessionManager` | Lưu/đọc/xóa phiên đăng nhập | **SharedPreferences** |
| `CurrencyFormatter` | Định dạng tiền tệ kiểu Việt Nam (dấu chấm, ký hiệu ₫) | `DecimalFormat`, `Locale("vi","VN")` |
| `DateUtils` | Định dạng/khoảng ngày, nhãn "Hôm nay/Hôm qua", đầu–cuối tháng | `SimpleDateFormat`, `Calendar`, Locale VN |
| `CsvExporter` | Xuất giao dịch ra file CSV (có BOM cho Excel tiếng Việt) | I/O file, `FileWriter`, luồng nền |

---

## 4. Mô hình cơ sở dữ liệu

Cơ sở dữ liệu Room tên `expense_manager_db`, **version 2**, gồm 4 bảng (entity): `users`, `categories`, `transactions`, `budgets`.

### 4.1. Bảng `users`
| Cột | Kiểu | Ràng buộc |
|-----|------|-----------|
| `id` | long | Khóa chính, tự tăng |
| `name` | String | Tên hiển thị |
| `email` | String | **Index UNIQUE** (không trùng email) |
| `password` | String | Lưu **hash SHA-256**, không lưu plaintext |
| `createdAt` | long | Thời điểm tạo (millis) |

### 4.2. Bảng `categories`
| Cột | Kiểu | Ý nghĩa |
|-----|------|---------|
| `id` | long | Khóa chính, tự tăng |
| `name` | String | Tên danh mục (Ăn uống, Lương...) |
| `icon` | String | Tên tài nguyên icon (vd `ic_food`) |
| `color` | String | Mã màu hex (vd `#FF5722`) |
| `type` | TransactionType | `EXPENSE` hoặc `INCOME` (lưu dạng text qua Converter) |
| `isDefault` | boolean | Đánh dấu danh mục mặc định |

Khi tạo DB lần đầu, callback `onCreate` tự **nạp sẵn 13 danh mục mặc định** (9 chi tiêu + 4 thu nhập).

### 4.3. Bảng `transactions`
| Cột | Kiểu | Ràng buộc |
|-----|------|-----------|
| `id` | long | Khóa chính, tự tăng |
| `amount` | double | Số tiền |
| `note` | String | Ghi chú |
| `date` | long | Thời gian (timestamp millis) |
| `categoryId` | long | **Khóa ngoại → categories.id** (`ON DELETE SET NULL`), có index |
| `type` | TransactionType | Thu / Chi |
| `userId` | long | Chủ sở hữu giao dịch |

### 4.4. Bảng `budgets`
| Cột | Kiểu | Ràng buộc |
|-----|------|-----------|
| `id` | long | Khóa chính, tự tăng |
| `categoryId` | long | **Khóa ngoại → categories.id** (`ON DELETE CASCADE`) |
| `amount` | double | Hạn mức |
| `monthYear` | String | Định dạng `"YYYY-MM"` |
| `userId` | long | Chủ sở hữu |

Có **Index UNIQUE** trên bộ `(categoryId, monthYear, userId)` → mỗi danh mục chỉ có 1 hạn mức/tháng/người dùng; khi thêm trùng sẽ **REPLACE**.

### 4.5. Quan hệ giữa các bảng
```
users (1) ───< transactions (N)      [logic, qua userId]
users (1) ───< budgets (N)           [logic, qua userId]
categories (1) ───< transactions (N) [khóa ngoại, SET NULL khi xóa danh mục]
categories (1) ───< budgets (N)      [khóa ngoại, CASCADE khi xóa danh mục]
```
> Ghi chú: quan hệ `users → transactions/budgets` được quản lý ở mức logic bằng cột `userId` (không khai báo foreign key), còn `categories` được ràng buộc khóa ngoại thật sự ở Room.

### 4.6. Lớp dữ liệu phụ trợ (không phải bảng)
- `CategorySummary`: nhận kết quả query gộp tổng tiền + số giao dịch theo danh mục.
- `CategorySpent`: nhận tổng chi theo từng `categoryId` trong 1 lần query.
- `MonthlySummary`: tổng thu/chi theo tháng (có `getBalance()`).
- `TransactionType`: enum `EXPENSE` / `INCOME`, lưu xuống DB qua `Converters`.

---

## 5. Luồng nghiệp vụ tiêu biểu

### 5.1. Đăng ký → Đăng nhập
1. Mở app → `LoginActivity`. Nếu `SessionManager.isLoggedIn()` = true → vào thẳng `MainActivity`.
2. Người dùng bấm "Đăng ký" → `RegisterActivity`: nhập tên, email, mật khẩu, xác nhận.
3. Validate: email đúng định dạng, mật khẩu ≥ 6 ký tự, xác nhận khớp; kiểm tra email đã tồn tại.
4. Hash mật khẩu (SHA-256) rồi `userDao.insert(user)`. Quay về màn đăng nhập.
5. Đăng nhập: lấy user theo email → `PasswordUtils.verify(...)`. Đúng → `createLoginSession(...)` (lưu vào SharedPreferences) → vào `MainActivity`.

### 5.2. Thêm giao dịch
1. Từ Trang chủ hoặc danh sách, bấm **FAB** → `AddEditTransactionActivity`.
2. Chọn loại (Thu/Chi) → `loadCategories()` nạp danh mục đúng loại vào **GridView**.
3. Nhập số tiền, ghi chú, chọn ngày (`DatePickerDialog`), chọn danh mục trong lưới.
4. Bấm Lưu → validate (số tiền > 0, đã chọn danh mục) → tạo `Transaction` → `transactionDao.insert(...)` trên luồng nền.
5. Đóng màn hình; nhờ LiveData, Trang chủ và Danh sách **tự cập nhật**.

### 5.3. Đặt ngân sách
1. Vào tab Ngân sách → `BudgetFragment`, chọn tháng bằng nút trước/sau.
2. Bấm FAB → dialog gồm `Spinner` (danh mục chi) + `EditText` (hạn mức).
3. Lưu → tạo `Budget(categoryId, amount, "YYYY-MM", userId)` → `insert` (REPLACE nếu trùng).
4. Danh sách hiển thị **đã chi / hạn mức**, % qua `ProgressBar`, và "Còn lại" hoặc "Vượt".

### 5.4. Xem thống kê
1. Tab Thống kê → `StatisticsFragment`.
2. `StatisticsViewModel` lấy `getCategorySummary(...)` của tháng đang chọn → vẽ **PieChart** + ListView tổng theo danh mục.
3. Đồng thời lấy `getMonthlySummary(...)` → ListView lịch sử thu/chi/số dư theo từng tháng.

### 5.5. Xuất CSV
1. Tab Cài đặt → "Xuất CSV".
2. Observe toàn bộ giao dịch của người dùng → gọi `CsvExporter.exportTransactions(...)`.
3. Ghi file `chi_tieu_YYYY-MM.csv` vào thư mục Downloads, có BOM để Excel đọc đúng tiếng Việt; báo Toast đường dẫn khi xong.

---

## 6. Điểm nổi bật kỹ thuật

- **Tối ưu truy vấn gộp**: thay vì tạo N observer cho N danh mục, `getSpentPerCategory(...)` và `getCategorySummary(...)` dùng `GROUP BY` lấy tất cả trong **một query duy nhất** → giảm tải, ít observer, code gọn (`BudgetFragment`).
- **Phản ứng dữ liệu thông minh**:
  - `MediatorLiveData` tính số dư từ 2 nguồn thu/chi (`HomeViewModel`).
  - `Transformations.switchMap` đổi nguồn dữ liệu theo bộ lọc/tháng đã chọn (`TransactionListViewModel`, `BudgetViewModel`, `StatisticsViewModel`).
- **Bảo mật mật khẩu**: hash **SHA-256** bằng `MessageDigest` có sẵn (không thêm thư viện), không bao giờ lưu/đối chiếu plaintext.
- **Đa người dùng cùng máy**: mọi truy vấn giao dịch/ngân sách đều lọc theo `userId` lấy từ phiên đăng nhập.
- **Định dạng theo locale Việt Nam**: tiền tệ `#.###  ₫` (`CurrencyFormatter`), ngày tháng "dd/MM/yyyy", "Tháng MM/yyyy", nhãn "Hôm nay/Hôm qua" (`DateUtils`).
- **Hiệu năng danh sách**: tất cả adapter ListView/GridView áp dụng **ViewHolder pattern** + cache danh mục (`Map<Long, Category>`) để tránh truy vấn lặp.
- **Xử lý nền đúng cách**: mọi thao tác ghi DB chạy trên `databaseWriteExecutor` (thread pool 4), cập nhật UI qua `runOnUiThread`/LiveData → không chặn luồng chính.
- **CSV thân thiện Excel**: ghi ký tự BOM `\uFEFF` và thay dấu phẩy trong ghi chú để không vỡ cột.

---

## 7. Hạn chế & hướng cải tiến

| Hạn chế hiện tại | Ảnh hưởng | Hướng cải tiến (giữ ràng buộc ListView/GridView) |
|------------------|-----------|--------------------------------------------------|
| Hash SHA-256 không có "salt" | Mật khẩu yếu dễ bị tấn công từ điển/rainbow table | Thêm salt ngẫu nhiên mỗi user, hoặc dùng PBKDF2/bcrypt (vẫn có sẵn trong JDK với PBKDF2) |
| `fallbackToDestructiveMigration()` | Mất dữ liệu khi nâng version DB | Viết Migration thật sự khi đổi schema |
| `loadCategories()` gọi `observe(this,...)` mỗi lần đổi loại | Có thể chồng nhiều observer trên cùng Activity | Tách `LiveData` cố định hoặc dùng `removeObservers` trước khi observe lại |
| Tồn tại song song bản RecyclerView (`TransactionAdapter`, `BudgetAdapter`) không dùng | Dư thừa code | Gỡ bỏ để gọn, hoặc giữ làm tài liệu so sánh ListView vs RecyclerView |
| Xuất CSV dùng `getExternalStoragePublicDirectory` | Lỗi thời với Android 10+ (Scoped Storage) | Dùng `MediaStore`/SAF để ghi vào Downloads đúng chuẩn mới |
| Chưa có cảnh báo khi chi vượt ngân sách | Người dùng dễ bỏ lỡ | Thêm thông báo (Notification/Toast) khi % vượt ngưỡng |
| Dark mode không lưu lựa chọn | Tắt app là mất | Lưu trạng thái dark mode vào SharedPreferences và áp dụng khi khởi động |
| Chưa kiểm thử tự động | Khó đảm bảo chất lượng khi mở rộng | Thêm unit test cho `PasswordUtils`, `CurrencyFormatter`, `DateUtils` |

---

## 8. Bảng phân rã chức năng theo kiến thức môn học

| Chức năng | Màn hình / Class | Kiến thức Android áp dụng |
|-----------|------------------|----------------------------|
| Đăng ký tài khoản | `RegisterActivity`, `PasswordUtils` | Activity, validation, SHA-256, Room insert |
| Đăng nhập & giữ phiên | `LoginActivity`, `SessionManager` | Activity, Intent, **SharedPreferences** |
| Điều hướng giữa các tab | `MainActivity` | **Navigation Component**, **BottomNavigation**, Fragment |
| Tổng quan số dư/thu/chi | `HomeFragment`, `HomeViewModel` | Fragment, **LiveData**, **MediatorLiveData** |
| Danh sách giao dịch gần nhất | `HomeFragment`, `TransactionListAdapter` | **ListView + BaseAdapter**, ViewHolder |
| Thêm/sửa giao dịch | `AddEditTransactionActivity` | Activity, **Intent + extras**, `DatePickerDialog`, ToggleGroup |
| Chọn danh mục | `CategoryGridViewAdapter` | **GridView + BaseAdapter** |
| Danh sách & lọc giao dịch | `TransactionListFragment`, `TransactionListViewModel` | Fragment, ListView, Chip, **switchMap**, AlertDialog |
| Xóa giao dịch | `TransactionListFragment` | Long-click, `AlertDialog`, Room delete |
| Đặt & theo dõi ngân sách | `BudgetFragment`, `BudgetListAdapter`, `BudgetViewModel` | ListView, `Spinner`, `ProgressBar`, switchMap |
| Biểu đồ thống kê | `StatisticsFragment` | **MPAndroidChart (PieChart)**, ListView |
| Lịch sử thu/chi theo tháng | `StatisticsFragment`, `StatisticsViewModel` | LiveData, truy vấn `GROUP BY` theo tháng |
| Lưu trữ dữ liệu | `AppDatabase`, các DAO, các Entity | **Room/SQLite**, khóa chính/ngoại, index, TypeConverter |
| Truy cập dữ liệu | `*Repository` | Repository pattern, `ExecutorService` |
| Chế độ tối | `SettingsFragment` | `AppCompatDelegate` (Night Mode) |
| Xuất CSV | `SettingsFragment`, `CsvExporter` | I/O file, `FileWriter`, luồng nền |
| Đặt lại / Đăng xuất | `SettingsFragment`, `SessionManager` | AlertDialog, Room delete, SharedPreferences |
| Định dạng tiền & ngày | `CurrencyFormatter`, `DateUtils` | `DecimalFormat`, `SimpleDateFormat`, `Locale` VN |

---

### Thông tin cấu hình kỹ thuật
- `minSdk` 26, `targetSdk`/`compileSdk` 35, Java 11.
- Thư viện chính: AndroidX AppCompat, Material Components, Room 2.6.1, Navigation 2.7.7, Lifecycle (ViewModel + LiveData) 2.7.0, MPAndroidChart v3.1.0.
- Launcher: `LoginActivity`. Sau đăng nhập: `MainActivity` chứa 4 tab Fragment + màn thêm/sửa giao dịch.
