package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class MediaPlayerManager
{
   private static final Logger LOGGER = new Logger();
   private static final int MAX_ERROR_RETRIES = 3;

   private final VideoPlayer videoPlayer;
   private final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

   static MediaPlayerManager of(VideoPlayer videoPlayer)
   {
      return new MediaPlayerManager(videoPlayer);
   }

   MediaPlayer createAndSetupPlayer(String videoPath)
   {
      stopAndDisposePlayer(videoPlayer.getMediaPlayer());

      Media media = new Media(new File(videoPath).toURI().toString());
      MediaPlayer mediaPlayer = new MediaPlayer(media);

      bindControllersTo(mediaPlayer);
      setupEventHandlers(mediaPlayer, videoPath);
      videoPlayer.getAudioNormalizationManager().apply(mediaPlayer);

      return mediaPlayer;
   }

   void stopAndDisposePlayer(MediaPlayer mediaPlayer)
   {
      if (mediaPlayer != null)
      {
         videoPlayer.getVolumeController().unbindVolume();
         if (videoPlayer.getSliderController() != null)
         {
            videoPlayer.getSliderController().unbindListeners();
         }
         mediaPlayer.setOnReady(null);
         mediaPlayer.setOnError(null);
         mediaPlayer.setOnEndOfMedia(null);
         mediaPlayer.setAudioSpectrumListener(null);
         mediaPlayer.stop();
         mediaPlayer.dispose();
         LOGGER.info("Listeners were removed. Media player was stopped and disposed.");
      }
   }

   private void bindControllersTo(MediaPlayer mediaPlayer)
   {
      videoPlayer.getVolumeController().bindToMediaPlayer(mediaPlayer);
      videoPlayer.getSliderController().bindToMediaPlayer(mediaPlayer);
      videoPlayer.getMediaViewResizer().bindToScene();
      videoPlayer.getPlaybackSpeedController().setMediaPlayer(mediaPlayer);
      SliderStyler.addColorToSlider(videoPlayer.getSliderTime(), videoPlayer.getSliderTime()::getValue);
      SliderStyler.addDynamicColorListener(videoPlayer.getSliderTime());
   }

   private void setupEventHandlers(MediaPlayer mediaPlayer, String videoPath)
   {
      mediaPlayer.setOnReady(() -> handleOnReady(mediaPlayer, videoPath));
      mediaPlayer.setOnError(() -> handleOnError(mediaPlayer, videoPath));
      mediaPlayer.setOnEndOfMedia(() -> {
         if (mediaPlayer == videoPlayer.getMediaPlayer())
         {
            handleOnEndOfMedia();
         }
         else
         {
            LOGGER.warning("onEndOfMedia event received for a disposed or old media player. Ignoring.");
         }
      });
   }

   private void handleOnReady(MediaPlayer mediaPlayer, String videoPath)
   {
      errorCounts.remove(videoPath);
      if (mediaPlayer.getTotalDuration().lessThanOrEqualTo(Duration.ZERO))
      {
         LOGGER.error("Total duration was 00:00 - Started new initialization");
         videoPlayer.initializeMediaPlayer(videoPath);
      }
      else
      {
         LOGGER.info("Video is ready to play. Total duration: " +
               TimeFormatter.format(mediaPlayer.getTotalDuration()));
         videoPlayer.playByDefault();
      }
   }

   private void handleOnError(MediaPlayer mediaPlayer, String videoPath)
   {
      videoPlayer.getAudioNormalizationManager().stop(mediaPlayer);
      int currentFailures = errorCounts.getOrDefault(videoPath, 0) + 1;

      if (currentFailures < MAX_ERROR_RETRIES)
      {
         errorCounts.put(videoPath, currentFailures);
         LOGGER.error(String.format("Error in media player for '%s'. Attempt %d of %d.",
               videoPath, currentFailures, MAX_ERROR_RETRIES));
         videoPlayer.initializeMediaPlayer(videoPath);
      }
      else
      {
         LOGGER.error(String.format("Failed to play '%s' after %d attempts. Skipping file.",
               videoPath, MAX_ERROR_RETRIES));
         errorCounts.remove(videoPath);
         handleOnEndOfMedia();
      }
   }

   private void handleOnEndOfMedia()
   {
      LOGGER.info("Video has finished. Loading next video.");
      videoPlayer.getAudioNormalizationManager().stop(videoPlayer.getMediaPlayer());
      videoPlayer.getPlaybackController().next();
   }
}