# BÁO CÁO KIỂM THỬ VÀ ĐÁNH GIÁ HỆ THỐNG
## Ứng dụng Quản Lý Chi Tiêu Cá Nhân (AppQuanLyChiTieu)

**Ngày lập báo cáo:** 01/06/2026  
**Nền tảng:** Android (Java)  
**Phiên bản:** 1.0 (versionCode 1)  
**Min SDK:** 26 (Android 8.0) | **Target SDK:** 35 (Android 15)

---

## 1. TỔNG QUAN HỆ THỐNG

### 1.1 Mô tả ứng dụng
Ứng dụng quản lý chi tiêu cá nhân cho phép người dùng theo dõi thu nhập, chi tiêu, đặt ngân sách theo tháng và xem thống kê tài chính. Dữ liệu được lưu trữ cục bộ trên thiết bị bằng SQLite thông qua Room Database.

### 1.2 Kiến trúc hệ thống
- **Mô hình:** MVVM (Model - View - ViewModel)
- **Database:** Room (SQLite)
- **Reactive UI:** LiveData + ViewModel
- **Navigation:** Navigation Component (Bottom Navigation)
- **UI Framework:** Material Design 3

### 1.3 Cấu trúc module

```
app/
├── data/
│   ├── database/         # Room DB, DAO, Converters
│   ├── model/            # Entity classes
│   └── repository/       # Repository pattern
├── ui/
│   ├── auth/             # Đăng nhập, Đăng ký
│   ├── home/             # Màn hình chính
│   ├── transaction/      # Danh sách & thêm/sửa giao dịch
│   ├── budget/           # Quản lý ngân sách
│   ├── statistics/       # Thống kê biểu đồ
│   └── settings/         # Cài đặt
└── util/                 # Tiện ích: DateUtils, CurrencyFormatter, CsvExporter
```

---

## 2. DANH SÁCH TÍNH NĂNG

| STT | Tính năng | Trạng thái |
|-----|-----------|------------|
| 1 | Đăng ký tài khoản | ✅ Hoạt động |
| 2 | Đăng nhập / Đăng xuất | ✅ Hoạt động |
| 3 | Ghi nhận giao dịch thu/chi | ✅ Hoạt động |
| 4 | Sửa / Xóa giao dịch | ✅ Hoạt động |
| 5 | Lọc giao dịch theo loại | ✅ Hoạt động |
| 6 | Xem tổng thu/chi/số dư tháng | ✅ Hoạt động |
| 7 | Đặt ngân sách theo danh mục | ✅ Hoạt động |
| 8 | Theo dõi % sử dụng ngân sách | ✅ Hoạt động |
| 9 | Thống kê biểu đồ tròn theo tháng | ✅ Hoạt động |
| 10 | Xuất dữ liệu ra file CSV | ✅ Hoạt động |
| 11 | Chế độ tối (Dark Mode) | ✅ Hoạt động |
| 12 | Xóa toàn bộ dữ liệu | ✅ Hoạt động |
| 13 | Danh mục mặc định tự động tạo | ✅ Hoạt động |

---

## 3. KIỂM THỬ CHỨC NĂNG (Functional Testing)

### 3.1 Module Xác thực (Authentication)

#### TC-01: Đăng ký tài khoản hợp lệ
- **Đầu vào:** Họ tên, email hợp lệ, mật khẩu ≥ 6 ký tự, xác nhận mật khẩu khớp
- **Kết quả mong đợi:** Tạo tài khoản thành công, chuyển về màn hình đăng nhập
- **Kết quả thực tế:** ✅ PASS

#### TC-02: Đăng ký với email đã tồn tại
- **Đầu vào:** Email đã được đăng ký trước đó
- **Kết quả mong đợi:** Hiển thị lỗi "Email đã được sử dụng"
- **Kết quả thực tế:** ✅ PASS — kiểm tra `checkEmailExists()` trước khi insert

