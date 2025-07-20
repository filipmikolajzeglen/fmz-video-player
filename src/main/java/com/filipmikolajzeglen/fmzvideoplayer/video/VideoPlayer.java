package com.filipmikolajzeglen.fmzvideoplayer.video;

import static javafx.scene.media.MediaPlayer.*;

import java.net.URL;
import java.util.ResourceBundle;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.SVGPath;
import lombok.Getter;
import lombok.Setter;

@Getter
public class VideoPlayer implements Initializable
{
   private static final Logger LOGGER = new Logger();

   @FXML
   private VBox vBoxFullPanel;
   @FXML
   private HBox hBoxControls;
   @FXML
   private HBox hBoxVolume;
   @FXML
   private Button buttonPlayPauseRestart;
   @FXML
   private SVGPath buttonPlayPauseRestartSVG;
   @FXML
   private Button buttonNext;
   @FXML
   private SVGPath buttonNextSVG;
   @FXML
   private Label labelCurrentTime;
   @FXML
   private Label labelTotalTime;
   @FXML
   private Label labelCurrentEpisode;
   @FXML
   private Label labelFullScreen;
   @FXML
   private SVGPath labelFullScreenSVG;
   @FXML
   private Label labelSpeed;
   @FXML
   private Label labelVolume;
   @FXML
   private SVGPath labelVolumeSVG;
   @FXML
   private Slider sliderVolume;
   @FXML
   private Slider sliderTime;
   @FXML
   private StackPane stackPaneParent;
   @FXML
   private MediaView mediaView;

   @Setter
   private MediaPlayer mediaPlayer;
   @Setter
   private boolean atEndOfVideo = false;
   @Setter
   private boolean isPlaying = true;
   @Setter
   private PlaybackController playbackController;
   @Setter
   private PlaybackSpeedController playbackSpeedController;
   @Setter
   private EpisodeInfoController episodeInfoController;
   @Setter
   private SliderController sliderController;
   @Setter
   private VolumeController volumeController;
   @Setter
   private FullScreenController fullScreenController;
   @Setter
   private MediaViewResizer mediaViewResizer;
   @Setter
   private PlaylistManager playlistManager;
   @Setter
   private FadeOutManager fadeOutManager;
   @Setter
   private PlaybackButtonController playbackButtonController;
   @Setter
   private MediaPlayerManager mediaPlayerManager;
   @Setter
   private VideoPlayerService videoPlayerService;

   @Override
   public void initialize(URL url, ResourceBundle resourceBundle)
   {
      VideoPlayerSetup videoPlayerSetup = new VideoPlayerSetup(this);
      videoPlayerSetup.setupAndStart();
   }

   void waitForMediaPlayerReadyAndPlay()
   {
      if (mediaPlayer == null)
      {
         LOGGER.error("MediaPlayer is null");
         return;
      }
      mediaPlayer.statusProperty().addListener(this::onMediaPlayerStatusChanged);
   }

   private void onMediaPlayerStatusChanged(ObservableValue<? extends Status> obs, Status oldStatus, Status newStatus)
   {
      if (newStatus == Status.READY)
      {
         Platform.runLater(mediaPlayer::play);
         LOGGER.info("MediaPlayer is ready and started playing.");
      }
   }

   void initializeMediaPlayer(String videoPath)
   {
      LOGGER.info("Delegating media player initialization to MediaPlayerManager for path: " + videoPath);
      mediaPlayer = mediaPlayerManager.createAndSetupPlayer(videoPath);
      mediaView.setMediaPlayer(mediaPlayer);
   }

   void initializeAllControlsSvgOnTheBeginning()
   {
      LOGGER.info("Initialize all controls svg on the beginning");
      playbackButtonController.setToPause();
      playbackButtonController.initializeNextButton();
   }

   void setUpButtonHandlers()
   {
      buttonPlayPauseRestart.setOnAction(event -> handlePlayPauseRestart());
      buttonNext.setOnAction(event -> handleNextClick());
   }

   void handlePlayPauseRestart()
   {
      sliderController.bindToMediaPlayer(mediaPlayer);
      playbackController.togglePlayPause();
   }

   void handleNextClick()
   {
      mediaPlayerManager.stopAudioNormalization(mediaPlayer);
      playbackController.next();
   }

   void resetTimeSlider()
   {
      sliderTime.setValue(FMZVideoPlayerConfiguration.Playback.RESET_TIME_VALUE);
      SliderStyler.addColorToSlider(sliderTime, () -> FMZVideoPlayerConfiguration.Playback.RESET_TIME_VALUE);
   }

   void playByDefault()
   {
      mediaPlayer.setAutoPlay(true);
      playbackButtonController.setToPause();
   }

   void updateCurrentTimeLabelIfNeeded()
   {
      if (mediaPlayer != null && !labelCurrentTime.textProperty().equals(labelTotalTime.textProperty()))
      {
         labelCurrentTime.textProperty().unbind();
         labelCurrentTime.setText(TimeFormatter.format(mediaPlayer.getTotalDuration()) + " / ");
      }
   }
}