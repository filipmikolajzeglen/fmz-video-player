package com.filipmikolajzeglen.video;

import com.filipmikolajzeglen.logger.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.filipmikolajzeglen.video.VideoPlayerConfiguration.*;

public class VideoPlayer implements Initializable {

    private final Logger logger = new Logger();

    @FXML
    private StackPane stackPaneParent;

    @FXML
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    private Media media;

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

    private boolean atEndOfVideo = false;
    private boolean isPlaying = true;
    private boolean isMuted = false;

    private ChangeListener<Boolean> sliderValueChangingListener;
    private ChangeListener<Number> sliderValueListener;
    private ChangeListener<Duration> mediaPlayerTotalDurationListener;
    private ChangeListener<Duration> mediaPlayerCurrentDurationListener;

    private final VideoPlayerService videoPlayerService = new VideoPlayerService();
    private Iterator<String> pathToVideoIterator;
    private Iterator<Video> videoIterator;
    private Video currentVideo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        videoPlayerService.initializeFMZDB();
        pathToVideoIterator = videoPlayerService.createPathsToAllVideos().listIterator();
        videoIterator = videoPlayerService.createVideosSchedule().listIterator();

        stopAndDisposeMediaPlayer();
        logInitialization();

        if (!pathToVideoIterator.hasNext()) {
            return;
        }

        currentVideo = videoIterator.next();
        initializeVideoInfo(currentVideo);
        initializeMediaPlayer(pathToVideoIterator.next());
        initializeAllControlsSvgOnTheBeginning();
        setUpFadeOutFeature();

        assertButtonPlayPauseRestartInitialized();