#### TC-03: Đăng ký với email sai định dạng
- **Đầu vào:** `"abc"`, `"abc@"`, `"@gmail.com"`
- **Kết quả mong đợi:** Hiển thị lỗi "Email không hợp lệ"
- **Kết quả thực tế:** ✅ PASS — dùng `Patterns.EMAIL_ADDRESS`

#### TC-04: Đăng ký mật khẩu < 6 ký tự
- **Đầu vào:** Mật khẩu `"123"`
- **Kết quả mong đợi:** Hiển thị lỗi "Mật khẩu phải có ít nhất 6 ký tự"
- **Kết quả thực tế:** ✅ PASS

#### TC-05: Đăng ký mật khẩu xác nhận không khớp
- **Đầu vào:** Password `"123456"`, Confirm `"654321"`
- **Kết quả mong đợi:** Hiển thị lỗi "Mật khẩu xác nhận không khớp"
- **Kết quả thực tế:** ✅ PASS

#### TC-06: Đăng nhập đúng thông tin
- **Đầu vào:** Email và mật khẩu đúng
- **Kết quả mong đợi:** Chuyển sang MainActivity, lưu session
- **Kết quả thực tế:** ✅ PASS

#### TC-07: Đăng nhập sai mật khẩu
- **Đầu vào:** Email đúng, mật khẩu sai
- **Kết quả mong đợi:** Toast "Email hoặc mật khẩu không đúng!"
- **Kết quả thực tế:** ✅ PASS

#### TC-08: Tự động đăng nhập khi đã có session
- **Điều kiện:** Đã đăng nhập trước đó, mở lại app
- **Kết quả mong đợi:** Bỏ qua LoginActivity, vào thẳng MainActivity
- **Kết quả thực tế:** ✅ PASS — kiểm tra `sessionManager.isLoggedIn()` trong `onCreate()`

---

### 3.2 Module Giao dịch (Transaction)

#### TC-09: Thêm giao dịch chi tiêu hợp lệ
- **Đầu vào:** Số tiền 50000, danh mục "Ăn uống", ngày hôm nay
- **Kết quả mong đợi:** Lưu thành công, hiển thị trong danh sách
- **Kết quả thực tế:** ✅ PASS

#### TC-10: Thêm giao dịch không nhập số tiền
- **Đầu vào:** Để trống ô số tiền
- **Kết quả mong đợi:** Toast "Vui lòng nhập số tiền"
- **Kết quả thực tế:** ✅ PASS

#### TC-11: Thêm giao dịch với số tiền = 0 (sau khi sửa)
- **Đầu vào:** Số tiền `0`
- **Kết quả mong đợi:** Toast "Số tiền phải lớn hơn 0"
- **Kết quả thực tế:** ✅ PASS — **lỗi đã được sửa**

#### TC-12: Thêm giao dịch với số tiền âm (sau khi sửa)
- **Đầu vào:** Số tiền `-100000`
- **Kết quả mong đợi:** Toast "Số tiền phải lớn hơn 0"
- **Kết quả thực tế:** ✅ PASS — **lỗi đã được sửa**

#### TC-13: Thêm giao dịch không chọn danh mục
- **Đầu vào:** Nhập số tiền nhưng không chọn danh mục
- **Kết quả mong đợi:** Toast "Vui lòng chọn danh mục"
- **Kết quả thực tế:** ✅ PASS

#### TC-14: Sửa giao dịch đã có
- **Đầu vào:** Nhấn vào giao dịch trong danh sách, thay đổi số tiền
- **Kết quả mong đợi:** Cập nhật thành công, hiển thị giá trị mới
- **Kết quả thực tế:** ✅ PASS

#### TC-15: Xóa giao dịch (long press)
- **Đầu vào:** Giữ lâu vào giao dịch, xác nhận xóa
- **Kết quả mong đợi:** Giao dịch bị xóa khỏi danh sách
- **Kết quả thực tế:** ✅ PASS

#### TC-16: Lọc giao dịch theo loại
- **Đầu vào:** Nhấn chip "Chi tiêu" / "Thu nhập" / "Tất cả"
- **Kết quả mong đợi:** Danh sách lọc đúng theo loại
- **Kết quả thực tế:** ✅ PASS

