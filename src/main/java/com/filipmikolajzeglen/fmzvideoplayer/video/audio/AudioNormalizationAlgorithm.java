package com.filipmikolajzeglen.fmzvideoplayer.video.audio;

import java.util.List;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.video.Video;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoService;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoIconsEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class AudioNormalizationAlgorithm
{
   private static final Logger LOGGER = new Logger();
   private static final int NORMALIZATION_SAMPLE_COUNT = 500;
   private static final double TARGET_VOLUME = 0.5;

   private final VideoPlayerView videoPlayerView;
   private final Animator animator;
   private AudioSpectrumListener currentListener;

   private boolean normalizing = false;

   static AudioNormalizationAlgorithm of(VideoPlayerView videoPlayerView)
   {
      return new AudioNormalizationAlgorithm(videoPlayerView, Animator.of(videoPlayerView));
   }

   void normalize(MediaPlayer mediaPlayer)
   {
      Video video = videoPlayerView.getVideoPlaylist().getCurrentVideo();
      VideoService videoService = videoPlayerView.getVideoService();

      if (!validateComponents(video, videoService))
      {
         return;
      }

      startNormalization();
      currentListener = createAudioSpectrumListener(mediaPlayer, video, videoService);
      mediaPlayer.setAudioSpectrumListener(currentListener);
   }

   private boolean validateComponents(Video video, VideoService videoService)
   {
      if (video == null || videoService == null)
      {
         LOGGER.error("Cannot normalize audio: missing Video or Service component.");
         return false;
      }
      return true;
   }

   private void startNormalization()
   {
      LOGGER.running("Volume is normalizing for current video...");
      normalizing = true;
      animator.start();
   }

   private AudioSpectrumListener createAudioSpectrumListener(MediaPlayer mediaPlayer, Video video,
         VideoService videoService)
   {
      return new AudioSpectrumListener()
      {
         private int sampleCount = 0;
         private double sumMagnitude = 0;

         @Override
         public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases)
         {
            double avg = calculateAverageMagnitude(magnitudes);
            sumMagnitude += avg;
            sampleCount++;

            if (sampleCount >= NORMALIZATION_SAMPLE_COUNT)
            {
               double normalizedVolume = calculateNormalizedVolume(sumMagnitude, sampleCount);
               handleNormalizationComplete(mediaPlayer, video, videoService, normalizedVolume);
            }
         }
      };
   }

   private double calculateAverageMagnitude(float[] magnitudes)
   {
      double avg = 0;
      for (float m : magnitudes)
      {
         avg += m;
      }
      return avg / magnitudes.length;
   }

   private double calculateNormalizedVolume(double sumMagnitude, int sampleCount)
   {
      double mean = sumMagnitude / sampleCount;
      double correction = TARGET_VOLUME - (mean / NORMALIZATION_SAMPLE_COUNT);
      return Math.max(0, Math.min(1, correction));
   }

   private void handleNormalizationComplete(MediaPlayer mediaPlayer, Video video, VideoService videoService,
         double normalizedVolume)
   {
      normalizing = false;
      animator.stop();

      mediaPlayer.setVolume(normalizedVolume);
      video.setAudioNormalizedVolume(normalizedVolume);
      videoService.getDatabase().update(video);
      mediaPlayer.setAudioSpectrumListener(null);
      currentListener = null;

      LOGGER.info(String.format("Normalized volume was saved to database: %s", normalizedVolume));
   }

   void reset(MediaPlayer mediaPlayer)
   {
      animator.stop();
      if (currentListener != null)
      {
         mediaPlayer.setAudioSpectrumListener(null);
         currentListener = null;
      }
   }

   @RequiredArgsConstructor
   static class Animator
   {
      private final Label volumeLabel;
      private final SVGPath volumeLabelSvg;
      private Timeline normalizationAnimation;
      private int iconIndex = 0;

      static Animator of(VideoPlayerView videoPlayerView)
      {
         return new Animator(videoPlayerView.getLabelVolume(), videoPlayerView.getLabelVolumeSVG());
      }

      private static final List<String> ICON_PATHS = List.of(
            PlayerConstants.Icons.NORMALIZE1(), PlayerConstants.Icons.NORMALIZE2(),
            PlayerConstants.Icons.NORMALIZE3(), PlayerConstants.Icons.NORMALIZE4(),
            PlayerConstants.Icons.NORMALIZE5(), PlayerConstants.Icons.NORMALIZE6(),
            PlayerConstants.Icons.NORMALIZE7(), PlayerConstants.Icons.NORMALIZE8(),
            PlayerConstants.Icons.NORMALIZE9(), PlayerConstants.Icons.NORMALIZE10(),
            PlayerConstants.Icons.NORMALIZE11()
      );

      void start()
      {
         stop();
         normalizationAnimation = new Timeline(
               new KeyFrame(Duration.millis(150), e -> toggleIcon())
         );
         normalizationAnimation.setCycleCount(Timeline.INDEFINITE);
         normalizationAnimation.play();
      }

      void stop()
      {
         if (normalizationAnimation != null)
         {
            normalizationAnimation.stop();
            normalizationAnimation = null;
         }
         iconIndex = 0;
      }

      void toggleIcon()
      {
         String iconPath = ICON_PATHS.get(iconIndex);
         VideoIconsEffect.setControlSVG(volumeLabel, volumeLabelSvg, iconPath);
         iconIndex = (iconIndex + 1) % ICON_PATHS.size();
      }
   }
}