        CompletableFuture.runAsync(this::waitForMediaPlayerReadyAndPlay);
    }

    private void logInitialization() {
        logger.info("Initialize FMZ Video Player with videos:");
        logger.info(videoPlayerService.createVideoScheduleLog());
    }

    private void assertButtonPlayPauseRestartInitialized() {
        if (buttonPlayPauseRestart == null) {
            logger.error("buttonPlayPauseRestart was NULL");
            throw new IllegalStateException("buttonPlayPauseRestart must be initialized");
        }

        logger.info("buttonPlayPauseRestart was correctly initialized");
        setUpButtonHandlers();
    }

    private void waitForMediaPlayerReadyAndPlay() {
        try {
            while (mediaPlayer.getStatus() != MediaPlayer.Status.READY) {
                Thread.sleep(2);
            }

            Platform.runLater(mediaPlayer::play);
        } catch (InterruptedException e) {
            logger.info("Something goes wrong during waiting for ready status");
            logger.error(e.getMessage());
        }

    }

    private void stopAndDisposeMediaPlayer() {
        if (mediaPlayer != null) {
            sliderTime.valueChangingProperty().removeListener(sliderValueChangingListener);
            sliderTime.valueProperty().removeListener(sliderValueListener);
            mediaPlayer.totalDurationProperty().removeListener(mediaPlayerTotalDurationListener);
            mediaPlayer.currentTimeProperty().removeListener(mediaPlayerCurrentDurationListener);

            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            logger.info("Listeners were removed. Media player was stopped and disposed then set to NULL");
        }
    }

    private void initializeMediaPlayer(String videoPath) {
        logger.info("Initialize media player. Current video path: " + videoPath);
        media = new Media(new File(videoPath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        volumeBinding();
        videoPlayerSizeBinding();
        videoTimeBinding();

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.getTotalDuration().lessThanOrEqualTo(Duration.ZERO)) {
                    logger.error("Total duration was 00:00 - Started new initialization");
                    initializeMediaPlayer(videoPath);
                }
            }
        });

        mediaPlayer.setOnError(new Runnable() {
            @Override
            public void run() {
                logger.error("Error occurred in media player: " + mediaPlayer.getError().getMessage());
                logger.warning("Try to initialize media player once again after error");
                initializeMediaPlayer(videoPath);
            }
        });
    }

    private void initializeMediaPlayer(final Iterator<String> urls, final Iterator<Video> videos) {
        stopAndDisposeMediaPlayer();
        initializeVideoInfoIfPresent(videos);
        initializeMediaPlayerIfUrlPresent(urls);
        updateCurrentTimeLabelIfNeeded();
    }

    private void initializeVideoInfoIfPresent(final Iterator<Video> videos) {
        if (videos.hasNext()) {
            currentVideo = videos.next();
            initializeVideoInfo(currentVideo);
        }
    }

    private void initializeVideoInfo(Video video) {
        String fullEpisodeInfo = String.format("%s - %s", video.getSeriesName().toUpperCase(), video.getEpisodeName());
        logger.info("Current video info: " + fullEpisodeInfo);
        labelCurrentEpisode.setText(fullEpisodeInfo);
    }

    private void initializeMediaPlayerIfUrlPresent(final Iterator<String> urls) {
        if (urls.hasNext()) {
            initializeMediaPlayer(urls.next());
            resetTimeSlider();
            configureCurrentTimeListener();
            configureTotalDurationListener();
            configurePlayPauseRestartAction();
            playByDefault();
        } else {
            logger.info("Next video does not exist, set RESTART button");
            showRestartButton();
        }
    }

    private void initializeAllControlsSvgOnTheBeginning() {
        logger.info("Initialize all controls svg on the beginning");
        setButtonPauseSVG();
        setButtonNextSVG();
        setLabelVolume2SVG();
        setLabelExitFullscreenSVG();

        sliderVolume.setValue(DEFAULT_VOLUME_VALUE);
        labelSpeed.setText(SPEED_LEVEL_1);
        hBoxVolume.getChildren().remove(sliderVolume);
    }

    private void setUpFadeOutFeature() {
        PauseTransition delayFadeOut = new PauseTransition(Duration.seconds(3));
        delayFadeOut.setOnFinished(e -> fadeOutPane(vBoxFullPanel));

        mediaView.setOnMouseMoved(evt -> delayFadeOut.playFromStart());
        vBoxFullPanel.setOnMouseExited(evt -> delayFadeOut.playFromStart());
        vBoxFullPanel.setOnMouseEntered(evt -> {
            vBoxFullPanel.setOpacity(1);
            delayFadeOut.stop();
        });
    }

    private void fadeOutPane(VBox pane) {
        Timeline fadeOutTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(pane.opacityProperty(), 1.0)),
                new KeyFrame(new Duration(1000), new KeyValue(pane.opacityProperty(), 0.0))
        );
        fadeOutTimeline.play();
    }

    private void setUpButtonHandlers() {
        buttonPlayPauseRestart.setOnAction(event -> handlePlayPauseRestart());
        labelFullScreen.setOnMouseClicked(event -> handleFullscreenClick());
        labelSpeed.setOnMouseClicked(event -> handleSpeedClick());

        labelVolume.setOnMouseClicked(event -> handleVolumeClick());
        labelVolume.setOnMouseEntered(event -> handleVolumeMouseEnter());
        hBoxVolume.setOnMouseExited(event -> handleVolumeMouseExit());
        hBoxVolume.setOnMouseEntered(event -> handleVolumeMouseEnter());
    }

    private void volumeBinding() {
        sliderVolume.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(sliderVolume.getValue());
                if (mediaPlayer.getVolume() != MUTE_VOLUME_VALUE) {
                    if (mediaPlayer.getVolume() < DEFAULT_VOLUME_VALUE) {
                        labelVolumeSVG.setContent(VOLUME1_SVG);
                    } else {
                        labelVolumeSVG.setContent(VOLUME2_SVG);
                    }
                    labelVolume.setGraphic(labelVolumeSVG);
                    isMuted = false;
                } else {
                    labelVolumeSVG.setContent(MUTE_SVG);
                    labelVolume.setGraphic(labelVolumeSVG);
                    isMuted = true;
                }

                addColorToSliderVolume(sliderVolume);
            }
        });
        mediaPlayer.volumeProperty().bindBidirectional(sliderVolume.valueProperty());
    }

    private void videoPlayerSizeBinding() {
        stackPaneParent.sceneProperty().addListener((observableValue, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                mediaView.fitHeightProperty()
                        .bind(newScene.heightProperty()
                                .subtract(hBoxControls.heightProperty().add(0)));
                mediaView.fitWidthProperty().bind(newScene.widthProperty());
            }
        });
        mediaView.setPreserveRatio(false);
    }

    private void videoTimeBinding() {

        sliderValueChangingListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean isChanging) {
                bindCurrentTimeLabel();
                if (!isChanging) {
                    mediaPlayer.seek(Duration.seconds(sliderTime.getValue()));
                }
            }
        };
        sliderTime.valueChangingProperty().addListener(sliderValueChangingListener);

        sliderValueListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                bindCurrentTimeLabel();
                if (mediaPlayer != null) {
                    double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                    if (Math.abs(currentTime - newValue.doubleValue()) > 0.5) {
                        mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                        logger.info("Moved slider time to: " + getFormattedCurrentVideoTime());
                    }
                    addColorToSliderTime(sliderTime, newValue);
                } else {
                    logger.error("sliderTime.valueProperty(): mediaPlayer is currently NULL");
                }
            }
        };
        sliderTime.valueProperty().addListener(sliderValueListener);

        mediaPlayerTotalDurationListener = new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
                sliderTime.setMax(newDuration.toSeconds());
                labelTotalTime.setText(getTime(newDuration));
            }
        };
        mediaPlayer.totalDurationProperty().addListener(mediaPlayerTotalDurationListener);

        mediaPlayerCurrentDurationListener = new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTime, Duration newTime) {
                bindCurrentTimeLabel();
                if (!sliderTime.isValueChanging()) {
                    sliderTime.setValue(newTime.toSeconds());
                }
            }
        };
        mediaPlayer.currentTimeProperty().addListener(mediaPlayerCurrentDurationListener);

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                currentVideo.setWatched(true);
                videoPlayerService.getDatabase().save(currentVideo);
                if (pathToVideoIterator.hasNext() && videoIterator.hasNext()) {
                    logger.info("====================== NEXT VIDEO IS INITIALIZING ======================");
                    initializeMediaPlayer(pathToVideoIterator, videoIterator);
                } else {
                    logger.info("======================  ALL VIDEOS WERE WATCHED  =======================");
                    showRestartButton();
                }
            }
        });
    }

    private void handlePlayPauseRestart() {
        bindCurrentTimeLabel();
        logger.info("Is end of video: " + atEndOfVideo);
        if (atEndOfVideo) {
            logger.info("Slider time value was set to 0");
            sliderTime.setValue(0);
            atEndOfVideo = false;
            isPlaying = false;
        }

        if (isPlaying) {
            logger.info("Video is pause. Button PLAY was set");
            setButtonPlaySVG();
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            logger.info("Video is playing. Button PAUSE was set");
            setButtonPauseSVG();
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    private void handleFullscreenClick() {
        Stage stage = (Stage) stackPaneParent.getScene().getWindow();

        if (stage.isFullScreen()) {
            logger.info("Fullscreen was exited");
            stage.setFullScreen(false);
            setLabelEnterFullscreenSVG();
        } else {
            logger.info("Fullscreen was entered");
            stage.setFullScreen(true);
            setLabelExitFullscreenSVG();
            stage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    logger.info("Button ESCAPE was clicked");
                    setLabelExitFullscreenSVG();
                }
            });
        }
    }

    private void handleSpeedClick() {
        if (mediaPlayer == null) {
            logger.error("Media player was NULL during handling speed click");
            return;
        }

        if (SPEED_LEVEL_1.equals(labelSpeed.getText())) {
            logger.info("Speed video was set to " + SPEED_LEVEL_1);
            mediaPlayer.setRate(SPEED_LEVEL_2_VALUE);
            labelSpeed.setText(SPEED_LEVEL_2);
        } else if (SPEED_LEVEL_2.equals(labelSpeed.getText())) {
            logger.info("Speed video was set to " + SPEED_LEVEL_2);
            mediaPlayer.setRate(SPEED_LEVEL_3_VALUE);
            labelSpeed.setText(SPEED_LEVEL_3);
        } else if (SPEED_LEVEL_3.equals(labelSpeed.getText())) {
            logger.info("Speed video was set to " + SPEED_LEVEL_3);
            mediaPlayer.setRate(SPEED_LEVEL_4_VALUE);
            labelSpeed.setText(SPEED_LEVEL_4);
        } else {
            logger.info("Speed video was restart to " + SPEED_LEVEL_1);
            mediaPlayer.setRate(SPEED_LEVEL_1_VALUE);
            labelSpeed.setText(SPEED_LEVEL_1);
        }
    }

    private void handleVolumeClick() {
        if (mediaPlayer == null || sliderVolume == null) {
            logger.error("Media player or slider volume was NULL during handling volume click");
            return;
        }

        if (isMuted) {
            logger.info("Unmuted video volume");
            setLabelVolume1SVG();
            mediaPlayer.setVolume(DEFAULT_VOLUME_VALUE);
            sliderVolume.setValue(DEFAULT_VOLUME_VALUE);
            isMuted = false;
        } else {
            logger.info("Muted video volume");
            setLabelVolumeMuteSVG();
            mediaPlayer.setVolume(MUTE_VOLUME_VALUE);
            sliderVolume.setValue(MUTE_VOLUME_VALUE);
            isMuted = true;
        }
    }

    private void handleVolumeMouseEnter() {
        if (!hBoxVolume.getChildren().contains(sliderVolume)) {
            hBoxVolume.getChildren().add(sliderVolume);
            addColorToSliderVolume(sliderVolume);
        }

        if (mediaPlayer != null) {
            sliderVolume.setValue(mediaPlayer.getVolume());
            addColorToSliderVolume(sliderVolume);
        } else {
            logger.error("Media player was NULL during handling volume mouse entered");
        }
    }

    private void handleVolumeMouseExit() {
        hBoxVolume.getChildren().remove(sliderVolume);
    }

    private void resetTimeSlider() {
        sliderTime.setValue(RESET_TIME_VALUE);
    }

    private void configureTotalDurationListener() {
        mediaPlayer.totalDurationProperty().addListener((observableValue, oldDuration, newDuration) -> {
            sliderTime.setMax(newDuration.toSeconds());
            labelTotalTime.setText(getTime(newDuration));
            logger.info("Total duartion was set to: " + labelTotalTime.getText());
        });
    }

    private void configureCurrentTimeListener() {
        mediaPlayer.currentTimeProperty().addListener((observableValue, oldTime, newTime) -> {
            bindCurrentTimeLabel();
            if (!sliderTime.isValueChanging()) {
                sliderTime.setValue(newTime.toSeconds());
            }
        });
    }

    private void configurePlayPauseRestartAction() {
        buttonPlayPauseRestart.setOnAction(actionEvent -> {
            bindCurrentTimeLabel();
            if (atEndOfVideo) {
                logger.info("Video is restarted");
                resetVideoIfEnded();
            }
            if (isPlaying) {
                logger.info("Video is paused");
                pause();
            } else {
                logger.info("Video is played");
                play();
            }
        });
    }

    private void resetVideoIfEnded() {
        sliderTime.setValue(RESET_TIME_VALUE);
        atEndOfVideo = false;
        isPlaying = false;
    }

    private void pause() {
        setButtonPlaySVG();
        mediaPlayer.pause();
        isPlaying = false;
    }

    private void play() {
        setButtonPauseSVG();
        mediaPlayer.play();
        isPlaying = true;
    }

    private void playByDefault() {
        mediaPlayer.setAutoPlay(true);
        setButtonPauseSVG();
    }

    private void showRestartButton() {
        setButtonRestartSVG();
    }

    private void updateCurrentTimeLabelIfNeeded() {
        if (!labelCurrentTime.textProperty().equals(labelTotalTime.textProperty())) {
            labelCurrentTime.textProperty().unbind();
            labelCurrentTime.setText(getTime(this.mediaPlayer.getTotalDuration()) + " / ");
        }
    }

    @FXML
    private void buttonNextClicked(MouseEvent event) {
        logger.info("====================== NEXT VIDEO IS INITIALIZING ======================");
        initializeMediaPlayer(pathToVideoIterator, videoIterator);
    }

    private String getFormattedCurrentVideoTime() {
        Duration duration = mediaPlayer.getCurrentTime();

        long minutes = (long) Math.floor(duration.toMinutes());
        long seconds = (long) Math.floor(duration.toSeconds() - minutes * 60);

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * This function takes the time of the video and calculates the seconds, minutes, and hours.
     *
     * @param time - The time of the video.
     * @return Corrected seconds, minutes, and hours.
     */
    public String getTime(Duration time) {
        if (time == null) {
            return "00:00";
        }

        double totalSeconds = time.toSeconds();
        int hours = (int) totalSeconds / 3600;
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Binds the text of the current time label to the current time of the video.
     * This method allows the timer to update in synchrony with the video. If the mediaPlayer is NULL,
     * an error message is logged.

     * Binding created using the createStringBinding method to invoke a Callable,
     * which is responsible for returning the formatted time string with hours, minutes and seconds.
     * The time is expressed in milliseconds e.g., 750.0 ms.
     */
    public void bindCurrentTimeLabel() {
        if (mediaPlayer != null) {
            labelCurrentTime.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String time = getTime(mediaPlayer.getCurrentTime()) + " / ";
                    return time;
                }
            }, mediaPlayer.currentTimeProperty()));
        } else {
            logger.error("bindCurrentTimeLabel: mediaPlayer is currently NULL");
        }
    }

    private void setButtonPlaySVG() {
        setSvgGraphic(buttonPlayPauseRestartSVG, PLAY_SVG, PLAY_SVG_SCALE_MODIFIER);
        buttonPlayPauseRestart.setGraphic(buttonPlayPauseRestartSVG);
    }

    private void setButtonPauseSVG() {
        setSvgGraphic(buttonPlayPauseRestartSVG, PAUSE_SVG, PAUSE_SVG_SCALE_MODIFIER);
        buttonPlayPauseRestart.setGraphic(buttonPlayPauseRestartSVG);
    }

    private void setButtonRestartSVG() {
        setSvgGraphic(buttonPlayPauseRestartSVG, RESTART_SVG, RESTART_SVG_SCALE_MODIFIER);
        buttonPlayPauseRestart.setGraphic(buttonPlayPauseRestartSVG);
    }

    private void setButtonNextSVG() {
        setSvgGraphic(buttonNextSVG, NEXT_SVG, NEXT_SVG_SCALE_MODIFIER);
    }

    private void setLabelEnterFullscreenSVG() {
        setSvgGraphic(labelFullScreenSVG, FULLSCREEN_SVG, SCREEN_MODE_SVG_SCALE_MODIFIER);
        labelFullScreen.setGraphic(labelFullScreenSVG);
    }

    private void setLabelExitFullscreenSVG() {
        setSvgGraphic(labelFullScreenSVG, EXIT_SVG, SCREEN_MODE_SVG_SCALE_MODIFIER);
        labelFullScreen.setGraphic(labelFullScreenSVG);
    }

    private void setLabelVolume1SVG() {
        setSvgGraphic(labelVolumeSVG, VOLUME1_SVG, VOLUME_SVG_SCALE_MODIFIER);
        labelVolume.setGraphic(labelVolumeSVG);
    }

    private void setLabelVolume2SVG() {
        setSvgGraphic(labelVolumeSVG, VOLUME2_SVG, VOLUME_SVG_SCALE_MODIFIER);
        labelVolume.setGraphic(labelVolumeSVG);
    }

    private void setLabelVolumeMuteSVG() {
        setSvgGraphic(labelVolumeSVG, MUTE_SVG, VOLUME_SVG_SCALE_MODIFIER);
        labelVolume.setGraphic(labelVolumeSVG);
    }

    private void setSvgGraphic(SVGPath svgPath, String svgCode, double svgScaleModifier) {
        svgPath.setContent(svgCode);
        svgPath.setScaleX(DEFAULT_SVG_SCALE * svgScaleModifier);
        svgPath.setScaleY(DEFAULT_SVG_SCALE * svgScaleModifier);
    }

    private void addColorToSliderVolume(Slider sliderVolume) {
        addColorToSlider(sliderVolume, sliderVolume::getValue);
    }

    private void addColorToSliderTime(Slider sliderTime, Number newValue) {
        addColorToSlider(sliderTime, newValue::doubleValue);
    }

    private void addColorToSlider(Slider slider, Supplier<Double> valueSupplier) {
        Node node = slider.lookup(".track");
        if (node != null) {
            double percentage = calculatePercentage(slider, valueSupplier);
            node.setStyle(generateStyle(percentage));
        }
    }

    private double calculatePercentage(Slider slider, Supplier<Double> valueSupplier) {
        return (valueSupplier.get() / slider.getMax()) * 100;
    }

    private String generateStyle(double percentage) {
        return String.format(Locale.US, "-fx-background-color: linear-gradient(to right, %s %f%% , %s %f%%);",
                PRIMARY_COLOR, percentage, GRADIENT_COLOR, percentage);
    }

}