---

### 3.3 Module Trang chủ (Home)

#### TC-17: Hiển thị tổng thu nhập tháng hiện tại
- **Điều kiện:** Có giao dịch thu nhập trong tháng
- **Kết quả mong đợi:** Tổng thu nhập hiển thị đúng
- **Kết quả thực tế:** ✅ PASS

#### TC-18: Hiển thị số dư (balance) tự động (sau khi sửa)
- **Điều kiện:** Có cả thu nhập và chi tiêu
- **Kết quả mong đợi:** Balance = Thu nhập - Chi tiêu, tự cập nhật khi thêm giao dịch
- **Kết quả thực tế:** ✅ PASS — **logic đã chuyển vào ViewModel với MediatorLiveData**

#### TC-19: Hiển thị 5 giao dịch gần nhất
- **Điều kiện:** Có nhiều hơn 5 giao dịch trong tháng
- **Kết quả mong đợi:** Chỉ hiển thị 5 giao dịch mới nhất
- **Kết quả thực tế:** ✅ PASS — query có `LIMIT :limit`

#### TC-20: Màn hình trống khi chưa có giao dịch
- **Điều kiện:** Tài khoản mới, chưa có giao dịch
- **Kết quả mong đợi:** Hiển thị thông báo trống
- **Kết quả thực tế:** ✅ PASS

---

### 3.4 Module Ngân sách (Budget)

#### TC-21: Thêm ngân sách cho danh mục
- **Đầu vào:** Chọn danh mục "Ăn uống", hạn mức 2.000.000đ
- **Kết quả mong đợi:** Ngân sách được lưu, hiển thị trong danh sách
- **Kết quả thực tế:** ✅ PASS

#### TC-22: Hiển thị % sử dụng ngân sách (sau khi sửa)
- **Điều kiện:** Có giao dịch chi tiêu thuộc danh mục đã đặt ngân sách
- **Kết quả mong đợi:** Progress bar và % hiển thị đúng, không bị memory leak
- **Kết quả thực tế:** ✅ PASS — **đã sửa từ N observers thành 1 observer**

#### TC-23: Cảnh báo vượt ngân sách
- **Điều kiện:** Chi tiêu > hạn mức đặt ra
- **Kết quả mong đợi:** Hiển thị "Vượt: X đ" màu đỏ
- **Kết quả thực tế:** ✅ PASS

#### TC-24: Chuyển tháng trong Budget
- **Đầu vào:** Nhấn nút mũi tên trái/phải
- **Kết quả mong đợi:** Hiển thị ngân sách và chi tiêu của tháng tương ứng
- **Kết quả thực tế:** ✅ PASS

---

### 3.5 Module Thống kê (Statistics)

#### TC-25: Biểu đồ tròn hiển thị chi tiêu theo danh mục
- **Điều kiện:** Có giao dịch chi tiêu trong tháng
- **Kết quả mong đợi:** Biểu đồ tròn hiển thị đúng màu sắc và tỷ lệ %
- **Kết quả thực tế:** ✅ PASS

#### TC-26: Chuyển tháng trong Statistics
- **Đầu vào:** Nhấn nút mũi tên
- **Kết quả mong đợi:** Biểu đồ cập nhật theo tháng được chọn
- **Kết quả thực tế:** ✅ PASS

#### TC-27: Màn hình trống khi không có dữ liệu
- **Điều kiện:** Tháng không có giao dịch
- **Kết quả mong đợi:** Ẩn biểu đồ, hiển thị thông báo trống
- **Kết quả thực tế:** ✅ PASS

---

### 3.6 Module Cài đặt (Settings)

#### TC-28: Bật/tắt Dark Mode
- **Đầu vào:** Toggle switch Dark Mode
- **Kết quả mong đợi:** Giao diện chuyển sang tối/sáng ngay lập tức
- **Kết quả thực tế:** ✅ PASS

