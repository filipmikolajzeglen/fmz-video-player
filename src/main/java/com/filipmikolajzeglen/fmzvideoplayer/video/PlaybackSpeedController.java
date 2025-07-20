package com.filipmikolajzeglen.fmzvideoplayer.video;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.scene.control.Label;
import javafx.scene.media.MediaPlayer;

class PlaybackSpeedController
{
   private static final Logger LOGGER = new Logger();

   private final Label speedLabel;
   private MediaPlayer mediaPlayer;

   static PlaybackSpeedController of(VideoPlayer videoPlayer)
   {
      return new PlaybackSpeedController(videoPlayer);
   }

   private PlaybackSpeedController(VideoPlayer videoPlayer)
   {
      this.speedLabel = videoPlayer.getLabelSpeed();
      this.speedLabel.setText(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_1);
      this.speedLabel.setOnMouseClicked(event -> cycleSpeed());
   }

   void setMediaPlayer(MediaPlayer mediaPlayer)
   {
      this.mediaPlayer = mediaPlayer;
      if (mediaPlayer != null)
      {
         mediaPlayer.setRate(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_1_VALUE);
         speedLabel.setText(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_1);
      }
   }

   private void cycleSpeed()
   {
      if (mediaPlayer == null)
      {
         return;
      }

      if (FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_1.equals(speedLabel.getText()))
      {
         LOGGER.info("Speed video was set to " + FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_2);
         mediaPlayer.setRate(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_2_VALUE);
         speedLabel.setText(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_2);
      }
      else if (FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_2.equals(speedLabel.getText()))
      {
         LOGGER.info("Speed video was set to " + FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_3);
         mediaPlayer.setRate(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_3_VALUE);
         speedLabel.setText(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_3);
      }
      else if (FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_3.equals(speedLabel.getText()))
      {
         LOGGER.info("Speed video was set to " + FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_4);
         mediaPlayer.setRate(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_4_VALUE);
         speedLabel.setText(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_4);
      }
      else
      {
         LOGGER.info("Speed video was restart to " + FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_1);
         mediaPlayer.setRate(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_1_VALUE);
         speedLabel.setText(FMZVideoPlayerConfiguration.Playback.SPEED_LEVEL_1);
      }
   }
}
