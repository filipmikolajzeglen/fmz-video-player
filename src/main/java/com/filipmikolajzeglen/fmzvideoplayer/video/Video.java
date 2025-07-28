package com.filipmikolajzeglen.fmzvideoplayer.video;

import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
   private long durationInSeconds;

   public static Video fromDirectoryAndFileName(File directory, String filename)
   {
      File videoFile = new File(directory, filename);
      return Video.builder()
            .withId(createId(directory, filename))
            .withSeriesName(extractSeriesName(directory))
            .withEpisodeName(extractEpisodeName(filename))
            .withSeasonNumber(extractSeasonNumber(filename))
            .withEpisodeNumber(extractEpisodeNumber(filename))
            .withExtension(extractExtension(filename))
            .withPath(buildPath(directory, filename))
            .withWatched(false)
            .withReWatch(false)
            .withReWatchCount(0)
            .withDurationInSeconds(VideoMetadataReader.getDurationInSeconds(videoFile))
            .build();
   }

   private static String createId(File directory, String filename)
   {
      // ID uses the new combined episode number
      return String.format("%s%s-%s",
            extractSeasonNumber(filename),
            extractEpisodeNumber(filename),
            extractSeriesName(directory));
   }

   private static String extractPartIndicator(String filename)
   {
      Pattern pattern = Pattern.compile("-\\((\\w)\\)-");
      Matcher matcher = pattern.matcher(filename);
      if (matcher.find())
      {
         return matcher.group(1);
      }
      return "";
   }

   private static String extractSeriesName(File directory)
   {
      return directory.getName();
   }

   private static String extractEpisodeName(String videoName)
   {
      Pattern pattern = Pattern.compile("^S\\d{2}E\\d{2}(?:-\\(\\w\\))?-(.*?)\\..*$");
      Matcher matcher = pattern.matcher(videoName);
      if (matcher.find())
      {
         return matcher.group(1).replace("-", " ");
      }
      int lastDot = videoName.lastIndexOf('.');
      return lastDot > 0 ? videoName.substring(0, lastDot) : videoName;
   }

   private static String extractSeasonNumber(String videoName)
   {
      return videoName.substring(0, 3);
   }

   private static String extractEpisodeNumber(String videoName)
   {
      String baseEpisode = videoName.substring(3, 6);
      return baseEpisode + extractPartIndicator(videoName);
   }

   private static String extractExtension(String videoName)
   {
      int lastDotIndex = videoName.lastIndexOf('.');
      if (lastDotIndex > 0 && lastDotIndex < videoName.length() - 1)
      {
         return videoName.substring(lastDotIndex + 1);
      }
      return "";
   }

   private static String buildPath(File directory, String videoName)
   {
      return directory.getAbsolutePath() + File.separator + videoName;
   }
}