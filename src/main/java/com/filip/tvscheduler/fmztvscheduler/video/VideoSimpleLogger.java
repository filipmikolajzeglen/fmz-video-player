package com.filip.tvscheduler.fmztvscheduler.video;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple logging class for videos.
 * <p>
 * The {@code VideoSimpleLogger} class provides logging functionality with
 * color-coded severity levels and automatic inclusion of the calling class and
 * line number. It uses a {@code StackTraceElement} to identify the caller.
 * <p>
 * The logger formats messages as follows:
 * [timestamp] level message (caller:line)
 * <p>
 * For example, a possible output might be:
 * [2023-11-19 13:47:30] INFO Some log message (some.package.SomeClass:42)
 * <p>
 * Logs can be created at four severity levels:
 * {@code INFO, ERROR, RUNNING, WARNING}, which are color-coded accordingly.
 */
public class VideoSimpleLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BRIGHT_WHITE = "\u001b[37;1m";
    private static final int LOG_INVOKER_INDEX = 2;

    public void log(Level level, String logMessage) {
        String dateTime = LocalDateTime.now().format(formatter);
        String levelColor = level.getColorCode();

        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement logInvoker = stackTraceElements[LOG_INVOKER_INDEX];
        String callerClass = logInvoker.getClassName();
        int callerLineNumber = logInvoker.getLineNumber();

        String formattedMessage = String.format("[%s] %s%s %s%s %s(%s:%d)%s",
                dateTime, levelColor, level.name(), ANSI_RESET, logMessage,
                ANSI_BRIGHT_WHITE, callerClass, callerLineNumber, ANSI_RESET);
        System.out.println(formattedMessage);
    }

    public enum Level {
        INFO("\u001B[36m"),
        ERROR("\u001B[31m"),
        RUNNING("\u001B[0m"),
        WARNING("\u001B[33m");

        private final String colorCode;

        Level(String colorCode) {
            this.colorCode = colorCode;
        }

        public String getColorCode() {
            return colorCode;
        }
    }
}