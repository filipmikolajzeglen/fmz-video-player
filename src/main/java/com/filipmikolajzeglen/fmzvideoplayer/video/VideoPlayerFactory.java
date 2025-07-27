package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.List;

import com.filipmikolajzeglen.fmzvideoplayer.database.Database;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.video.audio.AudioNormalizer;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoFadeOutEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoMediaSizeEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoEpisodeInfoView;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoFullScreenView;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlaybackButtonsView;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlaybackSpeedView;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoTimeSliderView;
import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoVolumeView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoPlayerFactory
{
   private static final Logger LOGGER = new Logger();
   private final VideoPlayerView videoPlayerView;

   public static VideoPlayerFactory of(VideoPlayerView videoPlayerView)
   {
      return new VideoPlayerFactory(videoPlayerView);
   }

   public void setupAndStart()
   {
      prepareVideoPlayerService();
      injectControllers();
      initializeBusinessLogic();
      startFirstVideoIfAvailable();
   }

   private void prepareVideoPlayerService()
   {
      VideoService videoService = createVideoPlayerService();
      videoPlayerView.setVideoService(videoService);
   }

   private VideoService createVideoPlayerService()
   {
      Database<Video> database = new Database<>(Video.class);
      database.setDatabaseName(PlayerConstants.Paths.FMZ_DATABASE_NAME);
      database.setTableName(PlayerConstants.Paths.FMZ_TABLE_NAME);
      database.setDirectoryPath(PlayerConstants.Paths.APP_DATA_DIRECTORY);
      return new VideoService(database);
   }

   private void injectControllers()
   {
      videoPlayerView.setVideoPlaybackCoordinator(VideoPlaybackCoordinator.of(videoPlayerView));
      videoPlayerView.setVideoMediaPlayer(VideoMediaPlayer.of(videoPlayerView));
      videoPlayerView.setVideoEpisodeInfoView(VideoEpisodeInfoView.of(videoPlayerView));
      videoPlayerView.setVideoTimeSliderView(VideoTimeSliderView.of(videoPlayerView));
      videoPlayerView.setVideoVolumeView(VideoVolumeView.of(videoPlayerView));
      videoPlayerView.setVideoFullScreenView(VideoFullScreenView.of(videoPlayerView));
      videoPlayerView.setVideoPlaybackSpeedView(VideoPlaybackSpeedView.of(videoPlayerView));
      videoPlayerView.setVideoMediaSizeEffect(VideoMediaSizeEffect.of(videoPlayerView));
      videoPlayerView.setVideoFadeOutEffect(VideoFadeOutEffect.of(videoPlayerView));
      videoPlayerView.setVideoPlaybackButtonsView(VideoPlaybackButtonsView.of(videoPlayerView));
      videoPlayerView.setVideoCommercialsPlaylist(new VideoCommercialsPlaylist());
      videoPlayerView.setAudioNormalizer(AudioNormalizer.of(videoPlayerView));
   }

   private void initializeBusinessLogic()
   {
      videoPlayerView.getVideoService().initializeFMZDB();
      List<Video> videos;

      if (PlayerConstants.Playback.PLAYLIST_TO_START != null)
      {
         LOGGER.info(
               "Creating playlist for a specific series: " + PlayerConstants.Playback.PLAYLIST_TO_START);
         videos = videoPlayerView.getVideoService().createPlaylistForSeries(PlayerConstants.Playback.PLAYLIST_TO_START);
         PlayerConstants.Playback.PLAYLIST_TO_START = null;
      }
      else if (PlayerConstants.Playback.USE_CUSTOM_SCHEDULE &&
            PlayerConstants.Playback.CUSTOM_SCHEDULE != null &&
            !PlayerConstants.Playback.CUSTOM_SCHEDULE.isEmpty())
      {
         videos = videoPlayerView.getVideoService()
               .createScheduleFromSeriesList(PlayerConstants.Playback.CUSTOM_SCHEDULE);
      }
      else
      {
         videos = videoPlayerView.getVideoService().createVideosSchedule();
      }
      VideoPlaylist videoPlaylist = new VideoPlaylist(videos);
      videoPlayerView.setVideoPlaylist(videoPlaylist);
      LOGGER.info(videoPlaylist.createPlaylistLog());
   }

   private void startFirstVideoIfAvailable()
   {
      if (PlayerConstants.Playback.COMMERCIALS_ENABLED)
      {
         videoPlayerView.getVideoCommercialsPlaylist().loadCommercials();
      }
      VideoPlaylist videoPlaylist = videoPlayerView.getVideoPlaylist();
      if (videoPlaylist.getPlaylistSize() > 0)
      {
         startFirstVideo(videoPlaylist);
      }
   }

   private void startFirstVideo(VideoPlaylist videoPlaylist)
   {
      videoPlaylist.moveToFirst();
      Video firstVideo = videoPlaylist.getCurrentVideo();
      String firstVideoPath = videoPlaylist.getCurrentVideoPath();

      if (firstVideo != null && firstVideoPath != null)
      {
         videoPlayerView.getVideoEpisodeInfoView().updateInfo(firstVideo);
         videoPlayerView.initializeMediaPlayer(firstVideoPath);
         videoPlayerView.initializeAllControlsSvgOnTheBeginning();
         videoPlayerView.setUpButtonHandlers();
      }
   }
}