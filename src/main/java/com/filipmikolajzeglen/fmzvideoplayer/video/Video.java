package com.filipmikolajzeglen.fmzvideoplayer.video;

import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@ToString
public class Video implements Serializable
{
   private String id;
   private String seriesName;
   private String episodeName;
   private String episodeNumber;
   private String seasonNumber;
   private String extension;
   private String path;
   private Double audioNormalizedVolume;
   private boolean watched;
   private boolean reWatch;
   private int reWatchCount;

   public static Video fromDirectoryAndFileName(File directory, String filename)
   {
      return Video.builder()
            .withId(createId(directory, filename))
            .withSeriesName(extractSeriesName(directory))
            .withEpisodeName(extractEpisodeName(filename))
            .withSeasonNumber(extractSeasonNumber(filename))
            .withEpisodeNumber(extractEpisodeNumber(filename))
            .withExtension(extractExtension(filename))
            .withPath(buildPath(directory, filename))
            .withAudioNormalizedVolume(null)
            .withWatched(false)
            .withReWatch(false)
            .withReWatchCount(0)
            .build();
   }

   private static String createId(File directory, String filename)
   {
      return String.format("%s%s-%s",
            extractSeasonNumber(filename),
            extractEpisodeNumber(filename),
            extractSeriesName(directory));
   }

   private static String extractSeriesName(File directory)
   {
      return directory.getName();
   }

   private static String extractEpisodeName(String videoName)
   {
      return videoName.substring(7, videoName.length() - 4).replace("-", " ");
   }

   private static String extractSeasonNumber(String videoName)
   {
      return videoName.substring(0, 3);
   }

   private static String extractEpisodeNumber(String videoName)
   {
      return videoName.substring(3, 6);
   }

   private static String extractExtension(String videoName)
   {
      return videoName.substring(videoName.length() - 3);
   }

   private static String buildPath(File directory, String videoName)
   {
      return directory.getAbsolutePath() + "\\" + videoName;
   }

}
