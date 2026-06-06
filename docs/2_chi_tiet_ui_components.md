# HƯỚNG DẪN ÔN TẬP LẬP TRÌNH ANDROID: CHI TIẾT CÁC THÀNH PHẦN GIAO DIỆN (UI COMPONENTS)

Tài liệu này giải thích chi tiết các thành phần giao diện thực tế trong mã nguồn của bạn, cách hoạt động của chúng và các kiến thức lý thuyết cốt lõi để trả lời khi giảng viên chỉ vào màn hình và hỏi *"Cái này là gì? Dùng component nào? Hoạt động ra sao?"*.

---

## 1. Thanh điều hướng Bottom Navigation & Jetpack Navigation
*   **Tệp tin giao diện:** `activity_main.xml` (chứa [FragmentContainerView](file:///d:/AppQuanLyChiTieu/app/src/main/res/layout/activity_main.xml#L9) và [BottomNavigationView](file:///d:/AppQuanLyChiTieu/app/src/main/res/layout/activity_main.xml#L21)).
*   **Tệp tin cấu hình điều hướng:** `nav_graph.xml` (chứa sơ đồ liên kết các Fragment màn hình).
*   **Cách thức liên kết:** Trong [MainActivity.java](file:///d:/AppQuanLyChiTieu/app/src/main/java/com/example/appquanlychitieu/MainActivity.java):
    ```java
    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment);
    NavController navController = navHostFragment.getNavController();
    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
    NavigationUI.setupWithNavController(bottomNav, navController);
    ```
*   **Nguyên lý hoạt động:** `BottomNavigationView` hiển thị menu ở cạnh dưới màn hình. `FragmentContainerView` đóng vai trò là một container rỗng. Khi người dùng click vào các mục menu, `NavController` sẽ dựa vào ID của menu item (phải trùng khớp với ID fragment trong `nav_graph.xml`) để tự động nạp Fragment tương ứng vào container.

---

## 2. Danh sách cuộn mượt RecyclerView & Adapter

Đây là thành phần cốt lõi hiển thị danh sách giao dịch ở màn hình Trang chủ (`HomeFragment`) và màn hình Lịch sử giao dịch (`TransactionListFragment`).

*   **Tệp tin mã nguồn:** [TransactionAdapter.java](file:///d:/AppQuanLyChiTieu/app/src/main/java/com/example/appquanlychitieu/ui/transaction/TransactionAdapter.java).
*   **RecyclerView hoạt động như thế nào?**
    1.  Nó sử dụng mẫu thiết kế **ViewHolder** (`ViewHolder extends RecyclerView.ViewHolder`) để lưu trữ tham chiếu đến các view con bên trong một item XML (như `TextView` số tiền, `ImageView` danh mục) nhằm tránh việc gọi `findViewById()` liên tục khi cuộn.
    2.  Nó chỉ khởi tạo số lượng View đủ hiển thị trên màn hình điện thoại (cộng thêm 1-2 view dự phòng ở biên). Khi cuộn màn hình, những View trôi ra khỏi màn hình sẽ không bị hủy mà được đưa vào một **bể chứa (Recycle Pool)**.
    3.  Khi có phần tử mới đi vào màn hình từ phía dưới, RecyclerView lấy một View cũ từ Recycle Pool ra, đưa cho Adapter cập nhật lại dữ liệu mới (gọi là cơ chế **Bind**) rồi hiển thị lại. Quá trình này giúp tiết kiệm tối đa bộ nhớ CPU và RAM.

### 2.1. Phân tích 3 phương thức bắt buộc của Adapter:
*   `onCreateViewHolder(ViewGroup parent, int viewType)`: 
    *   *Nhiệm vụ:* Nạp (Inflate) file layout XML thiết kế giao diện của từng dòng (ở đây là [item_transaction.xml](file:///d:/AppQuanLyChiTieu/app/src/main/res/layout/item_transaction.xml)) thành một đối tượng `View` trong Java, sau đó bọc nó vào đối tượng `ViewHolder` mới tạo và trả về.
    *   *Code thực tế:*
        ```java
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
        ```
*   `onBindViewHolder(ViewHolder holder, int position)`:
    *   *Nhiệm vụ:* Gắn kết dữ liệu từ nguồn (List dữ liệu tại vị trí `position`) vào các widget UI của `ViewHolder`. Đây là nơi bạn định dạng tiền tệ, đổi màu chữ số tiền (màu xanh nếu thu nhập, màu đỏ nếu chi tiêu) và nạp Icon tương ứng của Danh mục chi tiêu.
*   `getItemCount()`:
    *   *Nhiệm vụ:* Trả về tổng số lượng phần tử có trong danh sách dữ liệu để RecyclerView biết cần hiển thị bao nhiêu dòng.

---

## 3. Các thành phần giao diện nâng cao khác

### 3.1. Chọn Thu nhập / Chi tiêu bằng MaterialButtonToggleGroup
*   **Thành phần:** `MaterialButtonToggleGroup` bọc hai `MaterialButton` đại diện cho Thu nhập và Chi tiêu trong tệp layout [activity_add_edit_transaction.xml](file:///d:/AppQuanLyChiTieu/app/src/main/res/layout/activity_add_edit_transaction.xml).
*   **Lợi ích:** Đảm bảo tính loại trừ lẫn nhau (chỉ một nút được chọn tại một thời điểm - tương tự `RadioGroup` nhưng giao diện hiện đại hơn dưới dạng nút bấm dính liền nhau).
*   **Xử lý trong code:**
    ```java
    toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
        if (isChecked) {
            selectedType = checkedId == R.id.btn_income ? TransactionType.INCOME : TransactionType.EXPENSE;
            loadCategories(); // Tải lại danh sách danh mục tương ứng với loại vừa chọn
        }
    });
    ```

### 3.2. Chọn ngày bằng DatePickerDialog
*   **Thành phần:** Sử dụng lớp `DatePickerDialog` mặc định của Android SDK.
*   **Cách hoạt động:** Khi người dùng click vào EditText chọn ngày, một hộp thoại dạng lịch hiện lên. Khi người dùng chọn xong, hàm callback trả về các giá trị `year`, `month`, `dayOfMonth`. Ứng dụng gộp các giá trị này vào đối tượng `Calendar` để lấy thời gian dạng `timestamp (millisecond)` và định dạng lại chuỗi chữ hiển thị lên EditText bằng `DateUtils.formatDate()`.

### 3.3. Biểu đồ tròn PieChart (MPAndroidChart)
*   **Thành phần:** `<com.github.mikephil.charting.charts.PieChart>` trong tệp layout [fragment_statistics.xml](file:///d:/AppQuanLyChiTieu/app/src/main/res/layout/fragment_statistics.xml).
*   **Cách nạp dữ liệu:**
    1.  Chuyển danh sách `CategorySummary` thành danh sách các đối tượng `PieEntry` (chứa giá trị số tiền chi tiêu và tên danh mục).
    2.  Tạo một đối tượng `PieDataSet` chứa danh sách entry đó và gán các màu sắc tương ứng (lấy mã màu hex từ danh mục, ví dụ: `#FF5722`).
    3.  Tạo đối tượng `PieData` bao ngoài `PieDataSet`, định dạng hiển thị % bằng `PercentFormatter` và nạp vào PieChart thông qua `pieChart.setData(data)`. Sau đó gọi `pieChart.invalidate()` để vẽ lại biểu đồ lên màn hình.

---

## 4. Bộ câu hỏi lý thuyết & trả lời mẫu dành cho giảng viên

### Câu hỏi 1: Tại sao em lại chọn RecyclerView thay vì ListView để hiển thị danh sách giao dịch?
*   **Trả lời mẫu:** RecyclerView vượt trội hơn ListView ở các điểm:
    1.  **Hiệu năng vượt trội:** Bắt buộc áp dụng mẫu thiết kế ViewHolder giúp giảm tối đa số lần gọi `findViewById()`, tiết kiệm CPU. Nó tái sử dụng View cực tốt nhờ cơ chế Recycle Pool.
    2.  **Linh hoạt trong bố cục:** Dễ dàng thay đổi kiểu hiển thị bằng cách thay thế `LayoutManager` (như hiển thị dạng danh sách đứng bằng `LinearLayoutManager`, dạng lưới bằng `GridLayoutManager` hoặc lưới so le bằng `StaggeredGridLayoutManager`). Trong khi ListView chỉ hỗ trợ hiển thị danh sách đứng đơn giản.
    3.  **Hỗ trợ animation mượt mà:** RecyclerView tích hợp sẵn cơ chế tạo hiệu ứng khi thêm, xóa, sửa phần tử trong danh sách.

### Câu hỏi 2: Hàm `onCreateViewHolder` và `onBindViewHolder` trong RecyclerView Adapter khác nhau thế như thế nào? Khi nào thì chúng được gọi?
*   **Trả lời mẫu:**
    *   `onCreateViewHolder` được gọi khi RecyclerView cần tạo một ViewHolder mới (khi app mới khởi chạy hoặc khi số lượng View trên màn hình chưa đủ). Hàm này thực hiện việc chuyển mã XML của item thành đối tượng View (Inflate layout) và tạo đối tượng ViewHolder bọc View đó lại.
    *   `onBindViewHolder` được gọi khi RecyclerView cần hiển thị dữ liệu của một phần tử tại vị trí `position` cụ thể lên một ViewHolder đã có sẵn. Hàm này lấy dữ liệu từ danh sách gán vào các thuộc tính giao diện như đặt text cho TextView, gán ảnh cho ImageView. Nó được gọi liên tục mỗi khi người dùng cuộn danh sách để tái cấu trúc lại nội dung hiển thị của View.

### Câu hỏi 3: Hãy chỉ vào giao diện và giải thích cách em triển khai chọn danh mục dưới dạng lưới 4 cột ở màn hình Thêm giao dịch?
*   **Trả lời mẫu:**
    *   Trong giao diện XML của màn hình thêm giao dịch, em đặt một thẻ `<androidx.recyclerview.widget.RecyclerView>` với ID `rv_categories`.
    *   Trong file Java [AddEditTransactionActivity.java](file:///d:/AppQuanLyChiTieu/app/src/main/java/com/example/appquanlychitieu/ui/transaction/AddEditTransactionActivity.java), em cấu hình cho RecyclerView này hiển thị dưới dạng lưới bằng câu lệnh:
        `rvCategories.setLayoutManager(new GridLayoutManager(this, 4));`
    *   Con số `4` chỉ định lưới sẽ chia làm 4 cột đều nhau.
    *   Em viết thêm một class adapter nội là `CategoryGridAdapter` để chuyển đổi dữ liệu danh mục thành giao diện tròn (chứa icon và tên danh mục). Lớp này cũng xử lý việc nhấp chọn danh mục, giảm độ mờ (alpha) của các danh mục không được chọn và phóng to nhẹ danh mục được chọn để tạo trải nghiệm người dùng sinh động.
