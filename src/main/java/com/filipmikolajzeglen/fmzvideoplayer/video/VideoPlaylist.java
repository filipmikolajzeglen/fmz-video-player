package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@Getter
public class VideoPlaylist
{
   private final List<Video> playlist;
   private final List<String> videoPathsList;
   private int currentIndex;

   VideoPlaylist(List<Video> videos)
   {
      this.playlist = new ArrayList<>(videos);
      this.videoPathsList = new ArrayList<>();
      this.currentIndex = -1;

      for (Video video : videos)
      {
         videoPathsList.add(video.getPath());
      }

      log.info("Playlist initialized with {} videos", videos.size());
   }

   public boolean hasNext()
   {
      return currentIndex < playlist.size() - 1;
   }

   public Video getCurrentVideo()
   {
      if (currentIndex >= 0 && currentIndex < playlist.size())
      {
         return playlist.get(currentIndex);
      }
      return null;
   }

   public String getCurrentVideoPath()
   {
      if (currentIndex >= 0 && currentIndex < videoPathsList.size())
      {
         return videoPathsList.get(currentIndex);
      }
      return null;
   }

   public Video getNextVideo()
   {
      if (hasNext())
      {
         currentIndex++;
         log.info("Moving to next video, index: {}", currentIndex);
         return getCurrentVideo();
      }
      log.warn("No more videos in playlist");
      return null;
   }

   void moveToFirst()
   {
      if (!playlist.isEmpty())
      {
         currentIndex = 0;
         log.info("====================== START YOUR AWESOME TV ======================");
         log.info("Reset playlist to first video");
      }
   }

   int getPlaylistSize()
   {
      return playlist.size();
   }

   String createPlaylistLog()
   {
      StringBuilder log = new StringBuilder("Current playlist:\n");
      for (int i = 0; i < playlist.size(); i++)
      {
         Video video = playlist.get(i);
         String marker = i == currentIndex ? "-> " : "   ";
         log.append(String.format("%s[%d. %s - %s]\n",
               marker, (i + 1), video.getSeriesName().toUpperCase(), video.getEpisodeName()));
      }
      return log.toString();
   }

}
