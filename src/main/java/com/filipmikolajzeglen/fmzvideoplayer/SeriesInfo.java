package com.filipmikolajzeglen.fmzvideoplayer;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SeriesInfo
{
   private final SimpleStringProperty name;
   private final SimpleIntegerProperty episodeCount;
   private final SimpleStringProperty totalWatchingTime;

   public SeriesInfo(String name, int episodeCount)
   {
      this.name = new SimpleStringProperty(name);
      this.episodeCount = new SimpleIntegerProperty(episodeCount);
      this.totalWatchingTime = new SimpleStringProperty("...");
   }

   public String getName()
   {
      return name.get();
   }

   public SimpleStringProperty nameProperty()
   {
      return name;
   }

   public int getEpisodeCount()
   {
      return episodeCount.get();
   }

   public SimpleIntegerProperty episodeCountProperty()
   {
      return episodeCount;
   }

   public String getTotalWatchingTime()
   {
      return totalWatchingTime.get();
   }

   public SimpleStringProperty totalWatchingTimeProperty()
   {
      return totalWatchingTime;
   }

   public void setTotalWatchingTime(String time)
   {
      this.totalWatchingTime.set(time);
   }
}