package com.example.appquanlychitieu.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final Locale VI = new Locale("vi", "VN");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", VI);
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("yyyy-MM", VI);
    private static final SimpleDateFormat DISPLAY_MONTH_FORMAT = new SimpleDateFormat("'Tháng' MM/yyyy", VI);
    private static final SimpleDateFormat DAY_MONTH_FORMAT = new SimpleDateFormat("dd 'thg' MM", VI);

    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String formatMonthYear(long timestamp) {
        return MONTH_YEAR_FORMAT.format(new Date(timestamp));
    }

    public static String formatDisplayMonth(long timestamp) {
        return DISPLAY_MONTH_FORMAT.format(new Date(timestamp));
    }

    public static String formatDayMonth(long timestamp) {
        return DAY_MONTH_FORMAT.format(new Date(timestamp));
    }

    public static String getCurrentMonthYear() {
        return MONTH_YEAR_FORMAT.format(new Date());
    }

    public static long getStartOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }

    public static long getStartOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    public static long getEndOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(long timestamp) {
        return isSameDay(timestamp, System.currentTimeMillis());
    }

    public static boolean isYesterday(long timestamp) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(timestamp, yesterday.getTimeInMillis());
    }

    public static String getRelativeDateLabel(long timestamp) {
        if (isToday(timestamp)) return "Hôm nay";
        if (isYesterday(timestamp)) return "Hôm qua";
        return formatDate(timestamp);
    }
}
