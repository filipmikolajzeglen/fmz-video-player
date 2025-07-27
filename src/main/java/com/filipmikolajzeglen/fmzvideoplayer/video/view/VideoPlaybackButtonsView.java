package com.filipmikolajzeglen.fmzvideoplayer.video.view;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoIconsEffect;
import javafx.scene.control.Button;
import javafx.scene.shape.SVGPath;

public class VideoPlaybackButtonsView
{
   private final Button buttonPlayPauseRestart;
   private final SVGPath buttonPlayPauseRestartSVG;
   private final Button buttonNext;
   private final SVGPath buttonNextSVG;

   public static VideoPlaybackButtonsView of(VideoPlayerView videoPlayerView)
   {
      return new VideoPlaybackButtonsView(videoPlayerView);
   }

   private VideoPlaybackButtonsView(VideoPlayerView videoPlayerView)
   {
      this.buttonPlayPauseRestart = videoPlayerView.getButtonPlayPauseRestart();
      this.buttonPlayPauseRestartSVG = videoPlayerView.getButtonPlayPauseRestartSVG();
      this.buttonNext = videoPlayerView.getButtonNext();
      this.buttonNextSVG = videoPlayerView.getButtonNextSVG();
   }

   void initializeNextButton()
   {
      VideoIconsEffect.setControlSVG(buttonNext, buttonNextSVG, PlayerConstants.Icons.NEXT());
   }

   void setToPlay()
   {
      buttonPlayPauseRestartSVG.setTranslateY(-0.1);
      buttonPlayPauseRestartSVG.setTranslateX(-0.1);
      VideoIconsEffect.setControlSVG(buttonPlayPauseRestart, buttonPlayPauseRestartSVG,
            PlayerConstants.Icons.PLAY());
   }

   void setToPause()
   {
      buttonPlayPauseRestartSVG.setTranslateX(1);
      buttonPlayPauseRestartSVG.setTranslateY(-1);
      VideoIconsEffect.setControlSVG(buttonPlayPauseRestart, buttonPlayPauseRestartSVG,
            PlayerConstants.Icons.PAUSE());
   }

   void setToReplay()
   {
      VideoIconsEffect.setControlSVG(buttonPlayPauseRestart, buttonPlayPauseRestartSVG,
            PlayerConstants.Icons.REPLAY());
   }
}