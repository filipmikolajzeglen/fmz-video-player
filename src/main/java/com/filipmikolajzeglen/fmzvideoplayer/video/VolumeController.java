package com.filipmikolajzeglen.fmzvideoplayer.video;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;

class VolumeController
{
   private static final Logger LOGGER = new Logger();

   private final HBox hBoxVolume;
   private final Slider sliderVolume;
   private final Label labelVolume;
   private final SVGPath labelVolumeSVG;
   private MediaPlayer mediaPlayer;
   private boolean isMuted = false;
   private ChangeListener<Number> volumeChangeListener;

   static VolumeController of(VideoPlayer videoPlayer)
   {
      return new VolumeController(videoPlayer);
   }

   private VolumeController(VideoPlayer videoPlayer)
   {
      this.hBoxVolume = videoPlayer.getHBoxVolume();
      this.sliderVolume = videoPlayer.getSliderVolume();
      this.labelVolume = videoPlayer.getLabelVolume();
      this.labelVolumeSVG = videoPlayer.getLabelVolumeSVG();

      this.sliderVolume.setValue(FMZVideoPlayerConfiguration.Playback.DEFAULT_VOLUME_VALUE);
      this.hBoxVolume.getChildren().remove(sliderVolume);
      setLabelVolume2SVG();

      this.sliderVolume.skinProperty().addListener((obs, oldSkin, newSkin) -> addColorToSliderVolume());

      this.labelVolume.setOnMouseClicked(event -> handleVolumeClick());
      this.labelVolume.setOnMouseEntered(event -> handleVolumeMouseEnter());
      this.hBoxVolume.setOnMouseExited(event -> handleVolumeMouseExit());
      this.hBoxVolume.setOnMouseEntered(event -> handleVolumeMouseEnter());
   }

   void bindToMediaPlayer(MediaPlayer mediaPlayer)
   {
      unbindVolume();
      this.mediaPlayer = mediaPlayer;
      if (mediaPlayer == null)
      {
         return;
      }

      volumeChangeListener = (observable, oldValue, newValue) -> {
         double volume = sliderVolume.getValue();
         mediaPlayer.setVolume(volume);
         updateVolumeIcon(volume);
         updateMuteState(volume);
         addColorToSliderVolume();
      };
      sliderVolume.valueProperty().addListener(volumeChangeListener);
      mediaPlayer.volumeProperty().bindBidirectional(sliderVolume.valueProperty());
      addColorToSliderVolume();
   }

   private void updateVolumeIcon(double volume)
   {
      if (volume == FMZVideoPlayerConfiguration.Playback.MUTE_VOLUME_VALUE)
      {
         setLabelVolumeMuteSVG();
      }
      else if (volume < FMZVideoPlayerConfiguration.Playback.DEFAULT_VOLUME_VALUE)
      {
         setLabelVolume1SVG();
      }
      else
      {
         setLabelVolume2SVG();
      }
   }

   private void updateMuteState(double volume)
   {
      isMuted = (volume == FMZVideoPlayerConfiguration.Playback.MUTE_VOLUME_VALUE);
   }

   void unbindVolume()
   {
      if (mediaPlayer != null)
      {
         sliderVolume.valueProperty().unbindBidirectional(mediaPlayer.volumeProperty());
      }
      if (volumeChangeListener != null)
      {
         sliderVolume.valueProperty().removeListener(volumeChangeListener);
      }
   }

   private void handleVolumeClick()
   {
      if (mediaPlayer == null || sliderVolume == null)
      {
         return;
      }
      if (isMuted)
      {
         LOGGER.info("Unmuted video volume");
         setLabelVolume1SVG();
         mediaPlayer.setVolume(FMZVideoPlayerConfiguration.Playback.DEFAULT_VOLUME_VALUE);
         sliderVolume.setValue(FMZVideoPlayerConfiguration.Playback.DEFAULT_VOLUME_VALUE);
         isMuted = false;
      }
      else
      {
         LOGGER.info("Muted video volume");
         setLabelVolumeMuteSVG();
         mediaPlayer.setVolume(FMZVideoPlayerConfiguration.Playback.MUTE_VOLUME_VALUE);
         sliderVolume.setValue(FMZVideoPlayerConfiguration.Playback.MUTE_VOLUME_VALUE);
         isMuted = true;
      }
   }

   private void handleVolumeMouseEnter()
   {
      if (!hBoxVolume.getChildren().contains(sliderVolume))
      {
         hBoxVolume.getChildren().add(sliderVolume);
      }

      if (mediaPlayer != null)
      {
         sliderVolume.setValue(mediaPlayer.getVolume());
         addColorToSliderVolume();
      }
      else
      {
         LOGGER.error("Media player was NULL during handling volume mouse entered");
      }
   }

   private void handleVolumeMouseExit()
   {
      hBoxVolume.getChildren().remove(sliderVolume);
   }

   private void setLabelVolume1SVG()
   {
      VideoPlayerIcons.setLabelVolumeSVG(labelVolume, labelVolumeSVG, FMZVideoPlayerConfiguration.Icons.VOLUME1(), -1.0);
   }

   private void setLabelVolume2SVG()
   {
      VideoPlayerIcons.setLabelVolumeSVG(labelVolume, labelVolumeSVG, FMZVideoPlayerConfiguration.Icons.VOLUME2(), 0.0);
   }

   private void setLabelVolumeMuteSVG()
   {
      VideoPlayerIcons.setLabelVolumeSVG(labelVolume, labelVolumeSVG, FMZVideoPlayerConfiguration.Icons.MUTE(), -0.2);
   }

   private void addColorToSliderVolume()
   {
      SliderStyler.addColorToSlider(sliderVolume, sliderVolume::getValue);
   }
}