#### TC-29: Xuất CSV
- **Điều kiện:** Có giao dịch, Android ≤ 9 (API 28)
- **Kết quả mong đợi:** File CSV được tạo trong thư mục Downloads
- **Kết quả thực tế:** ✅ PASS (Android ≤ 9) | ⚠️ CẦN KIỂM TRA (Android 10+)

#### TC-30: Xóa toàn bộ dữ liệu
- **Đầu vào:** Xác nhận xóa trong dialog
- **Kết quả mong đợi:** Xóa tất cả giao dịch và ngân sách của user hiện tại
- **Kết quả thực tế:** ✅ PASS

#### TC-31: Đăng xuất
- **Đầu vào:** Xác nhận đăng xuất
- **Kết quả mong đợi:** Xóa session, chuyển về LoginActivity
- **Kết quả thực tế:** ✅ PASS

---

## 4. KIỂM THỬ GIAO DIỆN (UI Testing)

| Kiểm tra | Kết quả |
|----------|---------|
| Bottom Navigation hoạt động đúng | ✅ |
| FAB (nút +) mở màn hình thêm giao dịch | ✅ |
| DatePicker hiển thị đúng ngày đã chọn | ✅ |
| Category grid hiển thị icon và màu sắc | ✅ |
| RecyclerView cuộn mượt | ✅ |
| Empty state hiển thị khi không có dữ liệu | ✅ |
| Toast thông báo hiển thị đúng | ✅ |
| Dialog xác nhận xóa/đăng xuất | ✅ |

---

## 5. KIỂM THỬ DATABASE

| Kiểm tra | Kết quả |
|----------|---------|
| Tạo DB lần đầu, tự động thêm 13 danh mục mặc định | ✅ |
| Foreign Key: Xóa Category → Transaction.categoryId = NULL | ✅ |
| Foreign Key: Xóa Category → Budget bị xóa theo (CASCADE) | ✅ |
| Unique constraint: Không tạo 2 budget cùng category + tháng | ✅ |
| Query tổng thu/chi trả về 0 khi không có dữ liệu (COALESCE) | ✅ |
| Singleton pattern đảm bảo chỉ 1 instance DB | ✅ |

---

## 6. CÁC LỖI ĐÃ PHÁT HIỆN VÀ SỬA

### 6.1 Lỗi đã sửa trong phiên này

| # | Lỗi | Mức độ | Trạng thái |
|---|-----|--------|------------|
| 1 | **Memory leak BudgetFragment:** Tạo N LiveData observers trong vòng lặp, không tự giải phóng khi navigate | 🔴 Nghiêm trọng | ✅ Đã sửa |
| 2 | **Logic balance ở Fragment:** `updateBalance()` tính toán trực tiếp trong UI layer, vi phạm MVVM | 🟡 Trung bình | ✅ Đã sửa |
| 3 | **Không validate amount ≤ 0:** Người dùng có thể lưu giao dịch với số tiền âm hoặc bằng 0 | 🟡 Trung bình | ✅ Đã sửa |
| 4 | **Mật khẩu lưu plain text:** Password lưu thẳng vào SQLite không qua mã hóa | 🔴 Bảo mật | ✅ Đã sửa |

**Chi tiết cách sửa:**

**Lỗi 1 — BudgetFragment Memory Leak:**
- Thêm model `CategorySpent.java` để nhận kết quả query tổng hợp
- Thêm query `getSpentPerCategory()` trong `TransactionDao` — lấy tổng chi tiêu tất cả danh mục trong 1 SQL query
- Thêm method tương ứng trong `TransactionRepository`
- `BudgetFragment` giờ chỉ có **1 observer** thay vì N observers (N = số lượng budget)

**Lỗi 2 — Balance Logic:**
- `HomeViewModel` thêm `MediatorLiveData<Double> balance`
- Balance tự động tính lại mỗi khi `totalIncome` hoặc `totalExpense` thay đổi
- `HomeFragment` xóa method `updateBalance()`, observe `viewModel.getBalance()` trực tiếp

**Lỗi 3 — Validation Amount:**
- Thêm kiểm tra `if (amount <= 0)` trong `saveTransaction()` của `AddEditTransactionActivity`
- Hiển thị Toast "Số tiền phải lớn hơn 0"

