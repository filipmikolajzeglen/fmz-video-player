package com.filipmikolajzeglen.video;

import com.filipmikolajzeglen.database.FMZIdentifiable;
import lombok.*;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@ToString
public class Video implements Serializable, FMZIdentifiable {

    private String id;
    private String seriesName;
    private String episodeName;
    private String episodeNumber;
    private String seasonNumber;
    private String extension;
    private String path;
    private boolean isWatched;

    public static Video fromDirectoryAndFileName(File directory, String filename) {
        return Video.builder()
                .withId(createId(directory, filename))
                .withSeriesName(extractSeriesName(directory))
                .withEpisodeName(extractEpisodeName(filename))
                .withSeasonNumber(extractSeasonNumber(filename))
                .withEpisodeNumber(extractEpisodeNumber(filename))
                .withExtension(extractExtension(filename))
                .withPath(buildPath(directory, filename))
                .withIsWatched(false)
                .build();
    }

    private static String extractSeriesName(File directory) {
        return directory.getName();
    }

    private static String extractEpisodeName(String videoName) {
        return videoName.substring(7, videoName.length() - 4).replace("-", " ");
    }

    private static String extractSeasonNumber(String videoName) {
        return videoName.substring(0, 3);
    }

    private static String extractEpisodeNumber(String videoName) {
        return videoName.substring(3, 6);
    }

    private static String extractExtension(String videoName) {
        return videoName.substring(videoName.length() - 3);
    }

    private static String buildPath(File directory, String videoName) {
        return directory.getAbsolutePath() + "\\" + videoName;
    }

    private static String createId(File directory, String filename) {
        return String.format("%s%s-%s",
                extractSeasonNumber(filename),
                extractEpisodeNumber(filename),
                extractSeriesName(directory));
    }

    @Override
    public String getId() {
        return id;
    }

}
