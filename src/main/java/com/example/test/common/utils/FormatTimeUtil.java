package com.example.test.common.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatTimeUtil {
    public static String formatTime(long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return time.format(date);
    }

    public static long formatTime(Timestamp timestamp) {
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return Long.parseLong(time.format(date));
    }

    public static Timestamp formatTimestampTime(long currentTime) {
        return new Timestamp(currentTime);
    }
}
