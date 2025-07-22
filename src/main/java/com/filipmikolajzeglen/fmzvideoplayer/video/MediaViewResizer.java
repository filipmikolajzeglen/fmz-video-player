package com.filipmikolajzeglen.fmzvideoplayer.video;

import javafx.scene.Scene;
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

   /**
    * Inicjuje proces bindowania wymiarów MediaView do sceny.
    * Czeka, aż scena będzie dostępna, a następnie deleguje logikę bindowania.
    */
   void bindToScene()
   {
      mediaView.setPreserveRatio(false);
      parentPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
         if (oldScene == null && newScene != null)
         {
            bindDimensions(newScene);
         }
      });
   }

   /**
    * Prywatna metoda zawierająca logikę bindowania (CO bindować).
    * @param scene Aktualna scena aplikacji.
    */
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

   /**
    * Pozwala na zewnętrzną zmianę trybu zachowania proporcji wideo.
    */
   void setPreserveRatio(boolean preserve)
   {
      mediaView.setPreserveRatio(preserve);
   }

}