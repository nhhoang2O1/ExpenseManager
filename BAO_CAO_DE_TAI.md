# BÁO CÁO ĐỀ TÀI

## HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG

**KHOA: CÔNG NGHỆ THÔNG TIN 2**

Học phần: **Phát triển ứng dụng cho các thiết bị di động**

Trình độ đào tạo: **Đại học**

Hình thức đào tạo: **Chính quy**

**THÔNG TIN ĐỀ TÀI DỰ ÁN**

**ĐỀ TÀI SỐ 1**

---

## 1. Tên đề tài: 
**Xây dựng ứng dụng quản lý tài chính cá nhân Expense Manager**

## 2. Số lượng sinh viên thực hiện: 1 sinh viên

1. Nguyễn Ngọc Hoàng - MSSV: N22DCVT054 <Trưởng nhóm>

---

## 3. Mô tả đề tài

Expense Manager là ứng dụng Android giúp người dùng quản lý tài chính cá nhân một cách hiệu quả. Ứng dụng cung cấp các tính năng theo dõi thu chi, lập ngân sách, xem báo cáo thống kê, và bảo mật dữ liệu người dùng.

### 3.1. Chức năng Quản lý Danh mục

Ứng dụng cung cấp hệ thống danh mục để phân loại các giao dịch thu nhập và chi tiêu. Khi cài đặt lần đầu, hệ thống tự động tạo sẵn 13 danh mục mặc định bao gồm:

**Danh mục Chi tiêu:** Ăn uống, Di chuyển, Mua sắm, Nhà ở, Giải trí, Sức khỏe, Giáo dục, Hóa đơn, Khác.

**Danh mục Thu nhập:** Lương, Quà tặng, Đầu tư, Làm thêm.

Mỗi danh mục có màu sắc và biểu tượng riêng biệt để dễ dàng nhận diện. Các danh mục này được hiển thị xuyên suốt trong ứng dụng, từ màn hình thêm giao dịch (dạng lưới GridView) đến biểu đồ thống kê, tạo sự thống nhất về giao diện. Khi xóa một danh mục, các giao dịch liên quan vẫn được giữ lại để bảo toàn lịch sử tài chính.

### 3.2. Chức năng Quản lý Ngân sách

Đây là công cụ giúp người dùng kiểm soát chi tiêu hàng tháng. Người dùng có thể thiết lập hạn mức chi tiêu cho từng danh mục cụ thể trong một tháng (ví dụ: đặt ngân sách 2.000.000 VNĐ cho danh mục Ăn uống).

Hệ thống sẽ tự động tính tổng số tiền đã chi tiêu trong danh mục đó trong tháng hiện tại và hiển thị dưới dạng thanh tiến trình để người dùng dễ dàng theo dõi. Thanh tiến trình cho biết tỷ lệ phần trăm đã sử dụng so với ngân sách đề ra. Khi chi tiêu vượt quá hạn mức, thanh tiến trình chuyển sang màu đỏ và hiển thị số tiền đã vượt, cảnh báo người dùng cần điều chỉnh chi tiêu. Ngược lại, nếu còn trong hạn mức, hệ thống hiển thị số tiền còn lại bằng màu xanh lá.

### 3.3. Chức năng Màn hình Tổng quan (Dashboard)

Màn hình chính của ứng dụng cung cấp cái nhìn tổng quan về tình hình tài chính của người dùng trong tháng hiện tại:

**Bảng tổng quan:** Hiển thị 3 chỉ số quan trọng gồm tổng thu nhập, tổng chi tiêu và số dư (số dư = thu nhập - chi tiêu). Số dư được tự động cập nhật ngay lập tức khi có thay đổi về thu nhập hoặc chi tiêu. Tất cả số tiền được định dạng theo chuẩn Việt Nam với dấu chấm phân cách hàng nghìn và ký hiệu đồng (₫).

**Danh sách giao dịch gần đây:** Hiển thị 5 giao dịch mới nhất trong tháng hiện tại dưới dạng danh sách. Người dùng có thể nhấn vào giao dịch để xem chi tiết và chỉnh sửa.

**Thêm giao dịch mới:** Nút thêm giao dịch nhanh cho phép người dùng ghi lại thu chi bất cứ lúc nào. Màn hình thêm/sửa giao dịch hỗ trợ chọn ngày bằng lịch, chọn loại giao dịch (thu nhập hoặc chi tiêu), chọn danh mục từ lưới các danh mục có sẵn, nhập số tiền và ghi chú.

