package com.zpedroo.multieconomy.utils.formatter;

import java.util.concurrent.TimeUnit;

import static com.zpedroo.multieconomy.utils.config.TimeTranslations.*;

public class TimeFormatter {

    public static String millisToFormattedTime(long timeInMillis) {
        StringBuilder builder = new StringBuilder();

        Long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis) - (TimeUnit.MILLISECONDS.toDays(timeInMillis) * 24);
        Long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - (TimeUnit.MILLISECONDS.toHours(timeInMillis) * 60);
        Long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - (TimeUnit.MILLISECONDS.toMinutes(timeInMillis) * 60);

        if (hours > 0) builder.append(hours).append(" ").append(hours == 1 ? HOUR : HOURS).append(" ");
        if (minutes > 0) builder.append(minutes).append(" ").append(minutes == 1 ? MINUTE : MINUTES).append(" ");
        if (seconds > 0) builder.append(seconds).append(" ").append(seconds == 1 ? SECOND : SECONDS);

        return builder.toString().isEmpty() ? NOW : builder.toString();
    }
}