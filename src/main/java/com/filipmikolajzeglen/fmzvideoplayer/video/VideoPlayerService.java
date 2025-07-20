package com.filipmikolajzeglen.fmzvideoplayer.video;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

   List<Video> createVideosSchedule()
   {
      return getAllUnwatchedVideo().stream()
            .collect(Collectors.groupingBy(Video::getSeriesName))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .flatMap(entry -> entry.getValue().stream()
                  .limit(FMZVideoPlayerConfiguration.Playback.MAX_SINGLE_SERIES_PER_DAY))
            .limit(FMZVideoPlayerConfiguration.Playback.MAX_EPISODES_PER_DAY)
            .collect(Collectors.toList());
   }

   List<Video> getAllUnwatchedVideo()
   {
      return database.findAll().stream()
            .filter(video -> !video.isWatched())
            .collect(Collectors.toList());
   }

   List<Video> getAllVideoFromMainSource()
   {
      File directory = new File(FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE);
      File[] listFiles = requireNonNull(directory.listFiles());

      return Arrays.stream(listFiles)
            .filter(File::isDirectory)
            .flatMap(file -> getAllEpisodesOfCartoonFromDirectory(file).stream())
            .collect(Collectors.toList());
   }

   private List<Video> getAllEpisodesOfCartoonFromDirectory(File directoryName)
   {
      File[] files = requireNonNull(directoryName.listFiles());

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
}