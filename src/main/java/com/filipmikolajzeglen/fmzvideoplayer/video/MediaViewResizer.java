package com.filipmikolajzeglen.fmzvideoplayer.video;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;

class MediaViewResizer
{
   private final MediaView mediaView;
   private final StackPane parentPane;
   private final Pane controlsPane;

   static MediaViewResizer of(VideoPlayer videoPlayer)
   {
      return new MediaViewResizer(videoPlayer);
   }

   private MediaViewResizer(VideoPlayer videoPlayer)
   {
      this.mediaView = videoPlayer.getMediaView();
      this.parentPane = videoPlayer.getStackPaneParent();
      this.controlsPane = videoPlayer.getHBoxControls();
   }

   void bindToScene()
   {
      parentPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
         if (oldScene == null && newScene != null)
         {
            mediaView.fitWidthProperty().bind(newScene.widthProperty());
            if (controlsPane != null)
            {
               mediaView.fitHeightProperty().bind(
                     newScene.heightProperty().subtract(controlsPane.heightProperty())
               );
            }
            else
            {
               mediaView.fitHeightProperty().bind(newScene.heightProperty());
            }
         }
      });
      mediaView.setPreserveRatio(false);
   }

   void setPreserveRatio(boolean preserve)
   {
      mediaView.setPreserveRatio(preserve);
   }
}