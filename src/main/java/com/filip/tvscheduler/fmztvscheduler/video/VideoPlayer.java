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
import javafx.scene.Scene;
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
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.GRADIENT_COLOR;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.PRIMARY_COLOR;

public class VideoPlayer implements Initializable {

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

    private void stopAndDisposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void initializeMediaPlayer(String videoPath) {
        media = new Media(new File(videoPath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
    }

    private void initializeVideoInfo(Video video) {
        String fullEpisodeInfo = String.format("%s - %s", video.getVideoName().toUpperCase(), video.getEpisodeName());
        labelCurrentEpisode.setText(fullEpisodeInfo);
    }

    private void initializeAllControlsSvgOnTheBeginning() {
        setButtonPauseSvg();
        setButtonNextSvg();
        setLabelVolumeSvg();
        setLabelExitFullscreenSvg();

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
        labelFullScreen.setOnMouseClicked(e -> handleFullscreenClick());
        labelSpeed.setOnMouseClicked(e -> handleSpeedClick());

        labelVolume.setOnMouseClicked(e -> handleVolumeClick());
        labelVolume.setOnMouseEntered(e -> handleVolumeMouseEnter());
        hBoxVolume.setOnMouseExited(e -> handleVolumeMouseExit());
        hBoxVolume.setOnMouseEntered(e -> handleVolumeMouseEnter());
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
        stackPaneParent.sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> observableValue, Scene scene, Scene newScene) {
                if (scene == null && newScene != null) {
                    mediaView.fitHeightProperty().bind(newScene.heightProperty().subtract(hBoxControls.heightProperty().add(0)));
                }
            }
        });

