package com.filipmikolajzeglen.fmzvideoplayer.video.view;

import com.filipmikolajzeglen.fmzvideoplayer.player.VideoPlayer;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoSliderStyleEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoTimeFormatEffect;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class VideoTimeSliderView
{
   private final Slider sliderTime;
   private final Label labelCurrentTime;
   private final Label labelTotalTime;
   private ChangeListener<Boolean> sliderValueChangingListener;
   private ChangeListener<Number> sliderValueListener;
   private ChangeListener<javafx.util.Duration> mediaPlayerTotalDurationListener;
   private ChangeListener<javafx.util.Duration> mediaPlayerCurrentDurationListener;
   private VideoPlayer mediaPlayer;

   public static VideoTimeSliderView of(VideoPlayerView videoPlayerView)
   {
      return new VideoTimeSliderView(videoPlayerView);
   }

   private VideoTimeSliderView(VideoPlayerView videoPlayerView)
   {
      this.sliderTime = videoPlayerView.getSliderTime();
      this.labelCurrentTime = videoPlayerView.getLabelCurrentTime();
      this.labelTotalTime = videoPlayerView.getLabelTotalTime();
      VideoSliderStyleEffect.addDynamicColorListener(this.sliderTime);
   }

   public void bindToMediaPlayer(VideoPlayer mediaPlayer)
   {
      unbindListeners();
      this.mediaPlayer = mediaPlayer;
      if (mediaPlayer == null)
      {
         return;
      }
      bindSliderValueChangingListener();
      bindSliderValueListener();
      bindMediaPlayerTotalDurationListener();
      bindMediaPlayerCurrentDurationListener();
   }

   public void unbindListeners()
   {
      removeListener(sliderTime.valueChangingProperty(), sliderValueChangingListener);
      removeListener(sliderTime.valueProperty(), sliderValueListener);
      if (mediaPlayer != null)
      {
         removeListener(mediaPlayer.totalDurationProperty(), mediaPlayerTotalDurationListener);
         removeListener(mediaPlayer.currentTimeProperty(), mediaPlayerCurrentDurationListener);
      }
   }

   private <T> void removeListener(ObservableValue<T> property, javafx.beans.value.ChangeListener<T> listener)
   {
      if (listener != null)
      {
         property.removeListener(listener);
      }
   }

   private void bindSliderValueChangingListener()
   {
      sliderValueChangingListener = (observableValue, wasChanging, isChanging) -> {
         bindCurrentTimeLabel();
         if (!isChanging)
         {
            mediaPlayer.seek(javafx.util.Duration.seconds(sliderTime.getValue()));
         }
      };
      sliderTime.valueChangingProperty().addListener(sliderValueChangingListener);
   }

   private void bindSliderValueListener()
   {
      sliderValueListener = (observableValue, oldValue, newValue) -> {
         bindCurrentTimeLabel();
         double currentTime = mediaPlayer.getCurrentTime().toSeconds();
         if (Math.abs(currentTime - newValue.doubleValue()) > 0.5)
         {
            mediaPlayer.seek(javafx.util.Duration.seconds(newValue.doubleValue()));
         }
      };
      sliderTime.valueProperty().addListener(sliderValueListener);
   }

   private void bindMediaPlayerTotalDurationListener()
   {
      mediaPlayerTotalDurationListener = (observableValue, oldDuration, newDuration) -> {
         sliderTime.setMax(newDuration.toSeconds());
         labelTotalTime.setText(VideoTimeFormatEffect.format(newDuration));
      };
      mediaPlayer.totalDurationProperty().addListener(mediaPlayerTotalDurationListener);
   }

   private void bindMediaPlayerCurrentDurationListener()
   {
      mediaPlayerCurrentDurationListener = (observableValue, oldTime, newTime) -> {
         bindCurrentTimeLabel();
         if (!sliderTime.isValueChanging())
         {
            sliderTime.setValue(newTime.toSeconds());
         }
      };
      mediaPlayer.currentTimeProperty().addListener(mediaPlayerCurrentDurationListener);
   }

   private void bindCurrentTimeLabel()
   {
      if (mediaPlayer != null)
      {
         labelCurrentTime.textProperty().bind(
               Bindings.createStringBinding(() -> VideoTimeFormatEffect.format(mediaPlayer.getCurrentTime()) + " / ",
                     mediaPlayer.currentTimeProperty()
               )
         );
      }
   }
}