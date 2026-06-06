# HƯỚNG DẪN ÔN TẬP LẬP TRÌNH ANDROID: LÝ THUYẾT & KỊCH BẢN ÁP DỤNG BROADCASTRECEIVER

Tài liệu này tập trung giải đáp chi tiết câu hỏi lý thuyết của giảng viên: *"Cái này dùng BroadcastReceiver như thế nào? Dữ liệu của nó ra sao?"*. Tài liệu cung cấp kiến thức nền tảng và 2 kịch bản code mẫu hoàn chỉnh áp dụng vào ứng dụng quản lý chi tiêu của bạn để ghi điểm tuyệt đối trong buổi vấn đáp.

---

## 1. Lý thuyết cốt lõi về BroadcastReceiver

`BroadcastReceiver` (Bộ thu quảng bá) là một trong 4 thành phần cơ bản của hệ điều hành Android (Activity, Service, BroadcastReceiver, ContentProvider). Nó đóng vai trò như một bộ lắng nghe sự kiện của toàn hệ thống hoặc sự kiện nội bộ do ứng dụng tự phát ra.

*   **Cách thức truyền nhận dữ liệu:**
    *   Sự kiện được gửi đi thông qua một đối tượng **Intent**.
    *   Intent này bắt buộc phải chứa một chuỗi định danh sự kiện gọi là **Action** (ví dụ: sự kiện tắt màn hình là `Intent.ACTION_SCREEN_OFF`).
    *   Dữ liệu đính kèm theo sự kiện được lưu trữ trong **Bundle** của Intent dưới dạng các cặp Khóa - Giá trị (Key-Value) thông qua hàm `intent.putExtra()`.
    *   Khi sự kiện xảy ra, hệ điều hành Android tự động tìm kiếm các ứng dụng đã đăng ký lắng nghe Action đó và kích hoạt hàm `onReceive(Context context, Intent intent)` của BroadcastReceiver tương ứng để xử lý dữ liệu.

### 1.2. So sánh hai phương thức đăng ký:

| Tiêu chí | Đăng ký tĩnh (Static) | Đăng ký động (Dynamic) |
| :--- | :--- | :--- |
| **Cách khai báo** | Viết trong thẻ `<receiver>` ở file `AndroidManifest.xml` | Viết trực tiếp trong code Java sử dụng `context.registerReceiver()` |
| **Vòng đời** | Tồn tại độc lập với ứng dụng. Kể cả khi ứng dụng bị tắt hoàn toàn, hệ thống vẫn tự động khởi chạy app để nhận sự kiện. | Gắn liền với vòng đời của Component chứa nó (thường là Activity hoặc Fragment). |
| **Hủy đăng ký** | Không cần hủy thủ công. | Bắt buộc phải hủy đăng ký bằng `context.unregisterReceiver()` khi Component bị dừng (ví dụ hủy ở `onPause()` nếu đăng ký ở `onResume()`). |
| **Quy định Android mới** | Bị Google hạn chế tối đa từ Android 8.0 (Oreo) nhằm tiết kiệm pin và tránh RAM bị quá tải bởi các ứng dụng chạy ngầm liên tục. | Được Google khuyến khích sử dụng cho hầu hết các sự kiện. |

---

## 2. Kịch bản thực tế 1: Tự động ghi chép chi tiêu từ tin nhắn SMS biến động số dư ngân hàng

Đây là một chức năng cực kỳ cao cấp và thực tế cho các app quản lý tài chính.

### Bước 1: Khai báo quyền trong `AndroidManifest.xml`
Để đọc được tin nhắn SMS đến, ứng dụng cần xin quyền từ người dùng:
```xml
<uses-permission android:name="android.permission.RECEIVE_SMS" />
```

### Bước 2: Viết mã nguồn bộ thu `SMSReceiver.java`
Bạn tạo một file class kế thừa `BroadcastReceiver`:
```java
package com.example.appquanlychitieu.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;
import com.example.appquanlychitieu.util.SessionManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Kiểm tra xem Action của Intent có phải là tin nhắn SMS đến hay không
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // Trích xuất dữ liệu thô (pdus) từ Bundle của tin nhắn gửi tới
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        // Chuyển đổi dữ liệu thô thành đối tượng SmsMessage trong Java
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress(); // Số điện thoại gửi
                        String messageBody = smsMessage.getMessageBody(); // Nội dung tin nhắn
                        
                        // Ví dụ nội dung tin nhắn ngân hàng Vietcombank: 
                        // "VCB: TK 10243431... -50,000 VND luc 12:00..."
                        if (messageBody.contains("VCB") || messageBody.contains("Vietcombank")) {
                            parseAndSaveTransaction(context, messageBody);
                        }
                    }
                }
            }
        }
    }

    private void parseAndSaveTransaction(Context context, String smsText) {
        // Sử dụng Regex để tìm số tiền bị trừ (có dấu trừ phía trước, ví dụ: -50,000 hoặc -150.000)
        Pattern pattern = Pattern.compile("-[0-9,.]+");
        Matcher matcher = pattern.matcher(smsText);
        
        if (matcher.find()) {
            String amountStr = matcher.group().replace("-", "").replace(",", "").replace(".", "");
            double amount = Double.parseDouble(amountStr);
            
            // Lấy ID người dùng hiện tại từ SharedPreferences
            SessionManager session = new SessionManager(context);
            long userId = session.getUserId();
            
            if (userId != -1) {
                // Chạy ngầm ghi giao dịch tự động vào Database
                AppDatabase db = AppDatabase.getDatabase(context);
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    // ID danh mục 9 mặc định là "Khác" (ic_other)
                    Transaction autoTx = new Transaction(
                            amount, 
                            "Tự động từ SMS Ngân hàng", 
                            System.currentTimeMillis(), 
                            9, // Category Khác
                            TransactionType.EXPENSE, 
                            userId
                    );
                    db.transactionDao().insert(autoTx);
                });
            }
        }
    }
}
```

