# BÁO CÁO ĐỒ ÁN MÔN HỌC: ỨNG DỤNG QUẢN LÝ CHI TIÊU CÁ NHÂN (EXPENSE MANAGER)

**Môn học:** Lập trình Thiết bị Di động  
**Nền tảng phát triển:** Android (Java thuần)  
**Kiến trúc:** MVVM (Model - View - ViewModel) + Repository  
**Cơ sở dữ liệu:** Room Database (SQLite)

---

## CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI

### 1.1. Lý do chọn đề tài và Mục tiêu
Trong xã hội hiện đại, việc quản lý tài chính cá nhân ngày càng trở nên quan trọng. Việc không kiểm soát được các khoản thu chi hàng ngày dễ dẫn đến tình trạng mất cân đối tài chính. Nhằm giải quyết nhu cầu đó, nhóm quyết định thực hiện đề tài **"Ứng dụng Quản lý Chi tiêu Cá nhân"** trên nền tảng Android.

**Mục tiêu của ứng dụng:**
*   Giúp người dùng ghi chép nhanh chóng, chính xác các khoản thu nhập và chi tiêu phát sinh hàng ngày.
*   Theo dõi biến động số dư tài khoản trong tháng.
*   Thiết lập hạn mức chi tiêu (ngân sách) cho từng danh mục để tránh chi tiêu quá đà.
*   Trực quan hóa dữ liệu tài chính thông qua biểu đồ để người dùng dễ dàng đánh giá thói quen tiêu dùng.
*   Hoạt động độc lập, lưu trữ dữ liệu cục bộ (offline) an toàn ngay trên thiết bị của người dùng.

### 1.2. Đối tượng sử dụng và Phạm vi đề tài
*   **Đối tượng:** Cá nhân (học sinh, sinh viên, người đi làm) muốn có công cụ quản lý tài chính cá nhân tiện lợi, đơn giản và trực quan.
*   **Phạm vi:** Ứng dụng chạy offline trên hệ điều hành Android (hỗ trợ từ Android 8.0 - API 26 trở lên). Ứng dụng hỗ trợ cơ chế **đa tài khoản trên cùng thiết bị**: mỗi người dùng sẽ đăng ký một tài khoản riêng, dữ liệu thu chi được phân tách bảo mật theo mã tài khoản (`userId`).

### 1.3. Các chức năng chính của hệ thống

| Phân hệ | Chức năng chi tiết | Mô tả |
| :--- | :--- | :--- |
| **Tài khoản** | Đăng ký & Đăng nhập | Tạo tài khoản mới, mã hóa mật khẩu, kiểm tra trùng email, duy trì trạng thái đăng nhập. |
| **Quản lý Thu Chi** | Thêm, Sửa, Xóa giao dịch | Nhập số tiền, chọn ngày (DatePicker), ghi chú và chọn danh mục tương ứng (Ăn uống, Di chuyển, Lương...). |
| **Trang chủ** | Báo cáo nhanh | Hiển thị tổng quan số dư hiện tại, tổng thu nhập, tổng chi tiêu trong tháng và danh sách 5 giao dịch gần đây nhất. |
| **Ngân sách** | Thiết lập hạn mức | Đặt hạn mức chi tiêu tối đa cho từng danh mục theo tháng. Hiển thị thanh tiến trình % đã tiêu dùng và số tiền còn lại/vượt mức. |
| **Báo cáo Thống kê** | Biểu đồ trực quan | Vẽ biểu đồ tròn cơ cấu chi tiêu theo danh mục, hiển thị danh sách chi tiết số tiền chi theo nhóm và lịch sử thu/chi qua các tháng. |
| **Cài đặt** | Tiện ích hệ thống | Bật/tắt chế độ tối (Dark Mode), xuất dữ liệu giao dịch ra file Excel (CSV), xóa toàn bộ dữ liệu, đăng xuất. |

---

## CHƯƠNG 2: THIẾT KẾ HỆ THỐNG

