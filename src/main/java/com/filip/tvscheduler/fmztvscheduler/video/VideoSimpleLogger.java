package com.filip.tvscheduler.fmztvscheduler.video;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VideoSimpleLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BRIGHT_WHITE = "\u001b[37;1m";

    public void log(Level level, String logMessage) {
        String dateTime = LocalDateTime.now().format(formatter);
        String levelColor;
        switch(level) {
            case ERROR:
                levelColor = ANSI_RED;
                break;
            case RUNNING:
                levelColor = ANSI_RESET;
                break;
            case INFO:
                levelColor = ANSI_CYAN;
                break;
            default:
                levelColor = ANSI_RESET;
                break;
        }

        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement logInvoker = stackTraceElements[2];
        String callerClass = logInvoker.getClassName();
        int callerLineNumber = logInvoker.getLineNumber();

        String formattedMessage = String.format("[%s] %s%s %s%s %s(%s:%d)%s",
                dateTime, levelColor, level.name(), ANSI_RESET, logMessage,
                ANSI_BRIGHT_WHITE, callerClass, callerLineNumber, ANSI_RESET);
        System.out.println(formattedMessage);
    }

    public enum Level {
        INFO, ERROR, RUNNING
    }
}