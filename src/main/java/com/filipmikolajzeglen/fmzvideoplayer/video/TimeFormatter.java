package com.filipmikolajzeglen.fmzvideoplayer.video;

import javafx.util.Duration;

class TimeFormatter
{
   private static final int SECONDS_IN_HOUR = 3600;
   private static final int SECONDS_IN_MINUTE = 60;
   private static final String DEFAULT_FORMAT = "00:00";
   private static final String FORMAT_HOUR_MINUTE_SECOND = "%02d:%02d:%02d";
   private static final String FORMAT_MINUTE_SECOND = "%02d:%02d";

   static String format(Duration time)
   {
      if (time == null)
      {
         return DEFAULT_FORMAT;
      }
      int totalSeconds = (int) Math.floor(time.toSeconds());
      int hours = totalSeconds / SECONDS_IN_HOUR;
      int minutes = (totalSeconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
      int seconds = totalSeconds % SECONDS_IN_MINUTE;
      return hours > 0
            ? String.format(FORMAT_HOUR_MINUTE_SECOND, hours, minutes, seconds)
            : String.format(FORMAT_MINUTE_SECOND, minutes, seconds);
   }
}