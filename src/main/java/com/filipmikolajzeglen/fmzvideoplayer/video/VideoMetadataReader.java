package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.humble.video.Demuxer;
import io.humble.video.Global;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoMetadataReader
{
   public static long getDurationInSeconds(File videoFile)
   {
      if (videoFile == null || !videoFile.exists())
      {
         return 0L;
      }

      Demuxer demuxer = null;
      try
      {
         demuxer = Demuxer.make();
         demuxer.open(videoFile.getAbsolutePath(), null, false, true, null, null);
         long duration = demuxer.getDuration();
         if (duration != Global.NO_PTS)
         {
            return TimeUnit.MICROSECONDS.toSeconds(duration);
         }
         return 0L;
      }
      catch (IOException | InterruptedException e)
      {
         log.error("Error getting duration for file: {}", videoFile.getAbsolutePath(), e);
         if (e instanceof InterruptedException)
         {
            Thread.currentThread().interrupt();
         }
         return 0L;
      }
      finally
      {
         if (demuxer != null)
         {
            try
            {
               demuxer.close();
            }
            catch (IOException | InterruptedException e)
            {
               log.error("Error closing demuxer for file: {}", videoFile.getAbsolutePath(), e);
               if (e instanceof InterruptedException)
               {
                  Thread.currentThread().interrupt();
               }
            }
         }
      }
   }
}