# HƯỚNG DẪN ÔN TẬP LẬP TRÌNH ANDROID: KIẾN TRÚC TỔNG QUAN (MVVM & REPOSITORY PATTERN)

Tài liệu này phân tích chi tiết cấu trúc kiến trúc của ứng dụng **ExpenseManager** (Quản lý chi tiêu), giúp bạn trả lời các câu hỏi lý thuyết chuyên sâu của giảng viên về cách tổ chức mã nguồn, luồng dữ liệu, và lý do lựa chọn mô hình.

---

## 1. Mô hình Kiến trúc MVVM (Model - View - ViewModel)

Ứng dụng của bạn sử dụng mô hình kiến trúc **MVVM**, đây là kiến trúc chuẩn được Google khuyến nghị cho phát triển ứng dụng Android hiện đại nhằm tăng khả năng bảo trì, mở rộng và kiểm thử (testability).

```
   ┌────────────────────────────────────────────────────────┐
   │                    VIEW (UI Layer)                     │
   │      - Activities (LoginActivity, MainActivity,...)    │
   │      - Fragments (HomeFragment, BudgetFragment,...)    │
   │      - Layouts (XML)                                   │
   └───────────────────────────┬────────────────────────────┘
                               │
                Quan sát dữ liệu (Observe LiveData)
                               │
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │                VIEWMODEL (Business Logic)              │
   │     - HomeViewModel, BudgetViewModel,...               │
   │     - Giữ trạng thái UI bằng LiveData                  │
   └───────────────────────────┬────────────────────────────┘
                               │
                 Gọi lấy/cập nhật dữ liệu
                               │
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │             REPOSITORY (Data Coordinator)              │
   │     - TransactionRepository, BudgetRepository           │
   │     - Điều phối nguồn dữ liệu (Single Source of Truth)  │
   └───────────────────────────┬────────────────────────────┘
                               │
                      Đọc / Ghi dữ liệu
                               │
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │                ROOM DATABASE (SQLite)                  │
   │     - AppDatabase, DAOs (TransactionDao,...)           │
   └────────────────────────────────────────────────────────┘
```

### 1.1. Vai trò từng thành phần trong mã nguồn của bạn:

1.  **View (Tầng giao diện):**
    *   **Thành phần:** Gồm các file layout XML (ví dụ: `fragment_home.xml`) và các lớp Java kế thừa `AppCompatActivity` hoặc `Fragment` (ví dụ: `HomeFragment.java`, `LoginActivity.java`).
    *   **Nhiệm vụ:** Hiển thị dữ liệu lên màn hình và ghi nhận tương tác của người dùng (click, swipe, nhập liệu). View **không** chứa logic tính toán hay truy vấn cơ sở dữ liệu. Nó chỉ lắng nghe sự thay đổi của dữ liệu từ ViewModel thông qua việc quan sát (**Observe**) các đối tượng `LiveData`.
2.  **ViewModel (Tầng logic nghiệp vụ):**
    *   **Thành phần:** Các lớp kế thừa từ `AndroidViewModel` (ví dụ: `HomeViewModel.java`, `BudgetViewModel.java`).
    *   **Nhiệm vụ:** Xử lý và chuẩn bị dữ liệu để View hiển thị. Nó chứa các đối tượng `LiveData` lưu trữ dữ liệu cần thiết cho UI. ViewModel tồn tại độc lập với vòng đời của View. Ví dụ: khi xoay điện thoại, Activity bị hủy và tạo lại (Recreate), nhưng ViewModel vẫn giữ nguyên, giúp dữ liệu không bị mất và tránh lãng phí tài nguyên tải lại dữ liệu.
3.  **Repository (Tầng điều phối dữ liệu):**
    *   **Thành phần:** Các lớp như `TransactionRepository.java`, `BudgetRepository.java`.
    *   **Nhiệm vụ:** Đóng vai trò là nguồn dữ liệu duy nhất (Single Source of Truth) của ứng dụng. Repository che giấu sự phức tạp của tầng dữ liệu đối với ViewModel. ViewModel không cần biết dữ liệu được lấy từ SQLite qua Room hay từ máy chủ Web (API), nó chỉ việc yêu cầu dữ liệu từ Repository.
4.  **Model (Tầng dữ liệu):**
    *   **Thành phần:** Các thực thể (Entities) được định nghĩa cho Room Database như `Transaction.java`, `Category.java`, `Budget.java`, `User.java`.
    *   **Nhiệm vụ:** Đại diện cho cấu trúc dữ liệu thực tế lưu trữ trong bảng cơ sở dữ liệu SQLite.

---

## 2. Luồng đi của dữ liệu (Data Flow) trong thực tế

Hãy lấy ví dụ về luồng hoạt động khi **Người dùng thêm một giao dịch mới**:

1.  **Tương tác ở View:** Người dùng nhập số tiền, chọn ngày, chọn danh mục và nhấn nút "Lưu" trên giao diện của `AddEditTransactionActivity`.
2.  **Kích hoạt luồng lưu trữ:** 
    *   Code trong `AddEditTransactionActivity` kiểm tra tính hợp lệ của dữ liệu đầu vào.
    *   Nó sẽ gọi thực thi câu lệnh lưu trữ thông qua Database Instance. Trong code của bạn, việc này được thực hiện bằng cách tạo một luồng chạy ngầm qua Thread Pool của Room:
        ```java
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Transaction transaction = new Transaction(amount, note, selectedDate, selectedCategoryId, selectedType, userId);
            db.transactionDao().insert(transaction);
        });
        ```
