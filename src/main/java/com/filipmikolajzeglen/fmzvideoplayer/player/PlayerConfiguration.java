package com.filipmikolajzeglen.fmzvideoplayer.player;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerConfiguration
{
   private int maxSingleSeriesPerDay;
   private int maxEpisodesPerDay;
   private String videoMainSourcePath;
   private String iconStyle;
   private String primaryColor;
   private boolean commercialsEnabled;
   private int commercialsCount;
   private boolean useCustomSchedule;
   private List<String> customSchedule;
   private String commercialsPath;
}
