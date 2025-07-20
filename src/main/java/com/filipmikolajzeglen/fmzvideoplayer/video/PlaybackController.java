package com.filipmikolajzeglen.fmzvideoplayer.video;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class PlaybackController
{
   private static final Logger LOGGER = new Logger();
   private final VideoPlayer videoPlayer;

   static PlaybackController of(VideoPlayer videoPlayer)
   {
      return new PlaybackController(videoPlayer);
   }

   void togglePlayPause()
   {
      if (videoPlayer.isAtEndOfVideo())
      {
         replay();
      }
      else if (videoPlayer.isPlaying())
      {
         pause();
      }
      else
      {
         play();
      }
   }

   void play()
   {
      LOGGER.info("Video was resumed");
      videoPlayer.getMediaPlayer().play();
      videoPlayer.getPlaybackButtonController().setToPause();
      videoPlayer.setPlaying(true);
   }

   void pause()
   {
      LOGGER.info("Video was paused");
      videoPlayer.getMediaPlayer().pause();
      videoPlayer.getPlaybackButtonController().setToPlay();
      videoPlayer.setPlaying(false);
   }

   void replay()
   {
      LOGGER.info("Video was replayed");
      videoPlayer.getSliderTime().setValue(0);
      videoPlayer.getMediaPlayer().seek(javafx.util.Duration.ZERO);
      videoPlayer.getMediaPlayer().play();
      videoPlayer.getPlaybackButtonController().setToPause();
      videoPlayer.setAtEndOfVideo(false);
      videoPlayer.setPlaying(true);
   }

   void next()
   {
      LOGGER.info("====================== NEXT VIDEO IS INITIALIZING ======================");
      if (videoPlayer.getPlaylistManager().hasNext())
      {
         Video nextVideo = videoPlayer.getPlaylistManager().getNextVideo();
         String nextVideoPath = videoPlayer.getPlaylistManager().getCurrentVideoPath();

         if (nextVideo != null && nextVideoPath != null)
         {
            videoPlayer.getEpisodeInfoController().updateInfo(nextVideo);
            videoPlayer.initializeMediaPlayer(nextVideoPath);
            videoPlayer.resetTimeSlider();
            videoPlayer.updateCurrentTimeLabelIfNeeded();
         }
      }
      else
      {
         LOGGER.info("Next video does not exist, set REPLAY button");
         videoPlayer.getPlaybackButtonController().setToReplay();
         videoPlayer.setAtEndOfVideo(true);
         videoPlayer.setPlaying(false);
      }
   }
}