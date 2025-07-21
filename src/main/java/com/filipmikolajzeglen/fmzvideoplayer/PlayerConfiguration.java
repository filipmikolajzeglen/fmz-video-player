package com.filipmikolajzeglen.fmzvideoplayer;

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
}