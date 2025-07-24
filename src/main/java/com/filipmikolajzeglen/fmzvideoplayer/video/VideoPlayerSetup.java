package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.List;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.database.FMZDatabase;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;

class VideoPlayerSetup
{
   private static final Logger LOGGER = new Logger();
   private final VideoPlayer videoPlayer;

   VideoPlayerSetup(VideoPlayer videoPlayer)
   {
      this.videoPlayer = videoPlayer;
   }

   void setupAndStart()
   {
      prepareVideoPlayerService();
      injectControllers();
      initializeBusinessLogic();
      startFirstVideoIfAvailable();
   }

   private void prepareVideoPlayerService()
   {
      VideoPlayerService videoPlayerService = createVideoPlayerService();
      videoPlayer.setVideoPlayerService(videoPlayerService);
   }

   private VideoPlayerService createVideoPlayerService()
   {
      FMZDatabase<Video> database = new FMZDatabase<>(Video.class);
      database.setDatabaseName(FMZVideoPlayerConfiguration.Paths.FMZ_DATABASE_NAME);
      database.setTableName(FMZVideoPlayerConfiguration.Paths.FMZ_TABLE_NAME);
      database.setDirectoryPath(FMZVideoPlayerConfiguration.Paths.FMZ_DIRECTORY_PATH);
      return new VideoPlayerService(database);
   }

   private void injectControllers()
   {
      videoPlayer.setPlaybackController(PlaybackController.of(videoPlayer));
      videoPlayer.setMediaPlayerManager(MediaPlayerManager.of(videoPlayer));
      videoPlayer.setEpisodeInfoController(EpisodeInfoController.of(videoPlayer));
      videoPlayer.setSliderController(SliderController.of(videoPlayer));
      videoPlayer.setVolumeController(VolumeController.of(videoPlayer));
      videoPlayer.setFullScreenController(FullScreenController.of(videoPlayer));
      videoPlayer.setPlaybackSpeedController(PlaybackSpeedController.of(videoPlayer));
      videoPlayer.setMediaViewResizer(MediaViewResizer.of(videoPlayer));
      videoPlayer.setFadeOutManager(FadeOutManager.of(videoPlayer));
      videoPlayer.setPlaybackButtonController(PlaybackButtonController.of(videoPlayer));
      videoPlayer.setCommercialsManager(new CommercialsManager());
      videoPlayer.setAudioNormalizationManager(AudioNormalizationManager.of(videoPlayer));
   }

   private void initializeBusinessLogic()
   {
      videoPlayer.getVideoPlayerService().initializeFMZDB();
      List<Video> videos;
      if (FMZVideoPlayerConfiguration.Playback.USE_CUSTOM_SCHEDULE &&
            FMZVideoPlayerConfiguration.Playback.CUSTOM_SCHEDULE != null &&
            !FMZVideoPlayerConfiguration.Playback.CUSTOM_SCHEDULE.isEmpty())
      {
         // Użyj harmonogramu niestandardowego
         videos = videoPlayer.getVideoPlayerService()
               .createScheduleFromSeriesList(FMZVideoPlayerConfiguration.Playback.CUSTOM_SCHEDULE);
      }
      else
      {
         // Użyj harmonogramu generowanego automatycznie (Quick Start)
         videos = videoPlayer.getVideoPlayerService().createVideosSchedule();
      }
      PlaylistManager playlistManager = new PlaylistManager(videos);
      videoPlayer.setPlaylistManager(playlistManager);
      LOGGER.info(playlistManager.createPlaylistLog());
   }


   private void startFirstVideoIfAvailable()
   {
      if (FMZVideoPlayerConfiguration.Playback.COMMERCIALS_ENABLED)
      {
         videoPlayer.getCommercialsManager().loadCommercials();
      }
      PlaylistManager playlistManager = videoPlayer.getPlaylistManager();
      if (playlistManager.getPlaylistSize() > 0)
      {
         startFirstVideo(playlistManager);
      }
   }

   private void startFirstVideo(PlaylistManager playlistManager)
   {
      playlistManager.moveToFirst();
      Video firstVideo = playlistManager.getCurrentVideo();
      String firstVideoPath = playlistManager.getCurrentVideoPath();

      if (firstVideo != null && firstVideoPath != null)
      {
         videoPlayer.getEpisodeInfoController().updateInfo(firstVideo);
         videoPlayer.initializeMediaPlayer(firstVideoPath);
         videoPlayer.initializeAllControlsSvgOnTheBeginning();
         videoPlayer.setUpButtonHandlers();
      }
   }
}