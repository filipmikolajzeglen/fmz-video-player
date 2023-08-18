package com.filipmikolajzeglen.video;

import com.filipmikolajzeglen.database.FMZDatabase;
import com.filipmikolajzeglen.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.VIDEO_MAIN_SOURCE;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.MAX_SINGLE_SERIES_PER_DAY;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.MAX_EPISODES_PER_DAY;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.FMZ_DATABASE_NAME;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.FMZ_TABLE_NAME;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.FMZ_DIRECTORY_PATH;
import static java.util.Objects.requireNonNull;

class VideoPlayerService {

    private static final Logger LOGGER = new Logger();
    private static final FMZDatabase<Video> DATABASE = new FMZDatabase<>();

    public void initializeFMZDB() {
        DATABASE.setDatabaseName(FMZ_DATABASE_NAME);
        DATABASE.setTableName(FMZ_TABLE_NAME);
        DATABASE.setDirectoryPath(FMZ_DIRECTORY_PATH);
        DATABASE.initialize();
        DATABASE.saveAll(getAllVideoFromMainSource());
    }

    public FMZDatabase<Video> getDatabase() {
        return DATABASE;
    }

    public List<String> createPathsToAllVideos() {
        List<Video> videos = createVideosSchedule();

        return videos.stream()
                .map(Video::getPath)
                .collect(Collectors.toList());
    }

    public List<Video> createVideosSchedule() {
        List<Video> allUnwatchedVideos = getAllUnwatchedVideo();
        List<Video> videosSchedule = new ArrayList<>();

        allUnwatchedVideos.forEach(video -> {
            if (isReachedMaximumNumberOfEpisodesInTheSchedulePerDay(videosSchedule)) {
                return;
            }

            if (!isReachedEpisodesLimitOfASingleSeriesPerDay(videosSchedule, video) && !video.isWatched()) {
                videosSchedule.add(video);
            }
        });

        return videosSchedule;
    }

    public List<Video> getAllUnwatchedVideo() {
        return DATABASE.findAll().stream()
                .filter(video -> !video.isWatched())
                .collect(Collectors.toList());
    }

    private boolean isReachedMaximumNumberOfEpisodesInTheSchedulePerDay(List<Video> videosSchedule) {
        return videosSchedule.size() == MAX_EPISODES_PER_DAY;
    }

    private boolean isReachedEpisodesLimitOfASingleSeriesPerDay(List<Video> videosSchedule, Video currentVideo) {
        return videosSchedule.stream()
                .filter(video -> video.getSeriesName().equals(currentVideo.getSeriesName()))
                .count() == MAX_SINGLE_SERIES_PER_DAY;
    }

    public List<Video> getAllVideoFromMainSource() {
        File directory = new File(VIDEO_MAIN_SOURCE);
        File[] listFiles = requireNonNull(directory.listFiles());

        return Arrays.stream(listFiles)
                .filter(File::isDirectory)
                .flatMap(file -> getAllEpisodesOfCartoonFromDirectory(file).stream())
                .collect(Collectors.toList());
    }

    private List<Video> getAllEpisodesOfCartoonFromDirectory(File directoryName) {
        File[] files = requireNonNull(directoryName.listFiles());

        return Arrays.stream(files)
                .filter(file -> isVideoFile(file.toPath()))
                .map(file -> Video.fromDirectoryAndFileName(directoryName, file.getName()))
                .collect(Collectors.toList());
    }

    private boolean isVideoFile(Path path) {
        return !Files.isDirectory(path) && Optional.ofNullable(detectFileType(path))
                .map(s -> s.startsWith("video"))
                .orElse(false);
    }

    private String detectFileType(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            LOGGER.error("An IO exception occurred " + e);
            return "";
        }
    }

    public String createVideoScheduleLog() {
        String startLine = "List of videos in schedule:  ";
        AtomicInteger videoNumber = new AtomicInteger(1);

        return startLine + createVideosSchedule().stream()
                .map(video -> String.format("[%s. %s - %s]",
                        videoNumber.getAndIncrement(), video.getSeriesName().toUpperCase(), video.getEpisodeName()))
                .collect(Collectors.joining("\n", "\n", ""));
    }
}