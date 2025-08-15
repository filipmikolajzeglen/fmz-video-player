package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.player.VideoPlayer;
import com.filipmikolajzeglen.fmzvideoplayer.player.VLCJPlayer;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoSliderStyleEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoTimeFormatEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
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

   public VideoPlayer createAndSetupPlayer(String videoPath)
   {
      stopAndDisposePlayer(videoPlayerView.getMediaPlayer());

      // Utwórz EmbeddedMediaPlayer i ustaw powierzchnię na ImageView
      ImageView imageView = videoPlayerView.getImageView();
      MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
      EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
      embeddedMediaPlayer.videoSurface().set(new ImageViewVideoSurface(imageView));
      embeddedMediaPlayer.media().play(videoPath); // <-- zmiana z startPaused na play

      VideoPlayer mediaPlayer = new VLCJPlayer(embeddedMediaPlayer);

      bindControllersTo(mediaPlayer);
      setupEventHandlers(mediaPlayer, videoPath);

      return mediaPlayer;
   }

   void stopAndDisposePlayer(VideoPlayer mediaPlayer)
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
         mediaPlayer.stop();
         mediaPlayer.dispose();
         LOGGER.info("Listeners were removed. Media player was stopped and disposed.");
      }
   }

   private void bindControllersTo(VideoPlayer mediaPlayer)
   {
      videoPlayerView.getVideoVolumeView().bindToMediaPlayer(mediaPlayer);
      videoPlayerView.getVideoTimeSliderView().bindToMediaPlayer(mediaPlayer);
      videoPlayerView.getVideoMediaSizeEffect().bindToScene();
      // videoPlayerView.getVideoPlaybackSpeedView().setMediaPlayer(mediaPlayer);
      VideoSliderStyleEffect.addColorToSlider(videoPlayerView.getSliderTime(),
            videoPlayerView.getSliderTime()::getValue);
      VideoSliderStyleEffect.addDynamicColorListener(videoPlayerView.getSliderTime());
   }

   private void setupEventHandlers(VideoPlayer mediaPlayer, String videoPath)
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

   private void handleOnReady(VideoPlayer mediaPlayer, String videoPath)
   {
      errorCounts.remove(videoPath);
      if (mediaPlayer.getTotalDuration().lessThanOrEqualTo(javafx.util.Duration.ZERO))
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

   private void handleOnError(VideoPlayer mediaPlayer, String videoPath)
   {
      // videoPlayerView.getAudioNormalizer().stop(mediaPlayer);
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
      // videoPlayerView.getAudioNormalizer().stop(videoPlayerView.getMediaPlayer());
      videoPlayerView.getVideoPlaybackCoordinator().next();
   }
}