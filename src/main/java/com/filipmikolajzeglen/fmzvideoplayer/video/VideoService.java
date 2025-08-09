package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.database.Database;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@RequiredArgsConstructor
public class VideoService
{
   private static final Logger LOGGER = new Logger();
   private final Database<Video> database;

   public void initialize()
   {
      if (database.readAll().isEmpty())
      {
         LOGGER.warning("Database is empty, loading all videos from main source");
         database.createAll(getAllVideoFromMainSource());
      }
   }

   List<Video> getAllVideoFromMainSource()
   {
      File mainDirectory = new File(PlayerConstants.Paths.VIDEO_MAIN_SOURCE);
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
            !file.getName().equalsIgnoreCase(PlayerConstants.Paths.COMMERCIALS_FOLDER_NAME);
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

   List<Video> getAllUnwatchedVideo()
   {
      return database.readAll().stream()
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
                  .limit(PlayerConstants.Playback.MAX_SINGLE_SERIES_PER_DAY))
            .collect(Collectors.toList());
   }

   private List<Video> applyGlobalEpisodeLimit(List<Video> videos)
   {
      return videos.stream()
            .limit(PlayerConstants.Playback.MAX_EPISODES_PER_DAY)
            .collect(Collectors.toList());
   }

   public List<Video> createScheduleFromSeriesList(List<String> seriesNames)
   {
      Map<String, List<Video>> unwatchedVideosBySeries = getAllUnwatchedVideo().stream()
            .sorted(Comparator.comparing(Video::getEpisodeNumber))
            .collect(Collectors.groupingBy(Video::getSeriesName));

      Map<String, Integer> nextEpisodeIndexMap = new HashMap<>();
      List<Video> customScheduleVideos = new ArrayList<>();

      for (String seriesName : seriesNames)
      {
         List<Video> seriesEpisodes = unwatchedVideosBySeries.get(seriesName);
         if (seriesEpisodes == null || seriesEpisodes.isEmpty())
         {
            LOGGER.warning("No unwatched episodes for series: " + seriesName + " in the schedule.");
            continue;
         }

         int currentIndex = nextEpisodeIndexMap.getOrDefault(seriesName, 0);
         if (currentIndex < seriesEpisodes.size())
         {
            customScheduleVideos.add(seriesEpisodes.get(currentIndex));
            nextEpisodeIndexMap.put(seriesName, currentIndex + 1);
         }
         else
         {
            LOGGER.warning(
                  "All unwatched episodes for the series: " + seriesName + " have already been added to the schedule.");
         }
      }

      return customScheduleVideos;
   }

   public List<Video> createPlaylistForSeries(String seriesName)
   {
      File mainDir = new File(PlayerConstants.Paths.VIDEO_MAIN_SOURCE);
      File seriesDir = new File(mainDir, seriesName);

      if (seriesDir.exists() && seriesDir.isDirectory())
      {
         LOGGER.info("Creating playlist for series: " + seriesName);
         return getAllEpisodesOfCartoonFromDirectory(seriesDir);
      }
      else
      {
         LOGGER.warning("Directory for series not found: " + seriesName);
         return Collections.emptyList();
      }
   }

   public List<Video> findAllBySeriesName(String seriesName)
   {
      return database.readAll().stream()
            .filter(video -> video.getSeriesName().equalsIgnoreCase(seriesName))
            .collect(Collectors.toList());
   }
}