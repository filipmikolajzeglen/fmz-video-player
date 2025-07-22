package com.filipmikolajzeglen.fmzvideoplayer.video;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

class FullScreenController
{
   private static final Logger LOGGER = new Logger();

   private final StackPane rootPane;
   private final Label fullScreenLabel;
   private final SVGPath fullScreenSVGPath;

   static FullScreenController of(VideoPlayer videoPlayer)
   {
      return new FullScreenController(videoPlayer);
   }

   private FullScreenController(VideoPlayer videoPlayer)
   {
      this.rootPane = videoPlayer.getStackPaneParent();
      this.fullScreenLabel = videoPlayer.getLabelFullScreen();
      this.fullScreenSVGPath = videoPlayer.getLabelFullScreenSVG();
      setLabelMinimizeSVG();

      this.fullScreenLabel.setOnMouseClicked(event -> toggleFullScreen());
   }

   void toggleFullScreen()
   {
      Stage stage = (Stage) rootPane.getScene().getWindow();
      stage.setFullScreen(!stage.isFullScreen());
      stage.setAlwaysOnTop(true);

      if (stage.isFullScreen())
      {
         LOGGER.info("Fullscreen was entered");
         setLabelMinimizeSVG();
         stage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE)
            {
               LOGGER.info("Button ESCAPE was clicked");
               setLabelEnterFullscreenSVG();
            }
         });
      }
      else
      {
         LOGGER.info("Fullscreen was exited");
         setLabelEnterFullscreenSVG();
      }
   }

   private void setLabelEnterFullscreenSVG()
   {
      VideoPlayerIcons.setControlSVG(fullScreenLabel, fullScreenSVGPath, FMZVideoPlayerConfiguration.Icons.FULLSCREEN());
   }

   private void setLabelMinimizeSVG()
   {
      VideoPlayerIcons.setControlSVG(fullScreenLabel, fullScreenSVGPath, FMZVideoPlayerConfiguration.Icons.MINIMIZE());
   }
}