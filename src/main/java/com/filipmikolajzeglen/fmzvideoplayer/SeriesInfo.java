package com.filipmikolajzeglen.fmzvideoplayer;

public class SeriesInfo
{
   private final String name;
   private final int episodeCount;

   public SeriesInfo(String name, int episodeCount)
   {
      this.name = name;
      this.episodeCount = episodeCount;
   }

   public String getName()
   {
      return name;
   }

   public int getEpisodeCount()
   {
      return episodeCount;
   }
}