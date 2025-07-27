package com.filipmikolajzeglen.fmzvideoplayer.video.audio;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.video.Video;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.scene.media.MediaPlayer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AudioNormalizer
{
   private static final Logger LOGGER = new Logger();

   private final VideoPlayerView videoPlayerView;
   private AudioNormalizationAlgorithm audioNormalizationAlgorithm;

   public static AudioNormalizer of(VideoPlayerView videoPlayerView)
   {
      return new AudioNormalizer(videoPlayerView);
   }

   public void apply(MediaPlayer mediaPlayer)
   {
      if (videoPlayerView.isPlayingCommercial())
      {
         return;
      }

      Video currentVideo = videoPlayerView.getVideoPlaylist().getCurrentVideo();
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
            audioNormalizationAlgorithm = AudioNormalizationAlgorithm.of(videoPlayerView);
            audioNormalizationAlgorithm.normalize(mediaPlayer);
         }
      }
   }

   public void stop(MediaPlayer mediaPlayer)
   {
      if (mediaPlayer != null && audioNormalizationAlgorithm != null && audioNormalizationAlgorithm.isNormalizing())
      {
         audioNormalizationAlgorithm.reset(mediaPlayer);
         LOGGER.info("Audio normalization was stopped and cleaned for current episode.");
         audioNormalizationAlgorithm = null;
      }
   }
}
