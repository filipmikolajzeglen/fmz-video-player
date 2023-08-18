package com.filipmikolajzeglen.fmzvideoplayer;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

import static com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerConfiguration.APPLICATION_FXML;
import static com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerConfiguration.APPLICATION_ERROR_LOAD;
import static com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerConfiguration.APPLICATION_TITLE;
import static com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerConfiguration.APPLICATION_ICON;
import static com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerConfiguration.APPLICATION_ERROR;
import static java.util.Objects.requireNonNull;

public class FMZVideoPlayerApplication extends Application {

    private static final Logger logger = new Logger();

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(requireNonNull(getClass().getResource(APPLICATION_FXML)));
            if (root == null) {
                throw new IOException(APPLICATION_ERROR_LOAD);
            }

            Scene scene = new Scene(root, 1920, 1200);
            primaryStage.setTitle(APPLICATION_TITLE);
            primaryStage.getIcons().add(new Image(APPLICATION_ICON));
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true);
            primaryStage.show();
        } catch (IOException e) {
            logger.error(APPLICATION_ERROR + e.getCause());
            logger.error(APPLICATION_ERROR + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