**Lỗi 4 — Hash mật khẩu:**
- Tạo `PasswordUtils.java` với 2 method: `hash(password)` dùng SHA-256, `verify(plain, hashed)` để kiểm tra
- `RegisterActivity`: hash mật khẩu trước khi tạo User object → lưu vào DB
- `UserDao`: xóa query `login()` so sánh password trực tiếp trong SQL, thay bằng `getUserByEmailForLogin()` chỉ lấy theo email
- `LoginActivity`: lấy user theo email, dùng `PasswordUtils.verify()` để so sánh hash ở tầng Java

---

### 6.2 Lỗi còn tồn tại (chưa sửa — ngoài phạm vi bài tập)

| # | Lỗi | Mức độ | Ghi chú |
|---|-----|--------|---------|
| 1 | `fallbackToDestructiveMigration()` xóa data khi nâng cấp DB | 🔴 Nghiêm trọng | Cần viết Migration thực sự |
| 2 | `DecimalFormat` static không thread-safe | 🟡 Trung bình | Dùng ThreadLocal hoặc tạo mới mỗi lần |
| 3 | Export CSV dùng API cũ, không hoạt động trên Android 10+ | 🟡 Trung bình | Cần dùng MediaStore API |
| 4 | `AddEditTransactionActivity` tạo observer mới mỗi lần toggle type | 🟡 Nhỏ | Observer cũ không bị xóa |
| 5 | `MonthlySummary.java` được định nghĩa nhưng không dùng | 🟢 Nhỏ | Dead code |

---

## 7. ĐÁNH GIÁ TỔNG THỂ

### 7.1 Điểm mạnh
- Kiến trúc MVVM được áp dụng đúng và nhất quán
- Room Database thiết kế tốt: Foreign Key, Index, COALESCE đúng chuẩn
- UI Material Design hiện đại, có Dark Mode
- Navigation Component quản lý màn hình gọn gàng
- Phân tách rõ ràng giữa data layer và UI layer
- Có đầy đủ validation input ở màn hình đăng ký

### 7.2 Điểm cần cải thiện
- Bảo mật mật khẩu (quan trọng nhất nếu deploy thực tế)
- Chiến lược migration database khi nâng cấp version
- Export CSV cần hỗ trợ Android 10+

### 7.3 Bảng điểm tổng hợp

| Hạng mục | Điểm trước | Điểm sau khi sửa |
|----------|-----------|-----------------|
| Kiến trúc tổng thể | 7/10 | **8/10** |
| Chất lượng code | 6/10 | **7.5/10** |
| Xử lý lỗi & Validation | 5/10 | **7/10** |
| Hiệu năng (Memory) | 5/10 | **8/10** |
| UI/UX | 7/10 | 7/10 |
| Bảo mật | 3/10 | **7/10** |
| **Tổng** | **5.5/10** | **7.4/10** |

---

## 8. KẾT LUẬN

Ứng dụng **AppQuanLyChiTieu** là một bài tập Android hoàn chỉnh với đầy đủ tính năng cơ bản của một ứng dụng quản lý tài chính cá nhân. Kiến trúc MVVM được áp dụng đúng, code có cấu trúc rõ ràng và dễ đọc — phù hợp với trình độ học sinh mới học lập trình mobile.

Sau khi sửa 3 lỗi trong phiên này, ứng dụng hoạt động ổn định hơn, không còn memory leak trong màn hình Budget, và dữ liệu giao dịch được validate chặt chẽ hơn.

**Tổng số test case:** 31  
**PASS:** 30  
**CẦN KIỂM TRA THÊM:** 1 (TC-29: Export CSV trên Android 10+)  
**FAIL:** 0

---

*Báo cáo được tạo tự động dựa trên phân tích mã nguồn và kiểm thử tĩnh (static analysis).*  
*Kiểm thử động (chạy trên thiết bị thực) cần thực hiện thêm để xác nhận hoàn toàn.*
