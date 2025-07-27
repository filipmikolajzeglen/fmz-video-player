package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerLibrarySeries;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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

      Label titleLabel = new Label(playerLibrarySeries.getName());
      titleLabel.setWrapText(true);
      titleLabel.setTextAlignment(TextAlignment.CENTER);

      StackPane titleWrapper = new StackPane(titleLabel);
      titleWrapper.setPrefWidth(150);

      tileContainer.getChildren().addAll(coverView, titleWrapper);
      return tileContainer;
   }

   private void showSeriesDetailView(String basePath, PlayerLibrarySeries playerLibrarySeries)
   {
      BorderPane detailView = new BorderPane();
      detailView.setPadding(new Insets(10));

      TableView<EpisodeInfo> episodeTable = new TableView<>();
      TableColumn<EpisodeInfo, String> nameColumn = new TableColumn<>("Tytuł odcinka");
      nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
      episodeTable.getColumns().add(nameColumn);
      nameColumn.prefWidthProperty().bind(episodeTable.widthProperty().multiply(0.98));

      List<EpisodeInfo> episodes = getEpisodesForSeries(basePath, playerLibrarySeries.getName());
      episodeTable.setItems(FXCollections.observableArrayList(episodes));
      detailView.setCenter(episodeTable);

      VBox rightPane = new VBox(10);
      rightPane.setAlignment(Pos.TOP_CENTER);
      rightPane.setPadding(new Insets(0, 0, 0, 10));

      Node coverView;
      File coverFile = findCoverFile(basePath, playerLibrarySeries.getName());
      if (coverFile != null)
      {
         try
         {
            Image coverImage = new Image(coverFile.toURI().toString(), 150, 225, false, true);
            coverView = new ImageView(coverImage);
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

      Button playAllButton = new Button("Odtwórz wszystko");
      playAllButton.setOnAction(event -> {
         PlayerConstants.Playback.PLAYLIST_TO_START = playerLibrarySeries.getName();
         if (playerMainView != null)
         {
            playerMainView.onPlayClicked();
         }
      });

      Button backButton = new Button("Wróć do biblioteki");
      backButton.setOnAction(event -> libraryScrollPane.setContent(libraryTilePane));

      rightPane.getChildren().addAll(coverView, playAllButton, backButton);
      detailView.setRight(rightPane);

      libraryScrollPane.setContent(detailView);
   }

   private Node createCoverPlaceholder(String seriesName)
   {
      StackPane placeholder = new StackPane();
      placeholder.setPrefSize(150, 225);
      placeholder.setMinSize(150, 225);
      placeholder.setMaxSize(150, 225);

      Rectangle background = new Rectangle(150, 225);
      background.setArcWidth(10);
      background.setArcHeight(10);
      background.setFill(generateRandomColor());

      Text initials = new Text(getInitials(seriesName));
      initials.setFont(Font.font("Arial", FontWeight.BOLD, 40));
      initials.setFill(Color.WHITE);

      placeholder.getChildren().addAll(background, initials);
      StackPane.setAlignment(initials, Pos.CENTER);

      return placeholder;
   }

   private File findCoverFile(String basePath, String seriesName)
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
      private final SimpleStringProperty name;

      public EpisodeInfo(String name)
      {
         String displayName = name.lastIndexOf('.') > 0 ? name.substring(0, name.lastIndexOf('.')) : name;
         this.name = new SimpleStringProperty(displayName);
      }

      public String getName()
      {
         return name.get();
      }

      public SimpleStringProperty nameProperty()
      {
         return name;
      }
   }

   private List<EpisodeInfo> getEpisodesForSeries(String basePath, String seriesName)
   {
      File seriesDir = new File(basePath, seriesName);
      if (!seriesDir.isDirectory())
      {
         return Collections.emptyList();
      }
      File[] videoFiles = seriesDir.listFiles(file -> {
         String name = file.getName().toLowerCase();
         return file.isFile() && (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv"));
      });

      if (videoFiles == null)
      {
         return Collections.emptyList();
      }

      return Arrays.stream(videoFiles)
            .map(file -> new EpisodeInfo(file.getName()))
            .sorted(Comparator.comparing(EpisodeInfo::getName))
            .collect(Collectors.toList());
   }
}
