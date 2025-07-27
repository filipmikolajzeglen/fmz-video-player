package com.filipmikolajzeglen.fmzvideoplayer.video.effect;

import com.filipmikolajzeglen.fmzvideoplayer.video.view.VideoPlayerView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoFadeOutEffect
{
   private final VBox panel;
   private final MediaView mediaView;
   private final PauseTransition delayFadeOut;

   public static VideoFadeOutEffect of(VideoPlayerView videoPlayerView)
   {
      return new VideoFadeOutEffect(videoPlayerView);
   }

   private VideoFadeOutEffect(VideoPlayerView videoPlayerView)
   {
      this.panel = videoPlayerView.getVBoxFullPanel();
      this.mediaView = videoPlayerView.getMediaView();
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

   public void cancel()
   {
      delayFadeOut.stop();
   }
}