        stackPaneParent.sceneProperty().addListener((observableValue, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                mediaView.fitHeightProperty().bind(newScene.heightProperty().subtract(hBoxControls.heightProperty()));
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
                labelsMatchEndVideo(labelCurrentTime.getText(), labelTotalTime.getText());
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTime, Duration newTime) {
                bindCurrentTimeLabel();
                if (!sliderTime.isValueChanging()) {
                    sliderTime.setValue(newTime.toSeconds());
                }
                labelsMatchEndVideo(labelCurrentTime.getText(), labelTotalTime.getText());
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

        if (atEndOfVideo) {
            sliderTime.setValue(0);
            atEndOfVideo = false;
            isPlaying = false;
        }

        if (isPlaying) {
            setButtonPlaySvg();
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            setButtonPauseSvg();
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    private void handleFullscreenClick() {
        Stage stage = (Stage) stackPaneParent.getScene().getWindow();

        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
            setLabelExitFullscreenSvg();
        } else {
            stage.setFullScreen(true);
            setLabelEnterFullscreenSvg();
            stage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    setLabelEnterFullscreenSvg();
                }
            });
        }
    }

    private void handleSpeedClick() {
        if (mediaPlayer == null) {
            return;
        }

        if (SPEED_LEVEL_1.equals(labelSpeed.getText())) {
            mediaPlayer.setRate(SPEED_LEVEL_2_VALUE);
            labelSpeed.setText(SPEED_LEVEL_2);
        } else if (SPEED_LEVEL_2.equals(labelSpeed.getText())) {
            mediaPlayer.setRate(SPEED_LEVEL_3_VALUE);
            labelSpeed.setText(SPEED_LEVEL_3);
        } else if (SPEED_LEVEL_3.equals(labelSpeed.getText())) {
            mediaPlayer.setRate(SPEED_LEVEL_4_VALUE);
            labelSpeed.setText(SPEED_LEVEL_4);
        } else {
            mediaPlayer.setRate(SPEED_LEVEL_1_VALUE);
            labelSpeed.setText(SPEED_LEVEL_1);
        }
    }

    private void handleVolumeClick() {
        if (mediaPlayer == null || sliderVolume == null) {
            return;
        }

        if (isMuted) {
            setLabelVolume1Svg();
            mediaPlayer.setVolume(DEFAULT_VOLUME_VALUE);
            sliderVolume.setValue(DEFAULT_VOLUME_VALUE);
            isMuted = false;
        } else {
            setLabelVolumeMuteSvg();
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
        }
    }

    private void handleVolumeMouseExit() {
        hBoxVolume.getChildren().remove(sliderVolume);
    }

    private void initializeMediaPlayer(final Iterator<String> urls, final Iterator<Video> videos) {
        stopAndDisposeMediaPlayer();
        initializeVideoIfPresent(videos);
        initializeMediaPlayerIfUrlPresent(urls);
        atEndOfVideo = true;
        refreshLabelsIfNeeded();
    }

    private void initializeVideoIfPresent(final Iterator<Video> videos) {
        if (videos.hasNext()) {
            initializeVideoInfo(videos.next());
        }
    }

    private void initializeMediaPlayerIfUrlPresent(final Iterator<String> urls) {
        if (urls.hasNext()) {
            initializeMediaPlayer(urls.next());
            resetTimeSlider();
            configureTotalDurationListener();
            configureCurrentTimeListener();
            configurePlayPauseRestartAction();

            playVideoByDefault();
        } else {
            showRestartButton();
        }
    }

    private void resetTimeSlider() {
        sliderTime.setValue(RESET_TIME_VALUE);
    }

    private void configureTotalDurationListener() {
        mediaPlayer.totalDurationProperty().addListener((observableValue, oldDuration, newDuration) -> {
            sliderTime.setMax(newDuration.toSeconds());
            labelTotalTime.setText(getTime(newDuration));
        });
    }

    private void configureCurrentTimeListener() {
        mediaPlayer.currentTimeProperty().addListener((observableValue, oldTime, newTime) -> {
            bindCurrentTimeLabel();
            if (!sliderTime.isValueChanging()) {
                sliderTime.setValue(newTime.toSeconds());
            }
            labelsMatchEndVideo(labelCurrentTime.getText(), labelTotalTime.getText());
        });
    }

    private void configurePlayPauseRestartAction() {
        buttonPlayPauseRestart.setOnAction(actionEvent -> {
            bindCurrentTimeLabel();
            if (atEndOfVideo) {
                resetVideoIfEnded();
            }
            if (isPlaying) {
                pauseVideo();
            } else {
                playVideo();
            }
        });
    }

    private void resetVideoIfEnded() {
        sliderTime.setValue(RESET_TIME_VALUE);
        atEndOfVideo = false;
        isPlaying = false;
    }

    private void pauseVideo() {
        setButtonPlaySvg();
        mediaPlayer.pause();
        isPlaying = false;
    }

    private void playVideo() {
        setButtonPauseSvg();
        mediaPlayer.play();
        isPlaying = true;
    }

    private void playVideoByDefault() {
        mediaPlayer.setAutoPlay(true);
        setButtonPauseSvg();
    }

    private void showRestartButton() {
        setButtonRestartSvg();
    }

    private void refreshLabelsIfNeeded() {
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

    public void labelsMatchEndVideo(String labelTime, String labelTotalTime) {
        for (int i = 0; i < labelTotalTime.length(); i++) {
            if (labelTime.charAt(i) != labelTotalTime.charAt(i)) {
                atEndOfVideo = false;

                if (isPlaying) {
                    setButtonPauseSvg();
                } else {
                    setButtonPlaySvg();
                }
                break;
            } else {
                atEndOfVideo = true;
                setButtonRestartSvg();
            }
        }
    }

    public void bindCurrentTimeLabel() {
        labelCurrentTime.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getTime(mediaPlayer.getCurrentTime()) + " / ";
            }
        }, mediaPlayer.currentTimeProperty()));
    }

    private void setButtonPlaySvg() {
        setSvgGraphic(buttonPlayPauseRestartSVG, PLAY_SVG, 0.04);
        buttonPlayPauseRestart.setGraphic(buttonPlayPauseRestartSVG);
    }

    private void setButtonPauseSvg() {
        setSvgGraphic(buttonPlayPauseRestartSVG, PAUSE_SVG, 0.04);
        buttonPlayPauseRestart.setGraphic(buttonPlayPauseRestartSVG);
    }

    private void setButtonRestartSvg() {
        setSvgGraphic(buttonPlayPauseRestartSVG, RESTART_SVG, 0.05);
        buttonPlayPauseRestart.setGraphic(buttonPlayPauseRestartSVG);
    }

    private void setButtonNextSvg() {
        setSvgGraphic(buttonNextSVG, NEXT_SVG, 0.041);
    }

    private void setLabelVolumeSvg() {
        setSvgGraphic(labelVolumeSVG, VOLUME2_SVG, 0.031);
        labelVolume.setGraphic(labelVolumeSVG);
    }

    private void setLabelExitFullscreenSvg() {
        setSvgGraphic(labelFullScreenSVG, EXIT_SVG, 0.03);
        labelFullScreen.setGraphic(labelFullScreenSVG);
    }

    private void setLabelEnterFullscreenSvg() {
        setSvgGraphic(labelFullScreenSVG, FULLSCREEN_SVG, 0.03);
        labelFullScreen.setGraphic(labelFullScreenSVG);
    }

    private void setLabelVolume1Svg() {
        setSvgGraphic(labelVolumeSVG, VOLUME1_SVG, 0.031);
        labelVolume.setGraphic(labelVolumeSVG);
    }

    private void setLabelVolume2Svg() {
        setSvgGraphic(labelVolumeSVG, VOLUME2_SVG, 0.031);
        labelVolume.setGraphic(labelVolumeSVG);
    }

    private void setLabelVolumeMuteSvg() {
        setSvgGraphic(labelVolumeSVG, MUTE_SVG, 0.031);
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
        return String.format(Locale.US,"-fx-background-color: linear-gradient(to right, %s %f%% , %s %f%%);",
                PRIMARY_COLOR, percentage, GRADIENT_COLOR, percentage);
    }
}