package com.filipmikolajzeglen.fmzvideoplayer.video.view;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.scene.control.Label;
import javafx.scene.media.MediaPlayer;

public class VideoPlaybackSpeedView
{
   private static final Logger LOGGER = new Logger();

   private final Label speedLabel;
   private MediaPlayer mediaPlayer;

   public static VideoPlaybackSpeedView of(VideoPlayerView videoPlayerView)
   {
      return new VideoPlaybackSpeedView(videoPlayerView);
   }

   private VideoPlaybackSpeedView(VideoPlayerView videoPlayerView)
   {
      this.speedLabel = videoPlayerView.getLabelSpeed();
      this.speedLabel.setText(PlayerConstants.Playback.SPEED_LEVEL_1);
      this.speedLabel.setOnMouseClicked(event -> cycleSpeed());
   }

   public void setMediaPlayer(MediaPlayer mediaPlayer)
   {
      this.mediaPlayer = mediaPlayer;
      if (mediaPlayer != null)
      {
         mediaPlayer.setRate(PlayerConstants.Playback.SPEED_LEVEL_1_VALUE);
         speedLabel.setText(PlayerConstants.Playback.SPEED_LEVEL_1);
      }
   }

   private void cycleSpeed()
   {
      if (mediaPlayer == null)
      {
         return;
      }

      if (PlayerConstants.Playback.SPEED_LEVEL_1.equals(speedLabel.getText()))
      {
         LOGGER.info("Speed video was set to " + PlayerConstants.Playback.SPEED_LEVEL_2);
         mediaPlayer.setRate(PlayerConstants.Playback.SPEED_LEVEL_2_VALUE);
         speedLabel.setText(PlayerConstants.Playback.SPEED_LEVEL_2);
      }
      else if (PlayerConstants.Playback.SPEED_LEVEL_2.equals(speedLabel.getText()))
      {
         LOGGER.info("Speed video was set to " + PlayerConstants.Playback.SPEED_LEVEL_3);
         mediaPlayer.setRate(PlayerConstants.Playback.SPEED_LEVEL_3_VALUE);
         speedLabel.setText(PlayerConstants.Playback.SPEED_LEVEL_3);
      }
      else if (PlayerConstants.Playback.SPEED_LEVEL_3.equals(speedLabel.getText()))
      {
         LOGGER.info("Speed video was set to " + PlayerConstants.Playback.SPEED_LEVEL_4);
         mediaPlayer.setRate(PlayerConstants.Playback.SPEED_LEVEL_4_VALUE);
         speedLabel.setText(PlayerConstants.Playback.SPEED_LEVEL_4);
      }
      else
      {
         LOGGER.info("Speed video was restart to " + PlayerConstants.Playback.SPEED_LEVEL_1);
         mediaPlayer.setRate(PlayerConstants.Playback.SPEED_LEVEL_1_VALUE);
         speedLabel.setText(PlayerConstants.Playback.SPEED_LEVEL_1);
      }
   }
}
