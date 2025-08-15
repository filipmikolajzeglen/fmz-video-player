package com.filipmikolajzeglen.fmzvideoplayer.video.view;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoSliderStyleEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoIconsEffect;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import com.filipmikolajzeglen.fmzvideoplayer.player.VideoPlayer;
import javafx.scene.shape.SVGPath;

public class VideoVolumeView
{
   private static final Logger LOGGER = new Logger();

   private final HBox hBoxVolume;
   private final Slider sliderVolume;
   private final Label labelVolume;
   private final SVGPath labelVolumeSVG;
   private VideoPlayer videoPlayer;
   private boolean isMuted = false;
   private ChangeListener<Number> volumeChangeListener;

   public static VideoVolumeView of(VideoPlayerView videoPlayerView)
   {
      return new VideoVolumeView(videoPlayerView);
   }

   private VideoVolumeView(VideoPlayerView videoPlayerView)
   {
      this.hBoxVolume = videoPlayerView.getHBoxVolume();
      this.sliderVolume = videoPlayerView.getSliderVolume();
      this.labelVolume = videoPlayerView.getLabelVolume();
      this.labelVolumeSVG = videoPlayerView.getLabelVolumeSVG();

      this.sliderVolume.setValue(PlayerConstants.Playback.DEFAULT_VOLUME_VALUE);
      this.hBoxVolume.getChildren().remove(sliderVolume);
      setLabelVolume2SVG();

      this.sliderVolume.skinProperty().addListener((obs, oldSkin, newSkin) -> addColorToSliderVolume());

      this.labelVolume.setOnMouseClicked(event -> handleVolumeClick());
      this.labelVolume.setOnMouseEntered(event -> handleVolumeMouseEnter());
      this.hBoxVolume.setOnMouseExited(event -> handleVolumeMouseExit());
      this.hBoxVolume.setOnMouseEntered(event -> handleVolumeMouseEnter());
   }

   public void bindToVideoPlayer(VideoPlayer videoPlayer)
   {
      unbindVolume();
      this.videoPlayer = videoPlayer;
      if (videoPlayer == null)
      {
         return;
      }

      // Ustaw slider na aktualną głośność odtwarzacza
      sliderVolume.setValue(videoPlayer.getVolume());

      volumeChangeListener = (observable, oldValue, newValue) -> {
         int volume = (int) sliderVolume.getValue();
         videoPlayer.setVolume(volume); // VLCJPlayer używa 0-100
         updateVolumeIcon(volume);
         updateMuteState(volume);
         addColorToSliderVolume();
      };
      sliderVolume.valueProperty().addListener(volumeChangeListener);
      addColorToSliderVolume();
   }

   private void updateVolumeIcon(int volume)
   {
      if (volume == PlayerConstants.Playback.MUTE_VOLUME_VALUE)
      {
         setLabelVolumeMuteSVG();
      }
      else if (volume < PlayerConstants.Playback.DEFAULT_VOLUME_VALUE)
      {
         setLabelVolume1SVG();
      }
      else
      {
         setLabelVolume2SVG();
      }
   }

   private void updateMuteState(int volume)
   {
      isMuted = (volume == PlayerConstants.Playback.MUTE_VOLUME_VALUE);
   }

   public void unbindVolume()
   {
      if (volumeChangeListener != null)
      {
         sliderVolume.valueProperty().removeListener(volumeChangeListener);
      }
   }

   private void handleVolumeClick()
   {
      if (videoPlayer == null || sliderVolume == null)
      {
         return;
      }
      if (isMuted)
      {
         LOGGER.info("Unmuted video volume");
         setLabelVolume1SVG();
         videoPlayer.setVolume(PlayerConstants.Playback.DEFAULT_VOLUME_VALUE);
         sliderVolume.setValue(PlayerConstants.Playback.DEFAULT_VOLUME_VALUE);
         isMuted = false;
      }
      else
      {
         LOGGER.info("Muted video volume");
         setLabelVolumeMuteSVG();
         videoPlayer.setVolume(PlayerConstants.Playback.MUTE_VOLUME_VALUE);
         sliderVolume.setValue(PlayerConstants.Playback.MUTE_VOLUME_VALUE);
         isMuted = true;
      }
   }

   private void handleVolumeMouseEnter()
   {
      if (!hBoxVolume.getChildren().contains(sliderVolume))
      {
         hBoxVolume.getChildren().add(sliderVolume);
      }

      if (videoPlayer != null)
      {
         sliderVolume.setValue(videoPlayer.getVolume());
         addColorToSliderVolume();
      }
      else
      {
         LOGGER.error("Video player was NULL during handling volume mouse entered");
      }
   }

   private void handleVolumeMouseExit()
   {
      hBoxVolume.getChildren().remove(sliderVolume);
   }

   private void setLabelVolume1SVG()
   {
      VideoIconsEffect.setControlSVG(labelVolume, labelVolumeSVG, PlayerConstants.Icons.VOLUME1());
   }

   private void setLabelVolume2SVG()
   {
      VideoIconsEffect.setControlSVG(labelVolume, labelVolumeSVG, PlayerConstants.Icons.VOLUME2());
   }

   private void setLabelVolumeMuteSVG()
   {
      VideoIconsEffect.setControlSVG(labelVolume, labelVolumeSVG, PlayerConstants.Icons.MUTE());
   }

   private void addColorToSliderVolume()
   {
      VideoSliderStyleEffect.addColorToSlider(sliderVolume, sliderVolume::getValue);
   }
}