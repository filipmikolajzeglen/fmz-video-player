package com.filipmikolajzeglen.video;

import com.filipmikolajzeglen.database.FMZIdentifiable;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class Video implements Serializable, FMZIdentifiable {

    private String id;
    private String seriesName;
    private String episodeName;
    private String episodeNumber;
    private String seasonNumber;
    private String extension;
    private String path;
    private boolean isWatched;

    public Video() {
    }

    public Video(File directory, String currentVideo) {
        this.seriesName = extractSeriesName(directory);
        this.episodeName = extractEpisodeName(currentVideo);
        this.seasonNumber = extractSeasonNumber(currentVideo);
        this.episodeNumber = extractEpisodeNumber(currentVideo);
        this.extension = extractExtension(currentVideo);
        this.path = buildPath(directory, currentVideo);
        this.id = createId();
        this.isWatched = false;
    }

    private String extractSeriesName(File directory) {
        return directory.getName();
    }

    private String extractEpisodeName(String videoName) {
        return videoName.substring(7, videoName.length() - 4).replace("-", " ");
    }

    private String extractSeasonNumber(String videoName) {
        return videoName.substring(0, 3);
    }

    private String extractEpisodeNumber(String videoName) {
        return videoName.substring(3, 6);
    }

    private String extractExtension(String videoName) {
        return videoName.substring(videoName.length() - 3);
    }

    private String buildPath(File directory, String videoName) {
        return directory.getAbsolutePath() + "\\" + videoName;
    }

    private String createId() {
        return String.format("%s%s-%s", this.seasonNumber, this.episodeNumber, this.seriesName);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
                ", videoName: '" + seriesName + '\'' +
                ", episodeName: '" + episodeName + '\'' +
                ", episodeNumber: '" + episodeNumber + '\'' +
                ", seasonNumber: '" + seasonNumber + '\'' +
                ", videoExtension: '" + extension + '\'' +
                ", videoPath: '" + path + '\'' +
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
