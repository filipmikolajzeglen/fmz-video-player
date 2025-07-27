package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoSliderStyleEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoTimeFormatEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoMediaPlayer
{
   private static final Logger LOGGER = new Logger();
   private static final int MAX_ERROR_RETRIES = 5;

   private final VideoPlayerView videoPlayerView;
   private final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

   static VideoMediaPlayer of(VideoPlayerView videoPlayerView)
   {
      return new VideoMediaPlayer(videoPlayerView);
   }

   public MediaPlayer createAndSetupPlayer(String videoPath)
   {
      stopAndDisposePlayer(videoPlayerView.getMediaPlayer());

      Media media = new Media(new File(videoPath).toURI().toString());
      MediaPlayer mediaPlayer = new MediaPlayer(media);

      bindControllersTo(mediaPlayer);
      setupEventHandlers(mediaPlayer, videoPath);
      videoPlayerView.getAudioNormalizer().apply(mediaPlayer);

      return mediaPlayer;
   }

   void stopAndDisposePlayer(MediaPlayer mediaPlayer)
   {
      if (mediaPlayer != null)
      {
         videoPlayerView.getVideoVolumeView().unbindVolume();
         if (videoPlayerView.getVideoTimeSliderView() != null)
         {
            videoPlayerView.getVideoTimeSliderView().unbindListeners();
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
      videoPlayerView.getVideoVolumeView().bindToMediaPlayer(mediaPlayer);
      videoPlayerView.getVideoTimeSliderView().bindToMediaPlayer(mediaPlayer);
      videoPlayerView.getVideoMediaSizeEffect().bindToScene();
      videoPlayerView.getVideoPlaybackSpeedView().setMediaPlayer(mediaPlayer);
      VideoSliderStyleEffect.addColorToSlider(videoPlayerView.getSliderTime(),
            videoPlayerView.getSliderTime()::getValue);
      VideoSliderStyleEffect.addDynamicColorListener(videoPlayerView.getSliderTime());
   }

   private void setupEventHandlers(MediaPlayer mediaPlayer, String videoPath)
   {
      mediaPlayer.setOnReady(() -> handleOnReady(mediaPlayer, videoPath));
      mediaPlayer.setOnError(() -> handleOnError(mediaPlayer, videoPath));
      mediaPlayer.setOnEndOfMedia(() -> {
         if (mediaPlayer == videoPlayerView.getMediaPlayer())
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
         videoPlayerView.initializeMediaPlayer(videoPath);
      }
      else
      {
         LOGGER.info("Video is ready to play. Total duration: " +
               VideoTimeFormatEffect.format(mediaPlayer.getTotalDuration()));
         videoPlayerView.playByDefault();
      }
   }

   private void handleOnError(MediaPlayer mediaPlayer, String videoPath)
   {
      videoPlayerView.getAudioNormalizer().stop(mediaPlayer);
      int currentFailures = errorCounts.getOrDefault(videoPath, 0) + 1;

      if (currentFailures < MAX_ERROR_RETRIES)
      {
         errorCounts.put(videoPath, currentFailures);
         LOGGER.error(String.format("Error in media player for '%s'. Attempt %d of %d.",
               videoPath, currentFailures, MAX_ERROR_RETRIES));
         videoPlayerView.initializeMediaPlayer(videoPath);
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
      videoPlayerView.getAudioNormalizer().stop(videoPlayerView.getMediaPlayer());
      videoPlayerView.getVideoPlaybackCoordinator().next();
   }
}