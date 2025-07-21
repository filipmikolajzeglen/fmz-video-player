package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.List;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
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
class AudioNormalizer
{
   private static final Logger LOGGER = new Logger();
   private static final int NORMALIZATION_SAMPLE_COUNT = 500;

   private final VideoPlayer videoPlayer;
   private final Animator animator;
   private AudioSpectrumListener currentListener;

   private boolean normalizing = false;

   static AudioNormalizer of(VideoPlayer videoPlayer)
   {
      return new AudioNormalizer(videoPlayer, Animator.of(videoPlayer));
   }

   void normalize(MediaPlayer mediaPlayer)
   {
      Video video = videoPlayer.getPlaylistManager().getCurrentVideo();
      VideoPlayerService videoPlayerService = videoPlayer.getVideoPlayerService();

      if (!validateComponents(video, videoPlayerService))
      {
         return;
      }

      startNormalization();
      currentListener = createAudioSpectrumListener(mediaPlayer, video, videoPlayerService);
      mediaPlayer.setAudioSpectrumListener(currentListener);
   }

   private boolean validateComponents(Video video, VideoPlayerService videoPlayerService)
   {
      if (video == null || videoPlayerService == null)
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
         VideoPlayerService videoPlayerService)
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
               handleNormalizationComplete(mediaPlayer, video, videoPlayerService, normalizedVolume);
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
      double targetVolume = 0.5;
      double correction = targetVolume - (mean / NORMALIZATION_SAMPLE_COUNT);
      return Math.max(0, Math.min(1, correction));
   }

   private void handleNormalizationComplete(MediaPlayer mediaPlayer, Video video, VideoPlayerService videoPlayerService,
         double normalizedVolume)
   {
      normalizing = false;
      animator.stop();

      mediaPlayer.setVolume(normalizedVolume);
      video.setAudioNormalizedVolume(normalizedVolume);
      videoPlayerService.getDatabase().save(video);
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

      static Animator of(VideoPlayer videoPlayer)
      {
         return new Animator(videoPlayer.getLabelVolume(), videoPlayer.getLabelVolumeSVG());
      }

      private static final List<String> ICON_PATHS = List.of(
            FMZVideoPlayerConfiguration.Icons.NORMALIZE1(), FMZVideoPlayerConfiguration.Icons.NORMALIZE2(),
            FMZVideoPlayerConfiguration.Icons.NORMALIZE3(), FMZVideoPlayerConfiguration.Icons.NORMALIZE4(),
            FMZVideoPlayerConfiguration.Icons.NORMALIZE5(), FMZVideoPlayerConfiguration.Icons.NORMALIZE6(),
            FMZVideoPlayerConfiguration.Icons.NORMALIZE7(), FMZVideoPlayerConfiguration.Icons.NORMALIZE8(),
            FMZVideoPlayerConfiguration.Icons.NORMALIZE9(), FMZVideoPlayerConfiguration.Icons.NORMALIZE10(),
            FMZVideoPlayerConfiguration.Icons.NORMALIZE11()
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
         VideoPlayerIcons.setLabelVolumeSVG(volumeLabel, volumeLabelSvg, iconPath, 0.0);
         iconIndex = (iconIndex + 1) % ICON_PATHS.size();
      }
   }
}