package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

   List<Video> getAllUnwatchedVideo()
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

   // To jest przykład, który należy umieścić w klasie VideoPlayerService
   public List<Video> createScheduleFromSeriesList(List<String> seriesNames)
   {
      // 1. Pobierz wszystkie nieobejrzane filmy i pogrupuj je według serii,
      // upewniając się, że odcinki w każdej serii są posortowane.
      Map<String, List<Video>> unwatchedVideosBySeries = getAllUnwatchedVideo().stream()
            .sorted(Comparator.comparing(Video::getEpisodeNumber))
            .collect(Collectors.groupingBy(Video::getSeriesName));

      // Mapa do śledzenia indeksu następnego odcinka do pobrania dla każdej serii
      Map<String, Integer> nextEpisodeIndexMap = new java.util.HashMap<>();

      List<Video> customScheduleVideos = new ArrayList<>();

      // 2. Iteruj po harmonogramie zdefiniowanym przez użytkownika
      for (String seriesName : seriesNames)
      {
         // Pobierz listę nieobejrzanych odcinków dla bieżącej serii
         List<Video> seriesEpisodes = unwatchedVideosBySeries.get(seriesName);
         if (seriesEpisodes == null || seriesEpisodes.isEmpty())
         {
            LOGGER.warning("Brak nieobejrzanych odcinków dla serii: " + seriesName + " w harmonogramie.");
            continue; // Pomiń, jeśli dla danej serii nie ma odcinków
         }

         // Pobierz aktualny indeks dla tej serii, domyślnie 0
         int currentIndex = nextEpisodeIndexMap.getOrDefault(seriesName, 0);

         // Sprawdź, czy jest jeszcze dostępny odcinek
         if (currentIndex < seriesEpisodes.size())
         {
            // Dodaj następny odcinek do playlisty
            customScheduleVideos.add(seriesEpisodes.get(currentIndex));
            // Zaktualizuj indeks dla tej serii, aby następnym razem wziąć kolejny odcinek
            nextEpisodeIndexMap.put(seriesName, currentIndex + 1);
         }
         else
         {
            LOGGER.warning(
                  "Wszystkie nieobejrzane odcinki dla serii: " + seriesName + " zostały już dodane do harmonogramu.");
         }
      }

      return customScheduleVideos;
   }

   public List<Video> createPlaylistForSeries(String seriesName)
   {
      File mainDir = new File(FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE);
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

}