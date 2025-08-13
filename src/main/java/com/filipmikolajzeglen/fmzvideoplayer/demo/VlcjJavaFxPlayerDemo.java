package com.filipmikolajzeglen.fmzvideoplayer.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VlcjJavaFxPlayerDemo extends Application {

   private MediaPlayerFactory mediaPlayerFactory;
   private EmbeddedMediaPlayer embeddedMediaPlayer;

   private static final String VIDEO_PATH = "F:\\FoxKids\\Batman Przyszłości\\S01E04-Golem.mkv";

   @Override
   public void start(Stage primaryStage) {
      ImageView imageView = new ImageView();
      imageView.setPreserveRatio(true);

      BorderPane root = new BorderPane();
      root.setCenter(imageView);

      imageView.fitWidthProperty().bind(root.widthProperty());
      imageView.fitHeightProperty().bind(root.heightProperty());

      mediaPlayerFactory = new MediaPlayerFactory();
      embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
      embeddedMediaPlayer.videoSurface().set(new ImageViewVideoSurface(imageView));

      Scene scene = new Scene(root, 1280, 720);
      primaryStage.setTitle("Prosty odtwarzacz VLCJ");
      primaryStage.setScene(scene);
      primaryStage.show();

      embeddedMediaPlayer.media().play(VIDEO_PATH);
   }

   @Override
   public void stop() {
      if (embeddedMediaPlayer != null) {
         embeddedMediaPlayer.controls().stop();
         embeddedMediaPlayer.release();
      }
      if (mediaPlayerFactory != null) {
         mediaPlayerFactory.release();
      }
   }

   public static void main(String[] args) {
      launch(args);
   }
}