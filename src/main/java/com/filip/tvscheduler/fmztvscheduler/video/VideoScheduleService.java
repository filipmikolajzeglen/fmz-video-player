package com.filip.tvscheduler.fmztvscheduler.video;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class VideoScheduleService {

    private final static String VIDEO_MAIN_SOURCE = "E:\\FoxKids";
    private final static String FULLSCREEN = "--fullscreen";
    private final static String PLAYER_SOURCE = "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe";
    private final static int EPISODES_LIMIT_OF_A_SINGLE_SERIES_PER_DAY = 2;
    private final static int MAXIMUM_NUMBER_OF_EPISODES_IN_THE_SCHEDULE_PER_DAY = 30;

    public void runVideos() throws IOException {
        runCommand(prepareCommandsToRunAllVideos(createVideosSchedule())).start();
    }

    private ProcessBuilder runCommand(List<String> commands) {
        return new ProcessBuilder(commands)
                .directory(new File("E:\\FoxKids"))
                .redirectErrorStream(true);
    }

    private String getResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
            System.out.println(line);
        }
        return builder.toString().length() > 1 ? builder.toString() : null;
    }

    public List<String> createPathsToAllVideos() {
        List<String> videoCommands = new ArrayList<>();
        List<Video> videos = createVideosSchedule();

        for (Video video : videos) {
            videoCommands.add(video.getVideoPath());
        }

        return videoCommands;
    }

    private List<String> prepareCommandsToRunAllVideos(List<Video> videos) {
        List<String> videoCommands = new ArrayList<>();
        videoCommands.add(PLAYER_SOURCE);

        for (Video video : videos) {
            videoCommands.add(video.getVideoPath());
        }

        videoCommands.add(FULLSCREEN);
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