### 2.1. Kiến trúc phần mềm (MVVM + Repository)
Ứng dụng được xây dựng theo kiến trúc chuẩn khuyến cáo của Google dành cho Android: **MVVM (Model-View-ViewModel)** kết hợp lớp trung gian **Repository** nhằm tách biệt hoàn toàn phần xử lý giao diện (UI) và xử lý dữ liệu (Database).

#### Luồng đi của dữ liệu trong hệ thống:
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

*   **View (Activity/Fragment):** Chỉ đảm nhận nhiệm vụ hiển thị giao diện và nhận tương tác từ người dùng. View không trực tiếp truy vấn dữ liệu mà thực hiện đăng ký quan sát (**observe**) các đối tượng **LiveData** được cung cấp bởi ViewModel. Khi dữ liệu dưới database thay đổi, View sẽ tự động cập nhật.
*   **ViewModel:** Nơi lưu trữ dữ liệu tạm thời cho giao diện và xử lý các logic nghiệp vụ. ViewModel tồn tại độc lập với vòng đời của View (không bị hủy khi xoay màn hình), giúp giữ nguyên trạng thái ứng dụng.
*   **Repository:** Đóng vai trò là lớp quản lý các nguồn dữ liệu. Repository quyết định việc lấy dữ liệu từ cơ sở dữ liệu cục bộ và đẩy các tác vụ ghi/xóa xuống luồng nền (`databaseWriteExecutor`) để tránh làm đơ giao diện người dùng.
*   **DAO (Data Access Object):** Định nghĩa các phương thức và câu lệnh SQL truy vấn cơ sở dữ liệu thông qua thư viện Room.

### 2.2. Thiết kế Cơ sở dữ liệu (Database Schema)
Cơ sở dữ liệu của ứng dụng gồm 4 bảng dữ liệu chính được liên kết chặt chẽ:

```
  ┌──────────────┐             ┌──────────────────┐
  │    users     │             │   transactions   │
  ├──────────────┤             ├──────────────────┤
  │ id (PK)      │ ◄───┐       │ id (PK)          │
  │ name         │     └────── │ userId           │
  │ email (UQ)   │             │ categoryId (FK)  │ ───┐
  │ password     │             │ amount           │    │
  │ createdAt    │             │ note             │    │
  └──────────────┘             │ date             │    │
                               │ type             │    │
  ┌──────────────┐             └──────────────────┘    │
  │   budgets    │                                     │
  ├──────────────┤                                     │
  │ id (PK)      │                                     │
  │ userId       │ ◄───────────────────────────────────┤
  │ categoryId(FK) ────────────────────────────────────┼───┐
  │ amount       │                                     │   │
  │ monthYear    │                                     │   │
  └──────────────┘                                     │   │
                                                       ▼   ▼
                                              ┌──────────────────┐
                                              │    categories    │
                                              ├──────────────────┤
                                              │ id (PK)          │
                                              │ name             │
                                              │ icon             │
                                              │ color            │
                                              │ type             │
                                              │ isDefault        │
                                              └──────────────────┘
```

#### 2.2.1. Bảng `users` (Thông tin tài khoản)
*   `id` (Long, Primary Key, Auto-generate): Mã định danh duy nhất của người dùng.
*   `name` (String): Họ tên hiển thị.
*   `email` (String, Unique Index): Địa chỉ email đăng nhập (không cho phép trùng lặp).
*   `password` (String): Mật khẩu đã được mã hóa băm bằng thuật toán **BCrypt**.
*   `createdAt` (Long): Thời gian tạo tài khoản.

#### 2.2.2. Bảng `categories` (Danh mục chi tiêu)
*   `id` (Long, Primary Key, Auto-generate): Mã danh mục.
*   `name` (String): Tên danh mục (Ăn uống, Di chuyển, Lương, Mua sắm...).
*   `icon` (String): Tên tài nguyên icon của danh mục (ví dụ: `ic_food`).
*   `color` (String): Mã màu Hex tương ứng (ví dụ: `#FF5722`).
*   `type` (TransactionType): Phân loại danh mục (`EXPENSE` - Chi tiêu, hoặc `INCOME` - Thu nhập).
*   `isDefault` (Boolean): Đánh dấu danh mục hệ thống tạo sẵn.
*   *Lưu ý:* Cơ sở dữ liệu sẽ tự động nạp sẵn **13 danh mục mặc định** khi ứng dụng được khởi chạy lần đầu tiên.

