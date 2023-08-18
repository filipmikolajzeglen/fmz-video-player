package com.filipmikolajzeglen.video;

import com.filipmikolajzeglen.database.FMZDatabase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.VIDEO_MAIN_SOURCE;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.EPISODES_LIMIT_OF_A_SINGLE_SERIES_PER_DAY;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.MAXIMUM_NUMBER_OF_EPISODES_IN_THE_SCHEDULE_PER_DAY;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.FMZ_DATABASE_NAME;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.FMZ_TABLE_NAME;
import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.FMZ_DIRECTORY_PATH;
import static java.util.Objects.requireNonNull;

class VideoPlayerService {

    private final static FMZDatabase<Video> DATABASE = new FMZDatabase<>();

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

    public List<Video> getAllUnwatchedVideo() {
        return DATABASE.findAll().stream()
                .filter(video -> !video.isWatched())
                .collect(Collectors.toList());
    }

    public List<String> createPathsToAllVideos() {
        List<String> videoCommands = new ArrayList<>();
        List<Video> videos = createVideosSchedule();

        for (Video video : videos) {
            videoCommands.add(video.getPath());
        }

        return videoCommands;
    }

    public List<Video> createVideosSchedule() {
        List<Video> allUnwatchedVideos = getAllUnwatchedVideo();
        List<Video> videosSchedule = new ArrayList<>();

        for (Video video : allUnwatchedVideos) {
            if (isReachedMaximumNumberOfEpisodesInTheSchedulePerDay(videosSchedule)) {
                return videosSchedule;
            }

            if (!isReachedEpisodesLimitOfASingleSeriesPerDay(videosSchedule, video) && !video.isWatched()) {
                videosSchedule.add(video);
            }
        }

        return videosSchedule;
    }

    public List<Video> getAllVideoFromMainSource() {
        File directory = new File(VIDEO_MAIN_SOURCE);
        List<Video> videos = new ArrayList<>();
        File[] listFiles = requireNonNull(directory.listFiles());

        for (File file : listFiles) {
            if (file.isDirectory()) {
                videos.addAll(getAllEpisodesOfCartoonFromDirectory(file));
            }
        }

        return videos;
    }

    private boolean isReachedMaximumNumberOfEpisodesInTheSchedulePerDay(List<Video> videosSchedule) {
        return videosSchedule.size() == MAXIMUM_NUMBER_OF_EPISODES_IN_THE_SCHEDULE_PER_DAY;
    }

    private boolean isReachedEpisodesLimitOfASingleSeriesPerDay(List<Video> videosSchedule, Video currentVideo) {
        return videosSchedule.stream()
                .filter(video -> video.getSeriesName().equals(currentVideo.getSeriesName()))
                .count() == EPISODES_LIMIT_OF_A_SINGLE_SERIES_PER_DAY;
    }

    private List<Video> getAllEpisodesOfCartoonFromDirectory(File directoryName) {
        List<Video> videos = new ArrayList<>();
        File[] files = requireNonNull(directoryName.listFiles());

        for (File file : files) {
            if (isVideoFile(file.toPath())) {
                videos.add(new Video(directoryName, file.getName()));
            }
        }

        return videos;
    }

    private boolean isVideoFile(Path path) {
        if (Files.isDirectory(path)) {
            return false;
        }
        String detectedType = detectFileType(path);
        return detectedType != null && detectedType.startsWith("video");
    }

    private String detectFileType(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            System.out.println("An IO exception occurred " + e);
            return "";
        }
    }

    public String createVideoScheduleLog() {
        AtomicInteger videoNumber = new AtomicInteger(1);
        String startLine = "List of videos in schedule:  ";
        return startLine + createVideosSchedule().stream()
                .map(video -> "\n[" + videoNumber.getAndIncrement() + ". " +
                        video.getSeriesName() + " - " + video.getEpisodeName() + "]")
                .collect(Collectors.joining());
    }
}