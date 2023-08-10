package com.filip.tvscheduler.fmztvscheduler.video;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
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
import java.util.function.Supplier;

import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.*;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoSimpleLogger.Level.ERROR;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoSimpleLogger.Level.INFO;

public class VideoPlayer implements Initializable {

    private static VideoSimpleLogger logger = new VideoSimpleLogger();

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

    private final VideoScheduleService scheduler = new VideoScheduleService();
    private final Iterator<String> pathToVideoIterator = scheduler.createPathsToAllVideos().listIterator();
    private final Iterator<Video> videoIterator = scheduler.createVideosSchedule().listIterator();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stopAndDisposeMediaPlayer();

        logger.log(INFO, "Initialize FMZ Video Player with videos:");
        logger.log(INFO, scheduler.createVideoScheduleLog());

        if (pathToVideoIterator.hasNext()) {
            initializeMediaPlayer(pathToVideoIterator.next());
            initializeVideoInfo(videoIterator.next());
            initializeAllControlsSvgOnTheBeginning();

            setUpFadeOutFeature();
            setUpButtonHandlers();

            volumeBinding();
            videoPlayerSizeBinding();
            videoTimeBinding();

            mediaPlayer.play();
        }
    }

    private void stopAndDisposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            logger.log(INFO, "Media player was stopped and disposed. Set to NULL");
        }
    }

    private void initializeMediaPlayer(String videoPath) {
        logger.log(INFO, "Initialize media player");
        logger.log(INFO, "Current video path: " + videoPath);
        media = new Media(new File(videoPath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
    }

    private void initializeMediaPlayer(final Iterator<String> urls, final Iterator<Video> videos) {
        logger.log(INFO, "Initialize media player once again");
        stopAndDisposeMediaPlayer();
        initializeVideoIfPresent(videos);
        initializeMediaPlayerIfUrlPresent(urls);
        atEndOfVideo = true;
        updateCurrentTimeLabelIfNeeded();
    }

    private void initializeVideoIfPresent(final Iterator<Video> videos) {
        if (videos.hasNext()) {
            logger.log(INFO, "Next video exist");
            initializeVideoInfo(videos.next());
        }
    }

    private void initializeVideoInfo(Video video) {
        String fullEpisodeInfo = String.format("%s - %s", video.getVideoName().toUpperCase(), video.getEpisodeName());
        logger.log(INFO, "Current video info: " + fullEpisodeInfo);
        labelCurrentEpisode.setText(fullEpisodeInfo);
    }

    private void initializeMediaPlayerIfUrlPresent(final Iterator<String> urls) {
        if (urls.hasNext()) {
            logger.log(INFO, "Next video exist");
            initializeMediaPlayer(urls.next());
            resetTimeSlider();
            configureTotalDurationListener();
            configureCurrentTimeListener();
            configurePlayPauseRestartAction();

            playByDefault();
        } else {
            showRestartButton();
        }
    }

    private void initializeAllControlsSvgOnTheBeginning() {
        logger.log(INFO, "Initialize all controls svg on the beginning");
        setButtonPauseSVG();
        setButtonNextSVG();
        setLabelVolume2SVG();
        setLabelExitFullscreenSVG();

        sliderVolume.setValue(DEFAULT_VOLUME_VALUE);
        labelSpeed.setText(SPEED_LEVEL_1);
        hBoxVolume.getChildren().remove(sliderVolume);
    }

    private void setUpFadeOutFeature() {
        logger.log(INFO, "Setup fade out feature");
        PauseTransition delayFadeOut = new PauseTransition(Duration.seconds(3));
        delayFadeOut.setOnFinished(e -> fadeOutPane(vBoxFullPanel));

        mediaView.setOnMouseMoved(evt -> delayFadeOut.playFromStart());
        vBoxFullPanel.setOnMouseExited(evt -> delayFadeOut.playFromStart());
        vBoxFullPanel.setOnMouseEntered(evt -> {
            logger.log(INFO, "Stop fade out feature when mouse entered controls panel");
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
        logger.log(INFO, "Setup button handlers");
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
        mediaPlayer.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
                sliderTime.setMax(newDuration.toSeconds());
                labelTotalTime.setText(getTime(newDuration));

            }
        });

        sliderTime.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean isChanging) {
                bindCurrentTimeLabel();
                if (!isChanging) {
                    mediaPlayer.seek(Duration.seconds(sliderTime.getValue()));
                }
            }
        });

        sliderTime.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                bindCurrentTimeLabel();
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                if (Math.abs(currentTime - newValue.doubleValue()) > 0.5) {
                    mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                }
                addColorToSliderTime(sliderTime, newValue);
                labelsMatchEndVideo();
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTime, Duration newTime) {
                bindCurrentTimeLabel();
                if (!sliderTime.isValueChanging()) {
                    sliderTime.setValue(newTime.toSeconds());
                }
                labelsMatchEndVideo();
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                initializeMediaPlayer(pathToVideoIterator, videoIterator);
            }
        });
    }

    private void handlePlayPauseRestart() {
        bindCurrentTimeLabel();
        logger.log(INFO, "Is end of video: " + atEndOfVideo);
        if (atEndOfVideo) {
            logger.log(INFO, "Slider time value was set to 0");
            sliderTime.setValue(0);
            atEndOfVideo = false;
            isPlaying = false;
        }

        if (isPlaying) {
            logger.log(INFO, "Video is pause. Button PLAY was set");
            setButtonPlaySVG();
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            logger.log(INFO, "Video is playing. Button PAUSE was set");
            setButtonPauseSVG();
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    private void handleFullscreenClick() {
        Stage stage = (Stage) stackPaneParent.getScene().getWindow();

        if (stage.isFullScreen()) {
            logger.log(INFO, "Fullscreen was exited");
            stage.setFullScreen(false);
            setLabelExitFullscreenSVG();
        } else {
            logger.log(INFO, "Fullscreen was entered");
            stage.setFullScreen(true);
            setLabelEnterFullscreenSVG();
            stage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    logger.log(INFO, "Button ESCAPE was clicked");
                    setLabelEnterFullscreenSVG();
                }
            });
        }
    }

    private void handleSpeedClick() {
        if (mediaPlayer == null) {
            logger.log(ERROR, "Media player was NULL during handling speed click");
            return;
        }

        if (SPEED_LEVEL_1.equals(labelSpeed.getText())) {
            logger.log(INFO, "Speed video was set to " + SPEED_LEVEL_1);
            mediaPlayer.setRate(SPEED_LEVEL_2_VALUE);
            labelSpeed.setText(SPEED_LEVEL_2);
        } else if (SPEED_LEVEL_2.equals(labelSpeed.getText())) {
            logger.log(INFO, "Speed video was set to " + SPEED_LEVEL_2);
            mediaPlayer.setRate(SPEED_LEVEL_3_VALUE);
            labelSpeed.setText(SPEED_LEVEL_3);
        } else if (SPEED_LEVEL_3.equals(labelSpeed.getText())) {
            logger.log(INFO, "Speed video was set to " + SPEED_LEVEL_3);
            mediaPlayer.setRate(SPEED_LEVEL_4_VALUE);
            labelSpeed.setText(SPEED_LEVEL_4);
        } else {
            logger.log(INFO, "Speed video was restart to " + SPEED_LEVEL_1);
            mediaPlayer.setRate(SPEED_LEVEL_1_VALUE);
            labelSpeed.setText(SPEED_LEVEL_1);
        }
    }

    private void handleVolumeClick() {
        if (mediaPlayer == null || sliderVolume == null) {
            logger.log(ERROR, "Media player or slider volume was NULL during handling volume click");
            return;
        }

        if (isMuted) {
            logger.log(INFO, "Unmuted video volume");
            setLabelVolume1SVG();
            mediaPlayer.setVolume(DEFAULT_VOLUME_VALUE);
            sliderVolume.setValue(DEFAULT_VOLUME_VALUE);
            isMuted = false;
        } else {
            logger.log(INFO, "Muted video volume");
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
            logger.log(INFO, "Volume was set to " + sliderVolume.getValue());
        } else  {
            logger.log(ERROR, "Media player was NULL during handling volume mouse entered");
        }
    }

    private void handleVolumeMouseExit() {
        hBoxVolume.getChildren().remove(sliderVolume);
        logger.log(INFO, "Volume slider was removed after mouse exited");
    }

    private void resetTimeSlider() {
        sliderTime.setValue(RESET_TIME_VALUE);
        logger.log(INFO, "Time slider was reset to " + RESET_TIME_VALUE);
    }

    private void configureTotalDurationListener() {
        mediaPlayer.totalDurationProperty().addListener((observableValue, oldDuration, newDuration) -> {
            sliderTime.setMax(newDuration.toSeconds());
            labelTotalTime.setText(getTime(newDuration));
            logger.log(INFO, "Total duartion was set to: " + labelTotalTime.getText());
        });
    }

    private void configureCurrentTimeListener() {
        mediaPlayer.currentTimeProperty().addListener((observableValue, oldTime, newTime) -> {
            bindCurrentTimeLabel();
            if (!sliderTime.isValueChanging()) {
                sliderTime.setValue(newTime.toSeconds());
            }
            labelsMatchEndVideo();
        });
    }

    private void configurePlayPauseRestartAction() {
        buttonPlayPauseRestart.setOnAction(actionEvent -> {
            bindCurrentTimeLabel();
            if (atEndOfVideo) {
                logger.log(INFO, "configurePlayPauseRestartAction: Video is restarted");
                resetVideoIfEnded();
            }
            if (isPlaying) {
                logger.log(INFO, "configurePlayPauseRestartAction: Video is paused");
                pause();
            } else {
                logger.log(INFO, "configurePlayPauseRestartAction: Video is played");
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
        initializeMediaPlayer(pathToVideoIterator, videoIterator);
    }

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

    public void labelsMatchEndVideo() {
        Duration totalDuration = mediaPlayer.getTotalDuration();
        Duration currentTime = mediaPlayer.getCurrentTime();

        if (totalDuration.lessThanOrEqualTo(currentTime)) {
            logger.log(INFO, "labelsMatchEndVideo: set button RESTART");
            atEndOfVideo = true;
            setButtonRestartSVG();
        } else {
            atEndOfVideo = false;

            if (isPlaying) {
                setButtonPauseSVG();
            } else {
                setButtonPlaySVG();
            }
        }
    }

    public void bindCurrentTimeLabel() {
        logger.log(INFO, "bindCurrentTimeLabel");
        labelCurrentTime.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getTime(mediaPlayer.getCurrentTime()) + " / ";
            }
        }, mediaPlayer.currentTimeProperty()));
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