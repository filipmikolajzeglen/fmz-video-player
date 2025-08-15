package com.filipmikolajzeglen.fmzvideoplayer.video.effect;

import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class VideoMediaSizeEffect
{
   private final ImageView imageView;
   private final StackPane parentPane;
   private final Pane controlsPane;

   public static VideoMediaSizeEffect of(VideoPlayerView videoPlayerView)
   {
      return new VideoMediaSizeEffect(videoPlayerView);
   }

   private VideoMediaSizeEffect(VideoPlayerView videoPlayerView)
   {
      this.imageView = videoPlayerView.getImageView();
      this.parentPane = videoPlayerView.getStackPaneParent();
      this.controlsPane = videoPlayerView.getHBoxControls();
   }

   public void bindToScene()
   {
      if (imageView == null) return;
      imageView.setPreserveRatio(false);
      parentPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
         if (oldScene == null && newScene != null)
         {
            bindDimensions(newScene);
         }
      });
   }

   private void bindDimensions(Scene scene)
   {
      imageView.fitWidthProperty().bind(scene.widthProperty());

      if (controlsPane != null)
      {
         imageView.fitHeightProperty().bind(
               scene.heightProperty().subtract(controlsPane.heightProperty())
         );
      }
      else
      {
         imageView.fitHeightProperty().bind(scene.heightProperty());
      }
   }

}