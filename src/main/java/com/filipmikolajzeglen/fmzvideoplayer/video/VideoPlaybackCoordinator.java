package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.List;
import java.util.Optional;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoPlaybackCoordinator
{
   private static final Logger LOGGER = new Logger();
   private final VideoPlayerView videoPlayerView;

   static VideoPlaybackCoordinator of(VideoPlayerView videoPlayerView)
   {
      return new VideoPlaybackCoordinator(videoPlayerView);
   }

   public void togglePlayPause()
   {
      if (videoPlayerView.isAtEndOfVideo())
      {
         replay();
      }
      else if (videoPlayerView.isPlaying())
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
      videoPlayerView.play();
   }

   void pause()
   {
      LOGGER.info("Video was paused");
      videoPlayerView.pause();
   }

   void replay()
   {
      LOGGER.info("Video was replayed");
      videoPlayerView.replay();
   }

   public void next()
   {
      if (videoPlayerView.isPlayingCommercial())
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
      videoPlayerView.getCommercialPlaylist().remove(0);

      if (!videoPlayerView.getCommercialPlaylist().isEmpty())
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
      String nextAdPath = videoPlayerView.getCommercialPlaylist().get(0);
      LOGGER.info("Playing next commercial: " + nextAdPath);
      videoPlayerView.getVideoEpisodeInfoView().updateInfo(null);
      videoPlayerView.resetTimeSlider();
      videoPlayerView.initializeMediaPlayer(nextAdPath);
   }

   private void finishCommercialBlockAndPlayNextEpisode()
   {
      LOGGER.info("Commercial block finished. Proceeding to the episode.");
      videoPlayerView.setPlayingCommercial(false);
      playNextEpisode();
   }

   private void handleEpisodeFinish()
   {
      markCurrentVideoAsWatched();

      Optional<List<String>> commercialsToPlay = videoPlayerView.getVideoCommercialsPlaylist()
            .getCommercialsToPlay(videoPlayerView.getVideoPlaylist().hasNext());

      commercialsToPlay.ifPresentOrElse(
            this::startCommercialBlock,
            this::playNextEpisode
      );
   }

   private void markCurrentVideoAsWatched()
   {
      Video currentVideo = videoPlayerView.getVideoPlaylist().getCurrentVideo();
      if (currentVideo != null)
      {
         currentVideo.setWatched(true);
         videoPlayerView.getVideoService().getDatabase().save(currentVideo);
         LOGGER.info("Video marked as watched and saved to database: " + currentVideo.getId());
      }
   }

   private void startCommercialBlock(List<String> commercialsToPlay)
   {
      LOGGER.info("====================== START COMMERCIAL BLOCK ======================");
      LOGGER.info("Starting a commercial block with " + commercialsToPlay.size());
      videoPlayerView.setCommercialPlaylist(commercialsToPlay);
      videoPlayerView.setPlayingCommercial(true);

      String firstAdPath = commercialsToPlay.get(0);
      videoPlayerView.getVideoEpisodeInfoView().updateInfo(null);
      videoPlayerView.resetTimeSlider();
      videoPlayerView.initializeMediaPlayer(firstAdPath);
   }

   private void playNextEpisode()
   {
      LOGGER.info("======================       NEXT VIDEO       =======================");
      videoPlayerView.playNextEpisodeFromPlaylist();
   }

}