package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.List;
import java.util.Optional;

import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoPlaybackCoordinator
{
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
      log.info("Video was played");
      videoPlayerView.play();
   }

   void pause()
   {
      log.info("Video was paused");
      videoPlayerView.pause();
   }

   void replay()
   {
      log.info("Video was replayed");
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
      videoPlayerView.getCommercialPlaylist().removeFirst();

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
      log.info("======================    NEXT COMMERCIAL    ======================");
      String nextAdPath = videoPlayerView.getCommercialPlaylist().getFirst();
      log.info("Next commercial path: {}", nextAdPath);
      videoPlayerView.getVideoEpisodeInfoView().updateInfo(null);
      videoPlayerView.resetTimeSlider();
      videoPlayerView.initializeVideoPlayer(nextAdPath);
   }

   private void finishCommercialBlockAndPlayNextEpisode()
   {
      log.info("====================== FINISH COMMERCIAL BLOCK ======================");
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
         videoPlayerView.getVideoService().getDatabase().update(currentVideo);
         log.info("Video marked as watched and saved to database: {}", currentVideo.getId());
      }
   }

   private void startCommercialBlock(List<String> commercialsToPlay)
   {
      log.info("====================== START COMMERCIAL BLOCK ======================");
      log.info("Commercials to play: {}", commercialsToPlay.size());
      videoPlayerView.setCommercialPlaylist(commercialsToPlay);
      videoPlayerView.setPlayingCommercial(true);

      String firstAdPath = commercialsToPlay.getFirst();
      videoPlayerView.getVideoEpisodeInfoView().updateInfo(null);
      videoPlayerView.resetTimeSlider();
      videoPlayerView.initializeVideoPlayer(firstAdPath);
   }

   private void playNextEpisode()
   {
      log.info("======================       NEXT VIDEO       =======================");
      videoPlayerView.playNextEpisodeFromPlaylist();
   }

}