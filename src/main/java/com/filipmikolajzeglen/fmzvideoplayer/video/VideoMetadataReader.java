package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.io.IOException;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;

public class VideoMetadataReader
{
   private static final Logger LOGGER = new Logger();

   public static long getDurationInSeconds(File videoFile)
   {
      if (videoFile == null || !videoFile.exists())
      {
         return 0L;
      }
      try
      {
         FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));
         double duration = frameGrab.getVideoTrack().getMeta().getTotalDuration();
         return (long) duration;
      }
      catch (IOException | JCodecException e)
      {
         LOGGER.error("Error getting duration for file: " + videoFile.getAbsolutePath());
         return 0L;
      }
   }
}
