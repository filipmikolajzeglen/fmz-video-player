package com.filipmikolajzeglen.fmzvideoplayer.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLibrarySeriesMetadata
{
   private String name;
   private int episodeCount;
   private String totalWatchingTime;

   @Override
   public final boolean equals(Object o)
   {
      if (!(o instanceof PlayerLibrarySeriesMetadata that))
      {
         return false;
      }

      return name.equals(that.name);
   }

   @Override
   public int hashCode()
   {
      return name.hashCode();
   }
}
