package com.filipmikolajzeglen.fmzvideoplayer.video.effect;

import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;

public class VideoMediaSizeEffect
{
   private final MediaView mediaView;
   private final StackPane parentPane;
   private final Pane controlsPane;

   public static VideoMediaSizeEffect of(VideoPlayerView videoPlayerView)
   {
      return new VideoMediaSizeEffect(videoPlayerView);
   }

   private VideoMediaSizeEffect(VideoPlayerView videoPlayerView)
   {
      this.mediaView = videoPlayerView.getMediaView();
      this.parentPane = videoPlayerView.getStackPaneParent();
      this.controlsPane = videoPlayerView.getHBoxControls();
   }

   public void bindToScene()
   {
      mediaView.setPreserveRatio(false);
      parentPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
         if (oldScene == null && newScene != null)
         {
            bindDimensions(newScene);
         }
      });
   }

   private void bindDimensions(Scene scene)
   {
      mediaView.fitWidthProperty().bind(scene.widthProperty());

      if (controlsPane != null)
      {
         mediaView.fitHeightProperty().bind(
               scene.heightProperty().subtract(controlsPane.heightProperty())
         );
      }
      else
      {
         mediaView.fitHeightProperty().bind(scene.heightProperty());
      }
   }

}