package com.filipmikolajzeglen.fmzvideoplayer.video.view;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoIconsEffect;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class VideoFullScreenView
{
   private static final Logger LOGGER = new Logger();

   private final StackPane rootPane;
   private final Label fullScreenLabel;
   private final SVGPath fullScreenSVGPath;

   public static VideoFullScreenView of(VideoPlayerView videoPlayerView)
   {
      return new VideoFullScreenView(videoPlayerView);
   }

   private VideoFullScreenView(VideoPlayerView videoPlayerView)
   {
      this.rootPane = videoPlayerView.getStackPaneParent();
      this.fullScreenLabel = videoPlayerView.getLabelFullScreen();
      this.fullScreenSVGPath = videoPlayerView.getLabelFullScreenSVG();
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
      VideoIconsEffect.setControlSVG(fullScreenLabel, fullScreenSVGPath, PlayerConstants.Icons.FULLSCREEN());
   }

   private void setLabelMinimizeSVG()
   {
      VideoIconsEffect.setControlSVG(fullScreenLabel, fullScreenSVGPath, PlayerConstants.Icons.MINIMIZE());
   }
}