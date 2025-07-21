package com.filipmikolajzeglen.fmzvideoplayer;

import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_ERROR;
import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_FXML;
import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_ICON;
import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_STARTUP_FXML;
import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_TITLE;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Arrays;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FMZVideoPlayerApplication extends Application
{
   private static final Logger LOGGER = new Logger();

   @Override
   public void start(Stage primaryStage)
   {
      try
      {
         Parent root = FXMLLoader.load(requireNonNull(FMZVideoPlayerApplication.class.getResource(APPLICATION_STARTUP_FXML)));
         Scene scene = new Scene(root);
         primaryStage.setTitle("FMZ Video Player Configuration");
         primaryStage.getIcons().add(new Image(APPLICATION_ICON));
         primaryStage.setScene(scene);
         primaryStage.setResizable(false);
         primaryStage.show();
      }
      catch (IOException e)
      {
         LOGGER.error(APPLICATION_ERROR + e.getCause());
         LOGGER.error(APPLICATION_ERROR + Arrays.toString(e.getStackTrace()));
      }
   }

   public static void launchMainPlayer()
   {
      Platform.runLater(() -> {
         try
         {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(requireNonNull(FMZVideoPlayerApplication.class.getResource(APPLICATION_FXML)));
            initializeStage(stage, root);
            addFocusListener(stage);
         }
         catch (IOException e)
         {
            new Logger().error(APPLICATION_ERROR + e.getCause());
         }
      });
   }

   private static void initializeStage(Stage stage, Parent root)
   {
      Scene scene = new Scene(root, 1920, 1200);
      stage.setTitle(APPLICATION_TITLE);
      stage.getIcons().add(new Image(APPLICATION_ICON));
      stage.setScene(scene);
      stage.setAlwaysOnTop(true);
      stage.setFullScreen(true);
      stage.show();
   }

   private static void addFocusListener(Stage stage)
   {
      stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
         if (!isFocused && stage.isFullScreen())
         {
            Platform.runLater(() -> {
               stage.setAlwaysOnTop(false);
               stage.setAlwaysOnTop(true);
            });
         }
      });
   }

   public static void main(String[] args)
   {
      launch(args);
   }
}