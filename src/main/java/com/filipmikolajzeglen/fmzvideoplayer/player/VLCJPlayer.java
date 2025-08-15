package com.filipmikolajzeglen.fmzvideoplayer.player;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCJPlayer implements VideoPlayer {

   private final EmbeddedMediaPlayer embeddedMediaPlayer;

   private final ObjectProperty<Duration> totalDuration = new SimpleObjectProperty<>(Duration.ZERO);
   private final ObjectProperty<Duration> currentTime = new SimpleObjectProperty<>(Duration.ZERO);
   private final ObjectProperty<Runnable> onReady = new SimpleObjectProperty<>();
   private final ObjectProperty<Runnable> onEndOfMedia = new SimpleObjectProperty<>();
   private final ObjectProperty<Runnable> onError = new SimpleObjectProperty<>();
   private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.UNKNOWN);

   public VLCJPlayer(EmbeddedMediaPlayer embeddedMediaPlayer) {
      this.embeddedMediaPlayer = embeddedMediaPlayer;
      addListeners();
   }

   private void addListeners() {
      embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
         @Override
         public void lengthChanged(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, long newLength) {
            javafx.application.Platform.runLater(() -> totalDuration.set(Duration.millis(newLength)));
         }

         @Override
         public void timeChanged(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, long newTime) {
            javafx.application.Platform.runLater(() -> currentTime.set(Duration.millis(newTime)));
         }

         @Override
         public void mediaPlayerReady(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
            javafx.application.Platform.runLater(() -> {
               status.set(Status.READY);
               if (getOnReady() != null) {
                  getOnReady().run();
               }
            });
         }

         @Override
         public void playing(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
            javafx.application.Platform.runLater(() -> status.set(Status.PLAYING));
         }

         @Override
         public void paused(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
            javafx.application.Platform.runLater(() -> status.set(Status.PAUSED));
         }

         @Override
         public void stopped(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
            javafx.application.Platform.runLater(() -> status.set(Status.STOPPED));
         }

         @Override
         public void finished(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
            javafx.application.Platform.runLater(() -> {
               if (getOnEndOfMedia() != null) {
                  getOnEndOfMedia().run();
               }
            });
         }

         @Override
         public void error(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
            javafx.application.Platform.runLater(() -> {
               if (getOnError() != null) {
                  getOnError().run();
               }
            });
         }
      });
   }

   // --- Implementacja interfejsu VideoPlayer ---
   @Override
   public void play() {
      embeddedMediaPlayer.controls().play();
   }

   @Override
   public void pause() {
      embeddedMediaPlayer.controls().pause();
   }

   @Override
   public void stop() {
      embeddedMediaPlayer.controls().stop();
   }

   @Override
   public void setRate(float rate) {
      embeddedMediaPlayer.controls().setRate(rate);
   }

   @Override
   public void seek(Duration duration) {
      embeddedMediaPlayer.controls().setTime((long) duration.toMillis());
   }

   @Override
   public void setVolume(int volume) {
      embeddedMediaPlayer.audio().setVolume(volume);
   }

   @Override
   public int getVolume() {
      return embeddedMediaPlayer.audio().volume();
   }

   @Override
   public void release() {
      embeddedMediaPlayer.release();
   }

   @Override
   public void dispose() {
      release();
   }

   @Override
   public Duration getTotalDuration() {
      return totalDuration.get();
   }

   @Override
   public ObjectProperty<Duration> totalDurationProperty() {
      return totalDuration;
   }

   @Override
   public Duration getCurrentTime() {
      return currentTime.get();
   }

   @Override
   public ObjectProperty<Duration> currentTimeProperty() {
      return currentTime;
   }

   @Override
   public Status getStatus() {
      return status.get();
   }

   @Override
   public ObjectProperty<Status> statusProperty() {
      return status;
   }

   @Override
   public void setOnReady(Runnable value) {
      onReady.set(value);
   }

   @Override
   public Runnable getOnReady() {
      return onReady.get();
   }

   @Override
   public ObjectProperty<Runnable> onReadyProperty() {
      return onReady;
   }

   @Override
   public void setOnEndOfMedia(Runnable value) {
      onEndOfMedia.set(value);
   }

   @Override
   public Runnable getOnEndOfMedia() {
      return onEndOfMedia.get();
   }

   @Override
   public ObjectProperty<Runnable> onEndOfMediaProperty() {
      return onEndOfMedia;
   }

   @Override
   public void setOnError(Runnable value) {
      onError.set(value);
   }

   @Override
   public Runnable getOnError() {
      return onError.get();
   }

   @Override
   public ObjectProperty<Runnable> onErrorProperty() {
      return onError;
   }
}