3.  **Lưu vào SQLite (Room):**
    *   Task ghi dữ liệu được thực thi bất đồng bộ trên một luồng nền (background thread) được quản lý bởi `ExecutorService` có 4 luồng cố định (`newFixedThreadPool(4)`). Việc này giúp giao diện người dùng (Main/UI Thread) không bị đơ hoặc giật lag trong quá trình ghi đĩa.
    *   Room DB thực thi câu lệnh SQL INSERT vào bảng `transactions` trong SQLite.
4.  **Tự động cập nhật giao diện (Reactive Update):**
    *   Vì các câu lệnh lấy dữ liệu trong DAO của bạn trả về kiểu `LiveData<List<Transaction>>`, Room Database sẽ tự động nhận biết có sự thay đổi dữ liệu trong bảng `transactions`.
    *   Room tự động kích hoạt truy vấn lại ngầm và đẩy danh sách giao dịch mới nhất lên `LiveData`.
    *   `HomeViewModel` hoặc `TransactionListViewModel` chứa LiveData này sẽ nhận được dữ liệu mới.
    *   Ở phía View (`HomeFragment` hoặc `TransactionListFragment`), do đã đăng ký lắng nghe (Observe) LiveData này từ trước:
        ```java
        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            // Cập nhật danh sách vào Adapter
            adapter.setTransactions(transactions);
        });
        ```
    *   Hàm callback `onChanged` được kích hoạt trên UI Thread. View nhận danh sách mới, chuyển cho `RecyclerView.Adapter` và gọi `notifyDataSetChanged()` để vẽ lại giao diện cho người dùng thấy giao dịch vừa thêm.

---

## 3. Bộ câu hỏi lý thuyết & trả lời mẫu dành cho giảng viên

### Câu hỏi 1: Tại sao em lại sử dụng mô hình MVVM mà không dùng MVC hay viết hết code vào Activity?
*   **Trả lời mẫu:** 
    *   Nếu dùng MVC hoặc viết hết code vào Activity, lớp Activity sẽ trở nên cực kỳ cồng kềnh (gọi là *God Object/Spaghetti Code*), chứa cả code vẽ UI, code xử lý sự kiện, code tính toán và cả code kết nối database. Điều này rất khó bảo trì và sửa lỗi.
    *   Khi sử dụng **MVVM**:
        *   **Tách biệt trách nhiệm (Separation of Concerns):** Giúp mã nguồn rõ ràng, dễ đọc, dễ viết Unit Test cho logic mà không cần khởi chạy giao diện Android.
        *   **Quản lý vòng đời tốt (Lifecycle Awareness):** ViewModel không bị hủy khi cấu hình thiết bị thay đổi (như xoay màn hình), giúp dữ liệu không bị mất và tránh rò rỉ bộ nhớ (Memory Leak) so với việc giữ tham chiếu UI trong các luồng chạy ngầm.

### Câu hỏi 2: Lớp Repository đóng vai trò gì ở đây? Nếu không có Repository thì ứng dụng có chạy được không?
*   **Trả lời mẫu:** 
    *   Nếu không có Repository, ứng dụng vẫn chạy được bình thường bằng cách cho ViewModel gọi trực tiếp đến DAO của Room.
    *   Tuy nhiên, việc có **Repository** giúp chuẩn hóa kiến trúc. Nó đóng vai trò là lớp trừu tượng hóa dữ liệu (Data Abstraction Layer). Nếu sau này ứng dụng của em nâng cấp lên lưu trữ dữ liệu trên máy chủ trực tuyến (Cloud) bên cạnh SQLite dưới máy, ViewModel sẽ không cần phải sửa đổi bất kỳ dòng code nào. ViewModel chỉ gọi Repository, và Repository sẽ tự quyết định xem khi nào cần lấy dữ liệu từ mạng, khi nào cần lấy dữ liệu từ Room DB dưới máy offline.

### Câu hỏi 3: Tại sao tất cả các thao tác ghi dữ liệu (Insert, Update, Delete) của em lại phải chạy qua `databaseWriteExecutor`? Điều gì xảy ra nếu chạy trực tiếp trên Main Thread?
*   **Trả lời mẫu:**
    *   Hệ điều hành Android quy định không được phép thực hiện các thao tác tốn thời gian như đọc/ghi cơ sở dữ liệu hoặc kết nối mạng trực tiếp trên **Main Thread (UI Thread)**.
    *   Nếu chạy trực tiếp trên Main Thread, giao diện sẽ bị chặn (block). Nếu thao tác kéo dài quá 5 giây, hệ thống Android sẽ lập tức dừng ứng dụng và hiển thị thông báo lỗi **ANR (Application Not Responding - Ứng dụng không phản hồi)**, gây trải nghiệm rất tệ cho người dùng.
    *   Do đó, em dùng `databaseWriteExecutor` (một ExecutorService chứa 4 background threads) để đẩy các tác vụ Insert, Update, Delete xuống chạy ngầm. Khi hoàn thành, em dùng `runOnUiThread()` hoặc tận dụng cơ chế tự động chuyển luồng của `LiveData` để cập nhật lại giao diện trên Main Thread.