#### 2.2.3. Bảng `transactions` (Giao dịch thu chi)
*   `id` (Long, Primary Key, Auto-generate): Mã giao dịch.
*   `userId` (Long): Mã người dùng sở hữu giao dịch này (liên kết logic với `users.id`).
*   `categoryId` (Long, Foreign Key -> `categories.id`): Liên kết với danh mục chi tiết. Thiết lập chế độ `ON DELETE SET NULL` (nếu danh mục bị xóa, giao dịch vẫn giữ nguyên và danh mục chuyển về null).
*   `amount` (Double): Số tiền giao dịch.
*   `note` (String): Ghi chú giao dịch.
*   `date` (Long): Ngày thực hiện giao dịch (dưới dạng timestamp miliseconds).
*   `type` (TransactionType): Loại giao dịch (`EXPENSE` hoặc `INCOME`).

#### 2.2.4. Bảng `budgets` (Ngân sách chi tiêu)
*   `id` (Long, Primary Key, Auto-generate): Mã hạn mức.
*   `userId` (Long): Mã người dùng (liên kết logic với `users.id`).
*   `categoryId` (Long, Foreign Key -> `categories.id`): Hạn mức áp dụng cho danh mục nào. Thiết lập chế độ `ON DELETE CASCADE` (nếu danh mục bị xóa thì ngân sách tương ứng tự động xóa theo).
*   `amount` (Double): Số tiền hạn mức tối đa.
*   `monthYear` (String): Tháng áp dụng hạn mức (định dạng `"YYYY-MM"`).
*   *Ràng buộc đặc biệt:* Thiết lập chỉ mục duy nhất (**Unique Index**) trên bộ ba gồm `(categoryId, monthYear, userId)`. Đảm bảo mỗi người dùng chỉ có duy nhất 1 hạn mức cho 1 danh mục trong cùng 1 tháng. Khi thêm trùng sẽ tự động ghi đè (`REPLACE`).

---

## CHƯƠNG 3: TRIỂN KHAI HỆ THỐNG (IMPLEMENTATION)

### 3.1. Các luồng nghiệp vụ chính và Code minh họa

#### Luồng 1: Đăng ký tài khoản (Đảm bảo an toàn mật khẩu)
Khi người dùng đăng ký, ứng dụng thực hiện kiểm tra định dạng email và độ dài mật khẩu (tối thiểu 6 ký tự). Để đảm bảo tính an toàn dữ liệu, mật khẩu thô (plaintext) tuyệt đối không được ghi trực tiếp vào cơ sở dữ liệu mà được mã hóa băm bằng thư viện **BCrypt** trước khi thực hiện lệnh chèn (`insert`).
```java
// Thực thi ghi trên luồng nền (background thread)
AppDatabase.databaseWriteExecutor.execute(() -> {
    // 1. Kiểm tra email đã được đăng ký chưa
    int exists = db.userDao().checkEmailExists(email);
    if (exists > 0) {
        runOnUiThread(() -> etEmail.setError("Email đã được sử dụng"));
        return;
    }
    // 2. Hash mật khẩu thô bằng BCrypt
    String hashedPassword = PasswordUtils.hash(password);
    User user = new User(name, email, hashedPassword);
    // 3. Chèn người dùng mới vào database
    db.userDao().insert(user);
    
    runOnUiThread(() -> {
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        finish();
    });
});
```

#### Luồng 2: Đăng nhập và Duy trì phiên đăng nhập (`SessionManager`)
Ứng dụng sử dụng **SharedPreferences** ở chế độ riêng tư (`MODE_PRIVATE`) để lưu trạng thái đăng nhập của người dùng. Khi mở app, nếu phát hiện phiên đăng nhập cũ vẫn còn hiệu lực, ứng dụng sẽ chuyển thẳng vào màn hình chính mà không yêu cầu nhập lại thông tin đăng nhập.
Mật khẩu nhập vào khi đăng nhập sẽ được so sánh với chuỗi băm trong database thông qua hàm `PasswordUtils.verify()`:
```java
User user = db.userDao().getUserByEmailForLogin(email);
boolean isPasswordCorrect = user != null && PasswordUtils.verify(password, user.getPassword());
if (isPasswordCorrect) {
    // Lưu thông tin đăng nhập vào Session
    sessionManager.createLoginSession(user.getId(), user.getName(), user.getEmail());
    navigateToMain();
}
```

