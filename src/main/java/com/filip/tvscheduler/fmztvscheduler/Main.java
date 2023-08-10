package com.filip.tvscheduler.fmztvscheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.APPLICATION_FXML;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.APPLICATION_ERROR_LOAD;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.APPLICATION_TITLE;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.APPLICATION_ICON;
import static com.filip.tvscheduler.fmztvscheduler.video.VideoPlayerConfigurator.APPLICATION_ERROR;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(APPLICATION_FXML));
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
            System.err.println(APPLICATION_ERROR + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
