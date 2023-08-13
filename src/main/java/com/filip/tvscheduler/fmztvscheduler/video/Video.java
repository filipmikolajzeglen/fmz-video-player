package com.filip.tvscheduler.fmztvscheduler.video;

import com.filip.tvscheduler.fmztvscheduler.fmzdatabase.FMZIdentifable;


import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class Video implements Serializable, FMZIdentifable {

    private String id;

    private String videoName;

    private String episodeName;

    private String episodeNumber;

    private String seasonNumber;

    private String videoExtension;

    private String videoPath;
    private boolean isWatched;

    public Video() {
    }

    public Video(File directory, String currentVideo) {
        this.videoName = directory.getName();
        this.episodeName = currentVideo.substring(7, currentVideo.length() - 4).replace("-", " ");
        this.episodeNumber = currentVideo.substring(3, 6);
        this.seasonNumber = currentVideo.substring(0, 3);
        this.videoExtension = currentVideo.substring(currentVideo.length() - 3);
        ;
        this.videoPath = directory.getAbsolutePath() + "\\" + currentVideo;
        this.isWatched = false;
        this.id = String.format("%s%s-%s", seasonNumber, episodeNumber, videoName);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    public String getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(String episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(String seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public String getVideoExtension() {
        return videoExtension;
    }

    public void setVideoExtension(String videoExtension) {
        this.videoExtension = videoExtension;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public boolean isWatched() {
        return isWatched;
    }

    public void setWatched(boolean watched) {
        isWatched = watched;
    }

    @Override
    public String toString() {
        return "\nVideo { " +
                "id: '" + id + '\'' +
                ", videoName: '" + videoName + '\'' +
                ", episodeName: '" + episodeName + '\'' +
                ", episodeNumber: '" + episodeNumber + '\'' +
                ", seasonNumber: '" + seasonNumber + '\'' +
                ", videoExtension: '" + videoExtension + '\'' +
                ", videoPath: '" + videoPath + '\'' +
                ", isWatched: " + isWatched +
                " }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Video)) return false;
        Video video = (Video) o;
        return id.equals(video.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