#### Luồng 3: Thêm / Sửa giao dịch thu chi
Màn hình `AddEditTransactionActivity` thực hiện nạp danh sách danh mục tương ứng với loại giao dịch người dùng chọn (Thu hoặc Chi) hiển thị lên dạng lưới (**GridView**). Khi người dùng nhấn lưu, app phân tích xem đây là hành động sửa (có truyền kèm `transaction_id`) hay thêm mới để gọi truy vấn SQL phù hợp:
```java
if (editTransactionId != -1) {
    // Chế độ sửa giao dịch
    Transaction t = db.transactionDao().getTransactionById(editTransactionId);
    t.setAmount(amount); t.setNote(note); t.setDate(selectedDate);
    t.setCategoryId(selectedCategoryId); t.setType(selectedType);
    db.transactionDao().update(t);
} else {
    // Chế độ thêm mới giao dịch
    db.transactionDao().insert(new Transaction(amount, note, selectedDate, 
                                               selectedCategoryId, selectedType, userId));
}
```

#### Luồng 4: Danh sách giao dịch, Tìm kiếm bộ lọc bằng `switchMap`
Tại màn hình danh sách, người dùng có thể lọc nhanh các giao dịch bằng các thẻ (Chip) bộ lọc: *Tất cả*, *Thu nhập*, *Chi tiêu*. Để tối ưu hiệu năng và tránh rò rỉ bộ nhớ khi chuyển đổi bộ lọc liên tục, ViewModel sử dụng kỹ thuật biến đổi dữ liệu **`Transformations.switchMap`**:
```java
// Khi bộ lọc (filterType) thay đổi, nguồn LiveData tự động chuyển đổi nguồn truy vấn tương ứng
transactions = Transformations.switchMap(filterType, type -> {
    if ("EXPENSE".equals(type)) {
        return repository.getTransactionsByType(userId, TransactionType.EXPENSE);
    } else if ("INCOME".equals(type)) {
        return repository.getTransactionsByType(userId, TransactionType.INCOME);
    } else {
        return repository.getAllTransactions(userId);
    }
});
```

#### Luồng 5: Quản lý ngân sách chi tiêu và theo dõi tiến độ
Màn hình ngân sách cho phép người dùng đặt hạn mức chi tiêu hàng tháng theo danh mục. Ứng dụng tự động tính toán tổng số tiền người dùng đã chi tiêu trong tháng đó đối với danh mục thiết lập, so sánh và hiển thị trực quan thông qua thanh tiến độ (**ProgressBar**):
```java
// Công thức tính phần trăm chi tiêu trong BudgetListAdapter:
double spent = spentMap.getOrDefault(budget.getCategoryId(), 0.0);
double remaining = budget.getAmount() - spent;
int percentage = budget.getAmount() > 0 ? (int) ((spent / budget.getAmount()) * 100) : 0;

holder.progressBudget.setProgress(Math.min(percentage, 100));
holder.tvPercentage.setText(percentage + "%");
```

#### Luồng 6: Thống kê báo cáo trực quan
Ứng dụng tích hợp thư viện vẽ biểu đồ chuyên nghiệp **MPAndroidChart** để vẽ biểu đồ tròn (`PieChart`) thể hiện tỷ lệ % chi tiêu của từng danh mục trong tháng. Ngoài ra, ứng dụng còn truy vấn lịch sử dòng tiền theo từng tháng bằng câu lệnh SQL nhóm dữ liệu phức tạp:
```sql
-- Lấy tổng thu nhập và chi tiêu gom nhóm theo từng tháng của một người dùng
SELECT 
    strftime('%Y-%m', datetime(date/1000, 'unixepoch')) as monthYear,
    COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as totalIncome,
    COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as totalExpense
FROM transactions 
WHERE userId = :userId
GROUP BY monthYear 
ORDER BY monthYear DESC
```

