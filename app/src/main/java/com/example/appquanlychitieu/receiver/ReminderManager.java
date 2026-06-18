package com.example.appquanlychitieu.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.appquanlychitieu.data.model.Reminder;

import java.util.Calendar;

public class ReminderManager {

    public static void scheduleReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Intent
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_content", reminder.getContent());

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.getId(),
                intent,
                flags
        );

        // Tính toán thời gian
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // Đặt ngày theo mong muốn, nếu số ngày vượt quá tháng hiện tại thì lùi về ngày cuối tháng
        int maxDaysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int targetDay = Math.min(reminder.getDayOfMonth(), maxDaysInCurrentMonth);
        calendar.set(Calendar.DAY_OF_MONTH, targetDay);

        // Nếu thời gian đã qua trong tháng này, chuyển sang tháng sau
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.MONTH, 1);
            // Cập nhật lại ngày cuối tháng cho tháng sau
            int maxDaysInNextMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            int targetDayNextMonth = Math.min(reminder.getDayOfMonth(), maxDaysInNextMonth);
            calendar.set(Calendar.DAY_OF_MONTH, targetDayNextMonth);
        }

        long alarmTime = calendar.getTimeInMillis();
        Log.d("ReminderManager", "Scheduled alarm for reminder " + reminder.getId() + " at " + calendar.getTime().toString());

        // Đặt Alarm
        try {
            boolean canScheduleExact = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                canScheduleExact = alarmManager.canScheduleExactAlarms();
            }

            if (canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            } else if (canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e("ReminderManager", "Exact alarm permission denied. Falling back to inexact.", e);
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
    }

    public static void cancelReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.getId(),
                intent,
                flags
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
