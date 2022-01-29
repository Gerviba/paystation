package hu.schbme.pay.station.config;

import java.text.SimpleDateFormat;

public final class AppUtil {

    private AppUtil() {
    }

    public static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMATTER = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static final ThreadLocal<SimpleDateFormat> DATE_TIME_FILE_FORMATTER = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"));

    public static final ThreadLocal<SimpleDateFormat> DATE_ONLY_FORMATTER = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    public static final ThreadLocal<SimpleDateFormat> TIME_ONLY_FORMATTER = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("HH:mm:ss"));

    public static String formatNumber(long number) {
        var nums = String.valueOf(number);
        var result = new StringBuilder();
        for (int i = nums.length() - 1; i >= 0; i--) {
            result.insert(0, ((nums.length() - i) % 3 == 0 ? " " : "") + nums.charAt(i));
        }
        return result.toString();
    }

}
