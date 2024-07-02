package org.example.util;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.time.ZoneOffset.UTC;

public class TimeHelper {
    public static LocalDateTime fromUnixToLocalDateTimeUtc(@NotNull Long unixTimestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTimestamp), ZoneId.of(UTC.getId()));
    }
}
