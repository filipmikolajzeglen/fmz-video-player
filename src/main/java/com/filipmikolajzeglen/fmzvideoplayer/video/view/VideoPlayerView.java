package com.filipmikolajzeglen.fmzvideoplayer.video.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.player.VideoPlayer;
import com.filipmikolajzeglen.fmzvideoplayer.video.Video;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoCommercialsPlaylist;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoMediaPlayer;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlaybackCoordinator;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerFactory;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlaylist;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoService;
import com.filipmikolajzeglen.fmzvideoplayer.video.audio.AudioNormalizer;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoFadeOutEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoMediaSizeEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoSliderStyleEffect;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoTimeFormatEffect;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import lombok.Getter;
import lombok.Setter;

@Getter
public class VideoPlayerView implements Initializable
{
   private static final Logger LOGGER = new Logger();

   //@formatter:off
   @FXML private VBox vBoxFullPanel;
   @FXML private HBox hBoxControls;
   @FXML private HBox hBoxVolume;
   @FXML private Button buttonPlayPauseRestart;
   @FXML private SVGPath buttonPlayPauseRestartSVG;
   @FXML private Button buttonNext;
   @FXML private SVGPath buttonNextSVG;
   @FXML private Label labelCurrentTime;
   @FXML private Label labelTotalTime;
   @FXML private Label labelCurrentEpisode;
   @FXML private Label labelFullScreen;
   @FXML private SVGPath labelFullScreenSVG;
   @FXML private Label labelSpeed;
   @FXML private Label labelVolume;
   @FXML private SVGPath labelVolumeSVG;
   @FXML private Slider sliderVolume;
   @FXML private Slider sliderTime;
   @FXML private StackPane stackPaneParent;
   @FXML private ImageView imageView;

   @Setter private VideoPlayer mediaPlayer;
   @Setter private boolean atEndOfVideo = false;
   @Setter private boolean isPlaying = true;
   @Setter private VideoPlaybackCoordinator videoPlaybackCoordinator;
   @Setter private VideoPlaybackSpeedView videoPlaybackSpeedView;
   @Setter private VideoEpisodeInfoView videoEpisodeInfoView;
   @Setter private VideoTimeSliderView videoTimeSliderView;
   @Setter private VideoVolumeView videoVolumeView;
   @Setter private VideoFullScreenView videoFullScreenView;
   @Setter private VideoMediaSizeEffect videoMediaSizeEffect;
   @Setter private VideoPlaylist videoPlaylist;
   @Setter private VideoFadeOutEffect videoFadeOutEffect;
   @Setter private VideoPlaybackButtonsView videoPlaybackButtonsView;
   @Setter private VideoMediaPlayer videoMediaPlayer;
   @Setter private VideoService videoService;
   @Setter private List<String> commercialPlaylist = new ArrayList<>();
   @Setter private boolean playingCommercial = false;
   @Setter private VideoCommercialsPlaylist videoCommercialsPlaylist;
   @Setter private AudioNormalizer audioNormalizer;
   //@formatter:on

   @Override
   public void initialize(URL url, ResourceBundle resourceBundle)
   {
      VideoPlayerFactory videoPlayerFactory = VideoPlayerFactory.of(this);
      videoPlayerFactory.setupAndStart();
   }

   public void initializeMediaPlayer(String videoPath)
   {
      LOGGER.info("Delegating media player initialization to MediaPlayerManager for path: " + videoPath);
      mediaPlayer = videoMediaPlayer.createAndSetupPlayer(videoPath);
   }

   public void initializeAllControlsSvgOnTheBeginning()
   {
      LOGGER.info("Initialize all controls svg on the beginning");
      videoPlaybackButtonsView.setToPause();
      videoPlaybackButtonsView.initializeNextButton();
   }

   public void setUpButtonHandlers()
   {
      buttonPlayPauseRestart.setOnAction(event -> handlePlayPauseRestart());
      buttonNext.setOnAction(event -> handleNextClick());
   }

   void handlePlayPauseRestart()
   {
      videoTimeSliderView.bindToMediaPlayer(mediaPlayer);
      videoPlaybackCoordinator.togglePlayPause();
   }

   void handleNextClick()
   {
      // audioNormalizer.stop(mediaPlayer);
      videoPlaybackCoordinator.next();
   }

   public void resetTimeSlider()
   {
      sliderTime.setValue(PlayerConstants.Playback.RESET_TIME_VALUE);
      VideoSliderStyleEffect.addColorToSlider(sliderTime, () -> PlayerConstants.Playback.RESET_TIME_VALUE);
   }

   public void playByDefault()
   {
      if (mediaPlayer != null && mediaPlayer.getStatus() == VideoPlayer.Status.READY)
      {
         Platform.runLater(() -> {
            mediaPlayer.play();
            setPlaying(true);
            LOGGER.info("MediaPlayer playback started via playByDefault().");
         });
      }
   }

   void updateCurrentTimeLabelIfNeeded()
   {
      if (mediaPlayer != null && !labelCurrentTime.textProperty().equals(labelTotalTime.textProperty()))
      {
         labelCurrentTime.textProperty().unbind();
         labelCurrentTime.setText(VideoTimeFormatEffect.format(mediaPlayer.getTotalDuration()) + " / ");
      }
   }

   public void play()
   {
      mediaPlayer.play();
      videoPlaybackButtonsView.setToPause();
      setPlaying(true);
      setAtEndOfVideo(false);
   }

   public void pause()
   {
      mediaPlayer.pause();
      videoPlaybackButtonsView.setToPlay();
      setPlaying(false);
   }

   public void replay()
   {
      sliderTime.setValue(PlayerConstants.Playback.RESET_TIME_VALUE);
      mediaPlayer.seek(javafx.util.Duration.ZERO);
      play();
   }

   void handleEndOfPlaylist()
   {
      videoPlaybackButtonsView.setToReplay();
      setAtEndOfVideo(true);
      setPlaying(false);
   }

   public void playNextEpisodeFromPlaylist()
   {
      if (videoPlaylist.hasNext())
      {
         Video nextVideo = videoPlaylist.getNextVideo();
         String nextVideoPath = videoPlaylist.getCurrentVideoPath();

         if (nextVideo != null && nextVideoPath != null)
         {
            LOGGER.info("Playing next episode: " + nextVideo.getEpisodeName());
            videoEpisodeInfoView.updateInfo(nextVideo);
            initializeMediaPlayer(nextVideoPath);
            resetTimeSlider();
            updateCurrentTimeLabelIfNeeded();
         }
      }
      else
      {
         LOGGER.info("End of playlist. Setting REPLAY button.");
         handleEndOfPlaylist();
      }
   }

   public void shutdown()
   {
      LOGGER.running("Shutting down VideoPlayer resources...");
      if (mediaPlayer != null)
      {
         // audioNormalizer.stop(mediaPlayer);
         mediaPlayer.stop();
         mediaPlayer.dispose();
         mediaPlayer = null;
         LOGGER.info("MediaPlayer stopped and disposed.");
      }
   }

}