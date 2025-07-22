package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.database.FMZDatabase;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class VideoPlayerService
{
   private static final Logger LOGGER = new Logger();
   private final FMZDatabase<Video> database;

   void initializeFMZDB()
   {
      database.initialize();
      if (database.findAll().isEmpty())
      {
         LOGGER.warning("FMZDatabase is empty, loading all videos from main source");
         database.saveAll(getAllVideoFromMainSource());
      }
   }

   List<Video> getAllVideoFromMainSource()
   {
      File mainDirectory = new File(FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE);
      File[] allFilesAndDirs = mainDirectory.listFiles();

      if (allFilesAndDirs == null)
      {
         LOGGER.warning("Could not list files in the main video source directory: " + mainDirectory.getPath());
         return Collections.emptyList();
      }

      return Arrays.stream(allFilesAndDirs)
            .filter(this::isCartoonSeriesDirectory)
            .flatMap(this::streamEpisodesFromDirectory)
            .collect(Collectors.toList());
   }

   private boolean isCartoonSeriesDirectory(File file)
   {
      return file.isDirectory() &&
            !file.getName().equalsIgnoreCase(FMZVideoPlayerConfiguration.Paths.COMMERCIALS_FOLDER_NAME);
   }

   private Stream<Video> streamEpisodesFromDirectory(File directory)
   {
      return getAllEpisodesOfCartoonFromDirectory(directory).stream();
   }

   private List<Video> getAllEpisodesOfCartoonFromDirectory(File directoryName)
   {
      File[] files = directoryName.listFiles();
      if (files == null)
      {
         return Collections.emptyList();
      }

      return Arrays.stream(files)
            .filter(file -> isVideoFile(file.toPath()))
            .map(file -> Video.fromDirectoryAndFileName(directoryName, file.getName()))
            .collect(Collectors.toList());
   }

   private boolean isVideoFile(Path path)
   {
      return !Files.isDirectory(path) && Optional.ofNullable(detectFileType(path))
            .map(s -> s.startsWith("video"))
            .orElse(false);
   }

   private String detectFileType(Path path)
   {
      try
      {
         return Files.probeContentType(path);
      }
      catch (IOException e)
      {
         LOGGER.error("An IO exception occurred " + e);
         return "";
      }
   }

   List<Video> createVideosSchedule()
   {
      List<Video> allUnwatchedVideos = getAllUnwatchedVideo();
      Map<String, List<Video>> videosGroupedBySeries = groupVideosBySeries(allUnwatchedVideos);
      List<Video> videosWithPerSeriesLimit = applyPerSeriesLimit(videosGroupedBySeries);
      return applyGlobalEpisodeLimit(videosWithPerSeriesLimit);
   }

   private List<Video> getAllUnwatchedVideo()
   {
      return database.findAll().stream()
            .filter(video -> !video.isWatched())
            .collect(Collectors.toList());
   }

   private Map<String, List<Video>> groupVideosBySeries(List<Video> videos)
   {
      return videos.stream().collect(Collectors.groupingBy(Video::getSeriesName));
   }

   private List<Video> applyPerSeriesLimit(Map<String, List<Video>> videosBySeries)
   {
      return videosBySeries.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .flatMap(entry -> entry.getValue().stream()
                  .limit(FMZVideoPlayerConfiguration.Playback.MAX_SINGLE_SERIES_PER_DAY))
            .collect(Collectors.toList());
   }

   private List<Video> applyGlobalEpisodeLimit(List<Video> videos)
   {
      return videos.stream()
            .limit(FMZVideoPlayerConfiguration.Playback.MAX_EPISODES_PER_DAY)
            .collect(Collectors.toList());
   }
}