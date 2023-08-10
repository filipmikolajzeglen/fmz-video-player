package com.filip.tvscheduler.fmztvscheduler.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.VIDEO_MAIN_SOURCE;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.EPISODES_LIMIT_OF_A_SINGLE_SERIES_PER_DAY;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.MAXIMUM_NUMBER_OF_EPISODES_IN_THE_SCHEDULE_PER_DAY;
import static java.util.Objects.requireNonNull;

public class VideoScheduleService {

    public List<String> createPathsToAllVideos() {
        List<String> videoCommands = new ArrayList<>();
        List<Video> videos = createVideosSchedule();

        for (Video video : videos) {
            videoCommands.add(video.getVideoPath());
        }

        return videoCommands;
    }

    public List<Video> createVideosSchedule() {
        List<Video> allVideos = getAllVideoFromMainSource();
        List<Video> videosSchedule = new ArrayList<>();

        for (Video video : allVideos) {
            if (isReachedMaximumNumberOfEpisodesInTheSchedulePerDay(videosSchedule)) {
                return videosSchedule;
            }

            if (!isReachedEpisodesLimitOfASingleSeriesPerDay(videosSchedule, video) && !video.isWatched()) {
                video.setWatched(true);
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
                .filter(video -> video.getVideoName().equals(currentVideo.getVideoName()))
                .count() == EPISODES_LIMIT_OF_A_SINGLE_SERIES_PER_DAY;
    }

    private List<Video> getAllEpisodesOfCartoonFromDirectory(File directoryName) {
        List<Video> videos = new ArrayList<>();

        for (String video : requireNonNull(directoryName.list())) {
            if (isVideoFile(video)) {
                videos.add(new Video(directoryName, video));
            }
        }

        return videos;
    }

    // Primitive and not the best way to check if a file is video.
    // It should be changed because for now it is checked if file is not a directory
    private boolean isVideoFile(String file) {
        return file.charAt(file.length() - 4) == '.';
    }

}