### 3.2. Giao diện thực tế của hệ thống (Chèn hình ảnh minh họa)
*(Đây là danh sách các màn hình chính mà nhóm đã thiết kế giao diện thực tế trên điện thoại chạy ứng dụng)*
1.  **Màn hình Đăng nhập & Đăng ký:** Giao diện tối giản, tối ưu trải nghiệm nhập form.
2.  **Màn hình Trang chủ (Tổng quan tài chính):** Hiển thị số dư thẻ nổi bật, các biểu tượng thao tác nhanh và danh sách giao dịch gần đây.
3.  **Màn hình Thêm mới/Chỉnh sửa giao dịch:** Lưới chọn danh mục trực quan có màu sắc phân biệt, bảng chọn ngày dễ thao tác.
4.  **Màn hình Quản lý Ngân sách:** Danh sách hạn mức kèm thanh tiến trình trực quan (chuyển đỏ khi chi tiêu vượt hạn mức).
5.  **Màn hình Thống kê (Biểu đồ tròn):** Biểu đồ cơ cấu chi tiêu sinh động kèm chú thích và số liệu chi tiết.
6.  **Màn hình Cài đặt:** Các nút chức năng chuyển giao diện tối (Dark Mode) và nút xuất dữ liệu ra file CSV.

---

## CHƯƠNG 4: KẾT LUẬN & HƯỚNG PHÁT TRIỂN

### 4.1. Kết quả đạt được
*   **Về mặt kỹ thuật:** Ứng dụng được xây dựng thành công trên nền tảng Android SDK 35 (Java), chạy mượt mà trên các thiết bị từ Android 8.0 trở lên. Áp dụng đúng kiến trúc MVVM + Repository chuẩn của Google giúp mã nguồn sạch sẽ, dễ bảo trì.
*   **Về mặt nghiệp vụ:** Hoàn thành toàn bộ các tính năng cốt lõi đề ra trong mục tiêu đồ án. Đặc biệt cơ chế đa tài khoản và quản lý ngân sách hoạt động chính xác, giải quyết tốt nhu cầu thực tế của người dùng.
*   **Bảo mật:** Thông tin mật khẩu người dùng được bảo mật tuyệt đối nhờ băm mã hóa một chiều qua thuật toán **BCrypt** trước khi lưu trữ.

### 4.2. Những điểm hạn chế của đề tài
*   Ứng dụng hoạt động hoàn toàn offline trên một thiết bị duy nhất. Nếu người dùng gỡ cài đặt app hoặc làm mất máy, dữ liệu sẽ bị mất theo.
*   Chưa có cơ chế gửi thông báo đẩy (Push Notification) tự động nhắc nhở người dùng khi chi tiêu vừa chạm ngưỡng hạn mức ngân sách thiết lập trong ngày.
*   Hiện tại ứng dụng chỉ hỗ trợ một loại đơn vị tiền tệ duy nhất (VNĐ), chưa hỗ trợ chuyển đổi đa tiền tệ đối với các giao dịch ngoại tệ.

### 4.3. Hướng phát triển trong tương lai
*   **Đồng bộ đám mây (Cloud Sync):** Phát triển thêm phiên bản kết nối server (Firebase hoặc REST API tự xây dựng) để tự động đồng bộ dữ liệu, giúp người dùng đăng nhập trên nhiều thiết bị mà không bị mất dữ liệu.
*   **Cảnh báo thông minh (Smart Alerts):** Tích hợp dịch vụ chạy ngầm để gửi thông báo nhắc nhở ghi chép tài chính hàng ngày và đưa ra cảnh báo tức thời khi chi tiêu vượt hạn mức.
*   **Ứng dụng Trí tuệ Nhân tạo (AI Analysis):** Áp dụng AI phân tích lịch sử chi tiêu của người dùng, từ đó đưa ra các gợi ý và lời khuyên tiết kiệm tài chính cá nhân một cách thông minh, cá nhân hóa.
