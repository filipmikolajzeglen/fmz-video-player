package com.filipmikolajzeglen.fmzvideoplayer.video;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

class FadeOutManager
{
   private final VBox panel;
   private final MediaView mediaView;
   private final PauseTransition delayFadeOut;

   static FadeOutManager of(VideoPlayer videoPlayer)
   {
      return new FadeOutManager(videoPlayer);
   }

   private FadeOutManager(VideoPlayer videoPlayer)
   {
      this.panel = videoPlayer.getVBoxFullPanel();
      this.mediaView = videoPlayer.getMediaView();
      this.delayFadeOut = new PauseTransition(Duration.seconds(3));
      setupFadeOut();
      delayFadeOut.playFromStart();
   }

   private void setupFadeOut()
   {
      delayFadeOut.setOnFinished(e -> fadeOutPanel());
      mediaView.setOnMouseMoved(evt -> delayFadeOut.playFromStart());
      panel.setOnMouseExited(evt -> delayFadeOut.playFromStart());
      panel.setOnMouseEntered(evt -> {
         panel.setOpacity(1);
         delayFadeOut.stop();
      });
   }

   private void fadeOutPanel()
   {
      Timeline fadeOutTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(panel.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(panel.opacityProperty(), 0.0))
      );
      fadeOutTimeline.play();
   }

   public void cancel() {
      delayFadeOut.stop();
   }
}