**Xem tất cả giao dịch:** Người dùng có thể xem danh sách đầy đủ tất cả giao dịch bằng cách nhấn "Xem tất cả". Trong màn hình danh sách giao dịch, người dùng có thể lọc theo loại (tất cả, chi tiêu, thu nhập), nhấn vào giao dịch để chỉnh sửa, hoặc nhấn giữ để xóa giao dịch.

### 3.4. Chức năng Báo cáo và Thống kê

Màn hình thống kê cung cấp các công cụ phân tích chi tiêu trực quan:

**Biểu đồ tròn:** Hiển thị tỷ lệ phần trăm chi tiêu theo từng danh mục dưới dạng biểu đồ tròn (Donut Chart). Mỗi phần của biểu đồ có màu tương ứng với màu của danh mục, giúp người dùng dễ dàng nhận biết danh mục nào chiếm tỷ trọng lớn trong tổng chi tiêu. Biểu đồ có hiệu ứng chuyển động khi hiển thị lần đầu.

**Danh sách chi tiết:** Bên dưới biểu đồ là danh sách chi tiết các danh mục, hiển thị tổng số tiền và số lượng giao dịch của từng danh mục.

**Chuyển đổi tháng:** Người dùng có thể xem thống kê của các tháng trước đó bằng cách nhấn nút Trước/Sau. Khi chuyển tháng, dữ liệu sẽ tự động cập nhật mà không cần tải lại màn hình.

**Lịch sử tài chính theo tháng:** Tab lịch sử hiển thị bảng tổng hợp thu nhập, chi tiêu và số dư của từng tháng. Số dư được tô màu xanh lá nếu thặng dư (thu > chi) và màu đỏ nếu thâm hụt (chi > thu).

### 3.5. Chức năng Quản lý Người dùng và Cài đặt

**Đăng ký và Đăng nhập:**
- Người dùng có thể đăng ký tài khoản mới với email và mật khẩu
- Hệ thống kiểm tra định dạng email hợp lệ và yêu cầu mật khẩu tối thiểu 6 ký tự
- Mật khẩu được mã hóa bằng thuật toán BCrypt với hệ số bảo mật cao (cost factor = 12) trước khi lưu vào cơ sở dữ liệu, đảm bảo an toàn ngay cả khi dữ liệu bị rò rỉ
- Khi đăng nhập, hệ thống so sánh mật khẩu đã mã hóa để xác thực

**Quản lý phiên làm việc:**
- Hệ thống lưu thông tin đăng nhập để người dùng không phải đăng nhập lại mỗi lần mở ứng dụng
- Mỗi lần khởi động ứng dụng, hệ thống kiểm tra phiên đăng nhập có hợp lệ không bằng cách so sánh với dữ liệu trong cơ sở dữ liệu (xác thực kép). Điều này ngăn chặn trường hợp tài khoản đã bị xóa nhưng vẫn truy cập được ứng dụng

**Chế độ giao diện sáng/tối:**
- Ứng dụng hỗ trợ chuyển đổi giữa chế độ sáng (Light mode) và chế độ tối (Dark mode)
- Người dùng có thể bật/tắt chế độ tối trong màn hình Cài đặt bằng công tắc
- Giao diện thay đổi ngay lập tức trên toàn bộ ứng dụng mà không cần khởi động lại

**Xóa dữ liệu và Đăng xuất:**
- Người dùng có thể xóa toàn bộ dữ liệu giao dịch và ngân sách (sau khi xác nhận)
- Chức năng đăng xuất giúp chuyển về màn hình đăng nhập và xóa phiên làm việc

### 3.6. Các biện pháp Bảo mật

Ứng dụng Expense Manager đặt bảo mật dữ liệu người dùng lên hàng đầu với các biện pháp sau:

**Mã hóa mật khẩu:**
- Mật khẩu của người dùng được mã hóa một chiều bằng thuật toán BCrypt với hệ số bảo mật cao (cost factor = 12) và salt ngẫu nhiên
- Đảm bảo an toàn tuyệt đối ngay cả khi dữ liệu bị rò rỉ vì không thể giải mã ngược lại mật khẩu gốc
- Khi đăng nhập, hệ thống so sánh mật khẩu đã mã hóa để xác thực

