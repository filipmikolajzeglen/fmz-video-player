package com.filipmikolajzeglen.fmzvideoplayer.video;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.scene.media.MediaPlayer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class AudioNormalizationManager
{
   private static final Logger LOGGER = new Logger();

   private final VideoPlayer videoPlayer;
   private AudioNormalizer audioNormalizer;

   static AudioNormalizationManager of(VideoPlayer videoPlayer)
   {
      return new AudioNormalizationManager(videoPlayer);
   }

   void apply(MediaPlayer mediaPlayer)
   {
      if (videoPlayer.isPlayingCommercial())
      {
         return;
      }

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

   void stop(MediaPlayer mediaPlayer)
   {
      if (mediaPlayer != null && audioNormalizer != null && audioNormalizer.isNormalizing())
      {
         audioNormalizer.reset(mediaPlayer);
         LOGGER.info("Audio normalization was stopped and cleaned for current episode.");
         audioNormalizer = null;
      }
   }
}
