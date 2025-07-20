package com.filipmikolajzeglen.fmzvideoplayer.logger;

import static com.filipmikolajzeglen.fmzvideoplayer.logger.LoggerLevel.ERROR;
import static com.filipmikolajzeglen.fmzvideoplayer.logger.LoggerLevel.INFO;
import static com.filipmikolajzeglen.fmzvideoplayer.logger.LoggerLevel.RUNNING;
import static com.filipmikolajzeglen.fmzvideoplayer.logger.LoggerLevel.WARNING;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple logging class for videos.
 * <p>
 * The {@code VideoSimpleLogger} class provides logging functionality with color-coded severity levels and automatic
 * inclusion of the calling class and line number. It uses a {@code StackTraceElement} to identify the caller.
 * <p>
 * The logger formats messages as follows: [timestamp] level message (caller:line)
 * <p>
 * For example, a possible output might be: [2023-11-19 13:47:30] INFO Some log message (some.package.SomeClass:42)
 * <p>
 * Logs can be created at four severity levels: {@code INFO, ERROR, RUNNING, WARNING}, which are color-coded
 * accordingly.
 */
public class Logger
{
   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   private static final String ANSI_RESET = "\u001B[0m";
   private static final String ANSI_BRIGHT_WHITE = "\u001b[37;1m";
   private static final int LOG_INVOKER_INDEX = 3;

   public void info(String logMessage)
   {
      log(INFO, logMessage);
   }

   public void warning(String logMessage)
   {
      log(WARNING, logMessage);
   }

   public void running(String logMessage)
   {
      log(RUNNING, logMessage);
   }

   public void error(String logMessage)
   {
      log(ERROR, logMessage);
   }

   private void log(LoggerLevel level, String logMessage)
   {
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
}