package com.filipmikolajzeglen.fmzvideoplayer.player;

import javafx.beans.property.ObjectProperty;
import javafx.util.Duration;

public interface VideoPlayer
{
   enum Status
   {
      READY, PLAYING, PAUSED, STOPPED, UNKNOWN
   }

   void play();
   void pause();
   void stop();
   void setRate(float rate);
   void seek(Duration duration);
   void setVolume(int volume);
   int getVolume();
   void release();
   void dispose();

   Duration getTotalDuration();
   ObjectProperty<Duration> totalDurationProperty();

   Duration getCurrentTime();
   ObjectProperty<Duration> currentTimeProperty();

   Status getStatus();
   ObjectProperty<Status> statusProperty();

   void setOnReady(Runnable value);
   Runnable getOnReady();
   ObjectProperty<Runnable> onReadyProperty();

   void setOnEndOfMedia(Runnable value);
   Runnable getOnEndOfMedia();
   ObjectProperty<Runnable> onEndOfMediaProperty();

   void setOnError(Runnable value);
   Runnable getOnError();
   ObjectProperty<Runnable> onErrorProperty();
}