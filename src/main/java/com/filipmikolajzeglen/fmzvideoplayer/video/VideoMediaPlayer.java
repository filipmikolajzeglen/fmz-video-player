package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoSliderStyleEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoTimeFormatEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoMediaPlayer
{
   private static final int MAX_ERROR_RETRIES = 5;

   private final VideoPlayerView videoPlayerView;
   private final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

   static VideoMediaPlayer of(VideoPlayerView videoPlayerView)
   {
      return new VideoMediaPlayer(videoPlayerView);
   }

   public VideoPlayer createAndSetupPlayer(String videoPath)
   {
      stopAndDisposePlayer(videoPlayerView.getVideoPlayer());

      ImageView imageView = videoPlayerView.getImageView();
      MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
      EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
      embeddedMediaPlayer.videoSurface().set(new ImageViewVideoSurface(imageView));
      embeddedMediaPlayer.media().play(videoPath);

      VideoPlayer videoPlayer = new VLCJPlayer(embeddedMediaPlayer);

      bindControllersTo(videoPlayer);
      setupEventHandlers(videoPlayer, videoPath);

      return videoPlayer;
   }

   void stopAndDisposePlayer(VideoPlayer videoPlayer)
   {
      if (videoPlayer != null)
      {
         videoPlayerView.getVideoVolumeView().unbindVolume();
         if (videoPlayerView.getVideoTimeSliderView() != null)
         {
            videoPlayerView.getVideoTimeSliderView().unbindListeners();
         }
         videoPlayer.setOnReady(null);
         videoPlayer.setOnError(null);
         videoPlayer.setOnEndOfMedia(null);
         videoPlayer.stop();
         videoPlayer.dispose();
         log.info("Stopping and disposing media player: {}", videoPlayer.getClass().getSimpleName());
      }
   }

   private void bindControllersTo(VideoPlayer mediaPlayer)
   {
      videoPlayerView.getVideoVolumeView().bindToVideoPlayer(mediaPlayer);
      videoPlayerView.getVideoTimeSliderView().bindToMediaPlayer(mediaPlayer);
      videoPlayerView.getVideoMediaSizeEffect().bindToScene();
      VideoSliderStyleEffect.addColorToSlider(videoPlayerView.getSliderTime(),
            videoPlayerView.getSliderTime()::getValue);
      VideoSliderStyleEffect.addDynamicColorListener(videoPlayerView.getSliderTime());
   }

   private void setupEventHandlers(VideoPlayer videoPlayer, String videoPath)
   {
      videoPlayer.setOnReady(() -> handleOnReady(videoPlayer, videoPath));
      videoPlayer.setOnError(() -> handleOnError(videoPath));
      videoPlayer.setOnEndOfMedia(() -> {
         if (videoPlayer == videoPlayerView.getVideoPlayer())
         {
            handleOnEndOfMedia();
         }
         else
         {
            log.warn("onEndOfMedia event received for a disposed or old media player. Ignoring.");
         }
      });
   }

   private void handleOnReady(VideoPlayer videoPlayer, String videoPath)
   {
      errorCounts.remove(videoPath);
      if (videoPlayer.getTotalDuration().lessThanOrEqualTo(javafx.util.Duration.ZERO))
      {
         log.error("Total duration was 00:00 - Started new initialization");
         videoPlayerView.initializeVideoPlayer(videoPath);
      }
      else
      {
         log.info("Video player is ready. Total duration: {}",
               VideoTimeFormatEffect.format(videoPlayer.getTotalDuration()));
         videoPlayerView.playByDefault();
      }
   }

   private void handleOnError(String videoPath)
   {
      // videoPlayerView.getAudioNormalizer().stop(mediaPlayer);
      int currentFailures = errorCounts.getOrDefault(videoPath, 0) + 1;

      if (currentFailures < MAX_ERROR_RETRIES)
      {
         errorCounts.put(videoPath, currentFailures);
         log.error("Error in media player for '{}'. Attempt {} of {}.", videoPath, currentFailures, MAX_ERROR_RETRIES);
         videoPlayerView.initializeVideoPlayer(videoPath);
      }
      else
      {
         log.error("Failed to play '{}' after {} attempts. Skipping file.", videoPath, MAX_ERROR_RETRIES);
         errorCounts.remove(videoPath);
         handleOnEndOfMedia();
      }
   }

   private void handleOnEndOfMedia()
   {
      log.info("Video has finished. Loading next video.");
      // videoPlayerView.getAudioNormalizer().stop(videoPlayerView.getMediaPlayer());
      videoPlayerView.getVideoPlaybackCoordinator().next();
   }
}