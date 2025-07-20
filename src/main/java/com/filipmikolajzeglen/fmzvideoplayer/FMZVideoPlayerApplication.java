package com.filipmikolajzeglen.fmzvideoplayer;

import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_ERROR;
import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_ERROR_LOAD;
import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_FXML;
import static com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration.UI.APPLICATION_ICON;
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
         Parent root = FXMLLoader.load(requireNonNull(getClass().getResource(APPLICATION_FXML)));
         if (root == null)
         {
            throw new IOException(APPLICATION_ERROR_LOAD);
         }
         initializeStage(primaryStage, root);
         addFocusListener(primaryStage);
      }
      catch (IOException e)
      {
         LOGGER.error(APPLICATION_ERROR + e.getCause());
         LOGGER.error(APPLICATION_ERROR + Arrays.toString(e.getStackTrace()));
      }
   }

   private void initializeStage(Stage stage, Parent root)
   {
      Scene scene = new Scene(root, 1920, 1200);
      stage.setTitle(APPLICATION_TITLE);
      stage.getIcons().add(new Image(APPLICATION_ICON));
      stage.setScene(scene);
      stage.setAlwaysOnTop(true);
      stage.setFullScreen(true);
      stage.show();
   }

   private void addFocusListener(Stage stage)
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