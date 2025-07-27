package com.filipmikolajzeglen.fmzvideoplayer.video.view;

import com.filipmikolajzeglen.fmzvideoplayer.video.Video;
import javafx.scene.control.Label;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoEpisodeInfoView
{
   private final Label labelCurrentEpisode;

   public static VideoEpisodeInfoView of(VideoPlayerView videoPlayerView)
   {
      return new VideoEpisodeInfoView(videoPlayerView.getLabelCurrentEpisode());
   }

   public void updateInfo(Video video)
   {
      if (video == null)
      {
         labelCurrentEpisode.setText("Reklama");
         return;
      }
      String info = String.format("%s - %s",
            video.getSeriesName().toUpperCase(),
            video.getEpisodeName());
      labelCurrentEpisode.setText(info);
   }
}