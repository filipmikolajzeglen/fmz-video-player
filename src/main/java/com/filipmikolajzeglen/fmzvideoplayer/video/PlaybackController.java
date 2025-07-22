package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.List;
import java.util.Optional;

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
      videoPlayer.play();
   }

   void pause()
   {
      LOGGER.info("Video was paused");
      videoPlayer.pause();
   }

   void replay()
   {
      LOGGER.info("Video was replayed");
      videoPlayer.replay();
   }

   void next()
   {
      if (videoPlayer.isPlayingCommercial())
      {
         handleCommercialTransition();
      }
      else
      {
         handleEpisodeFinish();
      }
   }

   private void handleCommercialTransition()
   {
      videoPlayer.getCommercialPlaylist().remove(0);

      if (!videoPlayer.getCommercialPlaylist().isEmpty())
      {
         playNextCommercial();
      }
      else
      {
         finishCommercialBlockAndPlayNextEpisode();
      }
   }

   private void playNextCommercial()
   {
      LOGGER.info("======================    NEXT COMMERCIAL    ======================");
      String nextAdPath = videoPlayer.getCommercialPlaylist().get(0);
      LOGGER.info("Playing next commercial: " + nextAdPath);
      videoPlayer.getEpisodeInfoController().updateInfo(null);
      videoPlayer.resetTimeSlider();
      videoPlayer.initializeMediaPlayer(nextAdPath);
   }

   private void finishCommercialBlockAndPlayNextEpisode()
   {
      LOGGER.info("Commercial block finished. Proceeding to the episode.");
      videoPlayer.setPlayingCommercial(false);
      playNextEpisode();
   }

   private void handleEpisodeFinish()
   {
      markCurrentVideoAsWatched();

      Optional<List<String>> commercialsToPlay = videoPlayer.getCommercialsManager()
            .getCommercialsToPlay(videoPlayer.getPlaylistManager().hasNext());

      commercialsToPlay.ifPresentOrElse(
            this::startCommercialBlock,
            this::playNextEpisode
      );
   }

   private void markCurrentVideoAsWatched()
   {
      Video currentVideo = videoPlayer.getPlaylistManager().getCurrentVideo();
      if (currentVideo != null)
      {
         currentVideo.setWatched(true);
         videoPlayer.getVideoPlayerService().getDatabase().save(currentVideo);
         LOGGER.info("Video marked as watched and saved to database: " + currentVideo.getId());
      }
   }

   private void startCommercialBlock(List<String> commercialsToPlay)
   {
      LOGGER.info("====================== START COMMERCIAL BLOCK ======================");
      LOGGER.info("Starting a commercial block with " + commercialsToPlay.size());
      videoPlayer.setCommercialPlaylist(commercialsToPlay);
      videoPlayer.setPlayingCommercial(true);

      String firstAdPath = commercialsToPlay.get(0);
      videoPlayer.getEpisodeInfoController().updateInfo(null);
      videoPlayer.resetTimeSlider();
      videoPlayer.initializeMediaPlayer(firstAdPath);
   }

   private void playNextEpisode()
   {
      LOGGER.info("======================       NEXT VIDEO       =======================");
      videoPlayer.playNextEpisodeFromPlaylist();
   }

}