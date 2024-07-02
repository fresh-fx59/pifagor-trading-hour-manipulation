package org.example.utils;

import java.time.LocalDateTime;

public class KlineCandleHelper {
    public static boolean isFirstMinuteOfHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0;
    }

    public static boolean isLastMinuteOfHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 59 && dateTime.getSecond() == 0;
    }
}
