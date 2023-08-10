package com.filip.tvscheduler.fmztvscheduler.video;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VideoSimpleLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31m";

    public void log(Level level, String logMessage) {
        String dateTime = LocalDateTime.now().format(formatter);
        String levelColor = (level == Level.ERROR) ? ANSI_RED : ANSI_CYAN;
        String formattedMessage = String.format("[%s] %s%s %s%s", dateTime, levelColor, level.name(), ANSI_RESET, logMessage);
        System.out.println(formattedMessage);
    }

    public enum Level {
        INFO, ERROR
    }
}