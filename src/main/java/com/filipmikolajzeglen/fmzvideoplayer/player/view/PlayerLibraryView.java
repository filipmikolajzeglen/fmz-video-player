package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import static com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants.UI.LIBRARY_SERIES_DETAIL_FXML;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerLibrarySeries;
import com.filipmikolajzeglen.fmzvideoplayer.video.Video;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class PlayerLibraryView
{
   //@formatter:off
   @FXML private TilePane libraryTilePane;
   @FXML private ScrollPane libraryScrollPane;
   @FXML private AnchorPane libraryContent;
   //@formatter:on

   private PlayerMainView playerMainView;

   @FXML
   public void initialize()
   {
      libraryContent.getProperties().put("controller", this);
   }

   public void setStartupConfigController(PlayerMainView controller)
   {
      this.playerMainView = controller;
   }

   public void refreshLibrary(String basePath, List<PlayerLibrarySeries> series)
   {
      libraryTilePane.getChildren().clear();
      for (PlayerLibrarySeries playerLibrarySeries : series)
      {
         VBox seriesTile = createSeriesTile(basePath, playerLibrarySeries);
         libraryTilePane.getChildren().add(seriesTile);
      }
   }

   private VBox createSeriesTile(String basePath, PlayerLibrarySeries playerLibrarySeries)
   {
      VBox tileContainer = new VBox(5);
      tileContainer.setAlignment(Pos.TOP_CENTER);
      tileContainer.setOnMouseClicked(event -> {
         if (event.getButton() == MouseButton.PRIMARY)
         {
            showSeriesDetailView(basePath, playerLibrarySeries);
         }
      });

      Node coverView;
      File coverFile = findCoverFile(basePath, playerLibrarySeries.getName());

      if (coverFile != null)
      {
         try
         {
            Image coverImage = new Image(coverFile.toURI().toString(), 150, 225, false, true);
            ImageView imageView = new ImageView(coverImage);
            imageView.setFitWidth(150);
            imageView.setFitHeight(225);

            Rectangle clip = new Rectangle(150, 225);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            imageView.setClip(clip);

            coverView = imageView;
         }
         catch (Exception e)
         {
            coverView = createCoverPlaceholder(playerLibrarySeries.getName());
         }
      }
      else
      {
         coverView = createCoverPlaceholder(playerLibrarySeries.getName());
      }

      // Nowy efekt rozjaśnienia okładki
      ColorAdjust brightenEffect = new ColorAdjust();
      brightenEffect.setBrightness(0.2); // Możesz dostosować tę wartość

      Node finalCoverView = coverView;
      tileContainer.setOnMouseEntered(e -> {
         finalCoverView.setEffect(brightenEffect);
         tileContainer.setStyle("-fx-cursor: hand;");
      });

      Node finalCoverView1 = coverView;
      tileContainer.setOnMouseExited(e -> {
         finalCoverView1.setEffect(null);
         tileContainer.setStyle("-fx-cursor: default;");
      });

      Label titleLabel = new Label(playerLibrarySeries.getName());
      titleLabel.setWrapText(true);
      titleLabel.setTextAlignment(TextAlignment.CENTER);

      StackPane titleWrapper = new StackPane(titleLabel);
      titleWrapper.setPrefWidth(150);

      tileContainer.getChildren().addAll(coverView, titleWrapper);
      return tileContainer;
   }


   private void showSeriesDetailView(String basePath, PlayerLibrarySeries playerLibrarySeries) {
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource(LIBRARY_SERIES_DETAIL_FXML));
         Node detailView = loader.load();

         PlayerLibrarySeriesDetailView controller = loader.getController();
         controller.initData(basePath, playerLibrarySeries, playerMainView, this);

         libraryScrollPane.setFitToHeight(true);
         libraryScrollPane.setContent(detailView);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void showLibraryGrid() {
      libraryScrollPane.setFitToHeight(false);
      libraryScrollPane.setContent(libraryTilePane);
   }

   public Node createCoverPlaceholder(String seriesName)
   {
      return createCoverPlaceholder(seriesName, 150, 225);
   }

   public Node createCoverPlaceholder(String seriesName, double width, double height)
   {
      StackPane placeholder = new StackPane();
      placeholder.setPrefSize(width, height);
      placeholder.setMinSize(width, height);
      placeholder.setMaxSize(width, height);

      Rectangle background = new Rectangle(width, height);
      background.setArcWidth(10);
      background.setArcHeight(10);
      background.setFill(generateRandomColor());

      Text initials = new Text(getInitials(seriesName));
      initials.setFont(Font.font("Arial", FontWeight.BOLD, Math.min(width, height) / 4));
      initials.setFill(Color.WHITE);

      placeholder.getChildren().addAll(background, initials);
      StackPane.setAlignment(initials, Pos.CENTER);

      return placeholder;
   }

   public File findCoverFile(String basePath, String seriesName)
   {
      File seriesDir = new File(basePath, seriesName);
      File coverDir = new File(seriesDir, "Cover");

      if (coverDir.exists() && coverDir.isDirectory())
      {
         File[] coverFiles = coverDir.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return file.isFile() && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
         });

         if (coverFiles != null && coverFiles.length > 0)
         {
            return Arrays.stream(coverFiles).findFirst().orElse(null);
         }
      }
      return null;
   }

   private String getInitials(String name)
   {
      if (name == null || name.trim().isEmpty())
      {
         return "?";
      }

      String cleanedName = name.replaceAll("[^a-zA-Z0-9\\s]", "");
      String[] words = cleanedName.trim().split("\\s+");

      List<String> validWords = Arrays.stream(words)
            .filter(w -> !w.isEmpty())
            .collect(Collectors.toList());

      if (validWords.isEmpty())
      {
         return "?";
      }

      if (validWords.size() > 1)
      {
         String firstInitial = validWords.get(0).substring(0, 1);
         String secondInitial = validWords.get(1).substring(0, 1);
         return (firstInitial + secondInitial).toUpperCase();
      }
      else
      {
         String word = validWords.get(0);
         if (word.length() > 1)
         {
            return word.substring(0, 2).toUpperCase();
         }
         else
         {
            return word.substring(0, 1).toUpperCase();
         }
      }
   }

   private Color generateRandomColor()
   {
      Random random = new Random();
      double hue = random.nextDouble() * 360;
      double saturation = 0.5 + random.nextDouble() * 0.2;
      double brightness = 0.5 + random.nextDouble() * 0.2;
      return Color.hsb(hue, saturation, brightness);
   }

   public static class EpisodeInfo
   {
      private final SimpleStringProperty season;
      private final SimpleStringProperty episode;
      private final SimpleStringProperty name;
      private final SimpleStringProperty duration;
      private final SimpleStringProperty watched;

      public EpisodeInfo(Video video)
      {
         this.season = new SimpleStringProperty(video.getSeasonNumber());
         this.episode = new SimpleStringProperty(video.getEpisodeNumber());
         this.name = new SimpleStringProperty(video.getEpisodeName());
         this.duration = new SimpleStringProperty(formatDuration(video.getDurationInSeconds()));
         this.watched = new SimpleStringProperty(video.isWatched() ? "YES" : "NO");
      }

      private String formatDuration(long totalSeconds) {
         long minutes = totalSeconds / 60;
         long seconds = totalSeconds % 60;
         return String.format("%d:%02d", minutes, seconds);
      }

      public String getSeason() {
         return season.get();
      }

      public SimpleStringProperty seasonProperty() {
         return season;
      }

      public String getEpisode() {
         return episode.get();
      }

      public SimpleStringProperty episodeProperty() {
         return episode;
      }

      public String getName()
      {
         return name.get();
      }

      public SimpleStringProperty nameProperty()
      {
         return name;
      }

      public String getDuration() {
         return duration.get();
      }

      public SimpleStringProperty durationProperty() {
         return duration;
      }

      public String getWatched() {
         return watched.get();
      }

      public SimpleStringProperty watchedProperty() {
         return watched;
      }
   }
}