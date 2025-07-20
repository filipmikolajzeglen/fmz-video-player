package com.filipmikolajzeglen.fmzvideoplayer.video;

import javafx.scene.control.Label;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class EpisodeInfoController
{
   private final Label labelCurrentEpisode;

   static EpisodeInfoController of(VideoPlayer videoPlayer)
   {
      return new EpisodeInfoController(videoPlayer.getLabelCurrentEpisode());
   }

   void updateInfo(Video video)
   {
      if (video == null)
      {
         labelCurrentEpisode.setText("Brak informacji o odcinku");
         return;
      }
      String info = String.format("%s - %s",
            video.getSeriesName().toUpperCase(),
            video.getEpisodeName());
      labelCurrentEpisode.setText(info);
   }
}