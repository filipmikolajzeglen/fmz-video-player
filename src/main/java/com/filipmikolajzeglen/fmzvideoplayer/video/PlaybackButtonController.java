package com.filipmikolajzeglen.fmzvideoplayer.video;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import javafx.scene.control.Button;
import javafx.scene.shape.SVGPath;

class PlaybackButtonController
{
   private final Button buttonPlayPauseRestart;
   private final SVGPath buttonPlayPauseRestartSVG;
   private final Button buttonNext;
   private final SVGPath buttonNextSVG;

   static PlaybackButtonController of(VideoPlayer videoPlayer)
   {
      return new PlaybackButtonController(videoPlayer);
   }

   private PlaybackButtonController(VideoPlayer videoPlayer)
   {
      this.buttonPlayPauseRestart = videoPlayer.getButtonPlayPauseRestart();
      this.buttonPlayPauseRestartSVG = videoPlayer.getButtonPlayPauseRestartSVG();
      this.buttonNext = videoPlayer.getButtonNext();
      this.buttonNextSVG = videoPlayer.getButtonNextSVG();
   }

   void initializeNextButton()
   {
      VideoPlayerIcons.setButtonSVG(buttonNext, buttonNextSVG, FMZVideoPlayerConfiguration.Icons.NEXT());
   }

   void setToPlay()
   {
      buttonPlayPauseRestartSVG.setTranslateY(-0.1);
      buttonPlayPauseRestartSVG.setTranslateX(-0.1);
      VideoPlayerIcons.setButtonSVG(buttonPlayPauseRestart, buttonPlayPauseRestartSVG,
            FMZVideoPlayerConfiguration.Icons.PLAY());
   }

   void setToPause()
   {
      buttonPlayPauseRestartSVG.setTranslateX(1);
      buttonPlayPauseRestartSVG.setTranslateY(-1);
      VideoPlayerIcons.setButtonSVG(buttonPlayPauseRestart, buttonPlayPauseRestartSVG,
            FMZVideoPlayerConfiguration.Icons.PAUSE());
   }

   void setToReplay()
   {
      VideoPlayerIcons.setButtonSVG(buttonPlayPauseRestart, buttonPlayPauseRestartSVG,
            FMZVideoPlayerConfiguration.Icons.REPLAY());
   }
}