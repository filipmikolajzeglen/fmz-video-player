package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class MediaPlayerManager
{
   private static final Logger LOGGER = new Logger();

   private final VideoPlayer videoPlayer;
   private AudioNormalizer audioNormalizer;

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
      applyAudioNormalization(mediaPlayer);

      return mediaPlayer;
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
      mediaPlayer.setOnEndOfMedia(this::handleOnEndOfMedia);
   }

   private void handleOnReady(MediaPlayer mediaPlayer, String videoPath)
   {
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
      stopAudioNormalization(mediaPlayer);
      LOGGER.error("Error occurred in media player: " + mediaPlayer.getError().getMessage());
      LOGGER.warning("Try to initialize media player once again after error");
      videoPlayer.initializeMediaPlayer(videoPath);
   }

   private void handleOnEndOfMedia()
   {
      LOGGER.info("Video has finished. Loading next video.");
      stopAudioNormalization(videoPlayer.getMediaPlayer());
      Video currentVideo = videoPlayer.getPlaylistManager().getCurrentVideo();
      if (currentVideo != null)
      {
         currentVideo.setWatched(true);
         videoPlayer.getVideoPlayerService().getDatabase().save(currentVideo);
         LOGGER.info("Video marked as watched and saved to database: " + currentVideo.getId());
      }

      Platform.runLater(() -> videoPlayer.getPlaybackController().next());
   }

   private void applyAudioNormalization(MediaPlayer mediaPlayer)
   {
      Video currentVideo = videoPlayer.getPlaylistManager().getCurrentVideo();
      if (currentVideo != null)
      {
         Double normalizedVolume = currentVideo.getAudioNormalizedVolume();
         if (normalizedVolume != null)
         {
            mediaPlayer.setVolume(normalizedVolume);
            LOGGER.info(String.format("Normalized volume was restored from database: %s", normalizedVolume));
         }
         else
         {
            audioNormalizer = AudioNormalizer.of(videoPlayer);
            audioNormalizer.normalize(mediaPlayer);
         }
      }
   }

   void stopAudioNormalization(MediaPlayer mediaPlayer)
   {
      if (mediaPlayer != null && audioNormalizer != null && audioNormalizer.isNormalizing())
      {
         audioNormalizer.reset(mediaPlayer);
         LOGGER.info("Audio normalization was stopped and cleaned for current episode.");
      }
   }

   private void stopAndDisposePlayer(MediaPlayer mediaPlayer)
   {
      if (mediaPlayer != null)
      {
         videoPlayer.getVolumeController().unbindVolume();
         if (videoPlayer.getSliderController() != null)
         {
            videoPlayer.getSliderController().unbindListeners();
         }
         mediaPlayer.setAudioSpectrumListener(null);
         mediaPlayer.stop();
         mediaPlayer.dispose();
         LOGGER.info("Listeners were removed. Media player was stopped and disposed.");
      }
   }
}