package com.filipmikolajzeglen.fmzvideoplayer.video;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

class SliderController
{
   private final Slider sliderTime;
   private final Label labelCurrentTime;
   private final Label labelTotalTime;
   private ChangeListener<Boolean> sliderValueChangingListener;
   private ChangeListener<Number> sliderValueListener;
   private ChangeListener<Duration> mediaPlayerTotalDurationListener;
   private ChangeListener<Duration> mediaPlayerCurrentDurationListener;
   private MediaPlayer mediaPlayer;

   static SliderController of(VideoPlayer videoPlayer)
   {
      return new SliderController(videoPlayer);
   }

   private SliderController(VideoPlayer videoPlayer)
   {
      this.sliderTime = videoPlayer.getSliderTime();
      this.labelCurrentTime = videoPlayer.getLabelCurrentTime();
      this.labelTotalTime = videoPlayer.getLabelTotalTime();
      SliderStyler.addDynamicColorListener(this.sliderTime);
   }

   void bindToMediaPlayer(MediaPlayer mediaPlayer)
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

   void unbindListeners()
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
            mediaPlayer.seek(Duration.seconds(sliderTime.getValue()));
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
            mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
         }
      };
      sliderTime.valueProperty().addListener(sliderValueListener);
   }

   private void bindMediaPlayerTotalDurationListener()
   {
      mediaPlayerTotalDurationListener = (observableValue, oldDuration, newDuration) -> {
         sliderTime.setMax(newDuration.toSeconds());
         labelTotalTime.setText(TimeFormatter.format(newDuration));
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
               Bindings.createStringBinding(() -> TimeFormatter.format(mediaPlayer.getCurrentTime()) + " / ",
                     mediaPlayer.currentTimeProperty()
               )
         );
      }
   }
}