### Bước 3: Đăng ký Receiver tĩnh trong `AndroidManifest.xml`
```xml
<receiver android:name=".util.SMSReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

---

## 3. Kịch bản thực tế 2: Hẹn giờ nhắc nhở người dùng nhập chi tiêu cuối ngày (AlarmManager + BroadcastReceiver)

Ứng dụng cần nhắc nhở người dùng mở app nhập chi tiêu vào lúc 21h00 mỗi ngày để tránh quên.

### Bước 1: Tạo tệp `AlarmReceiver.java` để xử lý đẩy thông báo (Notification)
```java
package com.example.appquanlychitieu.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.appquanlychitieu.MainActivity;
import com.example.appquanlychitieu.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "expense_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo Notification Channel (Yêu cầu bắt buộc từ Android 8.0 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhở chi tiêu",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Định nghĩa hành động khi click vào thông báo (mở MainActivity)
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Xây dựng giao diện thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Icon thông báo
                .setContentTitle("Nhắc nhở chi tiêu")
                .setContentText("Hôm nay bạn đã ghi lại các khoản chi tiêu của mình chưa? Hãy nhập ngay nhé!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Tự biến mất khi nhấn vào

        // Phát thông báo ra màn hình với ID duy nhất
        notificationManager.notify(101, builder.build());
    }
}
```

### Bước 2: Kích hoạt hẹn giờ từ MainActivity hoặc SettingsFragment
```java
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public void setupDailyReminder(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, AlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    // Thiết lập thời điểm báo thức: 21:00 hàng ngày
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.set(Calendar.HOUR_OF_DAY, 21);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);

    // Nếu thời điểm đã qua trong ngày hôm nay, chuyển sang ngày mai
    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_YEAR, 1);
    }

    // Đặt lịch lặp lại hàng ngày (RTC_WAKEUP giúp đánh thức thiết bị nếu đang ngủ để gửi thông báo)
    alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
    );
}
```

---

## 4. Bộ câu hỏi lý thuyết & trả lời mẫu dành cho giảng viên

### Câu hỏi 1: Phương thức `onReceive()` của BroadcastReceiver chạy trên Thread nào? Nó có thể gọi kết nối mạng hoặc đọc ghi file lớn được không?
*   **Trả lời mẫu:** 
    *   Hàm `onReceive()` chạy trực tiếp trên **Main Thread (UI Thread)** của ứng dụng.
    *   Do đó, chúng ta **không được phép** thực hiện các thao tác tốn thời gian như gọi mạng API, đọc ghi file lớn, hay truy vấn database đồng bộ trực tiếp trong hàm này.
    *   Nếu cần làm việc đó, em phải đẩy tác vụ vào một background thread (ví dụ sử dụng `databaseWriteExecutor.execute(...)` như trong code xử lý tin nhắn SMS) hoặc khởi chạy một `WorkManager` / `IntentService` để xử lý ngầm, nhằm tránh việc chặn Main Thread dẫn tới lỗi treo app (ANR).

### Câu hỏi 2: Tại sao Google lại hạn chế việc đăng ký BroadcastReceiver tĩnh (khai báo trong AndroidManifest.xml) từ Android 8.0 trở lên?
*   **Trả lời mẫu:**
    *   Trước Android 8.0, việc đăng ký tĩnh cho phép app tự động khởi chạy khi nhận được các sự kiện hệ thống chung (như bật/tắt wifi, cắm sạc).
    *   Nếu có hàng chục ứng dụng trong máy cùng đăng ký nhận các sự kiện hệ thống này, khi sự kiện xảy ra, hệ điều hành sẽ phải đồng loạt đánh thức và khởi chạy ngầm tất cả các ứng dụng đó. Việc này gây ra hiện tượng ngốn dung lượng RAM đột ngột, làm thiết bị cực kỳ giật lag và hao pin nhanh chóng.
    *   Vì vậy, Google cấm đăng ký tĩnh hầu hết các sự kiện hệ thống, bắt buộc lập trình viên phải đăng ký động trong code để hệ thống chỉ phân phối sự kiện khi ứng dụng thực sự đang hoạt động.

### Câu hỏi 3: Trong kịch bản đọc tin nhắn SMS tự động, dữ liệu thô `pdus` nghĩa là gì?
*   **Trả lời mẫu:**
    *   `pdus` viết tắt của **Protocol Description Unit** (Đơn vị mô tả giao thức). Đây là định dạng thô theo chuẩn quốc tế để mã hóa nội dung tin nhắn SMS truyền tải qua sóng viễn thông.
    *   Khi nhận được mảng dữ liệu byte này trong Bundle, em sử dụng lớp tiện ích `SmsMessage.createFromPdu((byte[]) pdu)` của Android SDK để giải mã byte thô đó thành một đối tượng Java có cấu trúc rõ ràng, từ đó dễ dàng lấy ra nội dung tin nhắn (`getMessageBody()`) và số điện thoại người gửi (`getOriginatingAddress()`).