**Bảo vệ toàn vẹn dữ liệu:**
- Sử dụng Room Database với các ràng buộc khóa ngoại (Foreign Key) để đảm bảo tính toàn vẹn và nhất quán của dữ liệu
- Khi xóa tài khoản người dùng, tất cả dữ liệu liên quan (giao dịch, ngân sách) được tự động xóa sạch (CASCADE)
- Khi xóa danh mục, các giao dịch liên quan vẫn được giữ lại để bảo toàn lịch sử tài chính (SET_NULL)

**Xác thực phiên làm việc:**
- Hệ thống lưu thông tin phiên đăng nhập bằng SharedPreferences để người dùng không phải đăng nhập lại mỗi lần mở ứng dụng
- Mỗi lần khởi động ứng dụng, hệ thống thực hiện xác thực kép (double-validation):
  + Kiểm tra phiên đăng nhập có tồn tại trong SharedPreferences
  + So sánh với dữ liệu trong cơ sở dữ liệu để đảm bảo tài khoản vẫn hợp lệ
- Ngăn chặn trường hợp tài khoản đã bị xóa nhưng vẫn có thể truy cập ứng dụng

**Lưu trữ dữ liệu cục bộ:**
- Tất cả dữ liệu được lưu trữ cục bộ trên thiết bị của người dùng, không gửi lên server
- Đảm bảo quyền riêng tư tuyệt đối cho thông tin tài chính cá nhân

---

## 4. Yêu cầu của học viện

**Phát triển ứng dụng di động bằng ngôn ngữ lập trình Java đáp ứng các yêu cầu về chức năng và giao diện:** [CLO1]

- Ứng dụng được xây dựng trên nền tảng Android sử dụng các thành phần giao diện cơ bản như TextView, EditText, Button, ListView, GridView, ProgressBar, và các thành phần Material Design như Chip, ChipGroup, FloatingActionButton, MaterialButtonToggleGroup, SwitchMaterial, BottomNavigationView
- Sử dụng các Dialog như DatePickerDialog, AlertDialog để tương tác với người dùng
- Áp dụng kiến trúc MVVM (Model-View-ViewModel) kết hợp Repository Pattern để tách biệt logic nghiệp vụ và giao diện
- Sử dụng Room Database để quản lý cơ sở dữ liệu SQLite cục bộ
- Sử dụng LiveData và ViewModel để quản lý dữ liệu và trạng thái giao diện theo vòng đời của Activity/Fragment
- Sử dụng Navigation Component để điều hướng giữa các màn hình
- Lưu trữ cài đặt và phiên đăng nhập bằng SharedPreferences

**Bảo mật tài khoản và dữ liệu người dùng:** [CLO2]

- Có chức năng đăng ký và đăng nhập với xác thực email và mật khẩu
- Mật khẩu được mã hóa bằng thuật toán BCrypt với hệ số bảo mật cao (cost factor = 12) trước khi lưu vào cơ sở dữ liệu
- Phiên làm việc được xác thực kép tại màn hình đăng nhập và màn hình chính để đảm bảo tính hợp lệ
- Dữ liệu được lưu trữ cục bộ an toàn trên thiết bị của người dùng

**Triển khai và bảo vệ kết quả công việc:** [CLO2]

- Code được quản lý bằng Git và lưu trữ trên GitHub
- Áp dụng kiến trúc MVVM + Repository Pattern một cách nhất quán
- Tuân thủ các nguyên tắc lập trình hướng đối tượng và các best practices của Android

---

## 5. Công nghệ sử dụng

**Ngôn ngữ lập trình:** Java (JDK 11)

**Nền tảng:** Android
- Phiên bản biên dịch (compileSdk): 35
- Phiên bản tối thiểu (minSdk): 26 (Android 8.0)
- Phiên bản mục tiêu (targetSdk): 35

**Cơ sở dữ liệu:** Room Database 2.6.1 (SQLite)

**Thư viện chính:**
- AndroidX Lifecycle & ViewModel 2.7.0 - Quản lý vòng đời và trạng thái giao diện
- Navigation Component 2.7.7 - Điều hướng giữa các màn hình
- MPAndroidChart v3.1.0 - Vẽ biểu đồ thống kê
- BCrypt 0.10.2 - Mã hóa mật khẩu
- Material Design Components - Các thành phần giao diện hiện đại

**Kiến trúc:** MVVM (Model-View-ViewModel) kết hợp Repository Pattern

**Quản lý phiên bản:** Git và GitHub

---

**Ngày nộp:** 13/06/2026
