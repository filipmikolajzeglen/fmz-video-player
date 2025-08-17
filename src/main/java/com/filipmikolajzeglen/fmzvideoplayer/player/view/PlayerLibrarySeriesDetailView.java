package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerLibrarySeries;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class PlayerLibrarySeriesDetailView
{
   @FXML
   private TableView<PlayerLibraryView.EpisodeInfo> episodeTable;
   @FXML
   private TableColumn<PlayerLibraryView.EpisodeInfo, String> seasonColumn;
   @FXML
   private TableColumn<PlayerLibraryView.EpisodeInfo, String> episodeColumn;
   @FXML
   private TableColumn<PlayerLibraryView.EpisodeInfo, String> nameColumn;
   @FXML
   private TableColumn<PlayerLibraryView.EpisodeInfo, String> durationColumn;
   @FXML
   private TableColumn<PlayerLibraryView.EpisodeInfo, String> watchedColumn;
   @FXML
   private StackPane coverContainer;
   @FXML
   private Button playAllButton;
   @FXML
   private Button backButton;

   private PlayerMainView playerMainView;
   private PlayerLibraryView playerLibraryView;
   private PlayerLibrarySeries playerLibrarySeries;
   private String basePath;

   @FXML
   public void initialize()
   {
      setupTableColumns();
   }

   private void setupTableColumns()
   {
      seasonColumn.setCellValueFactory(cellData -> cellData.getValue().seasonProperty());
      episodeColumn.setCellValueFactory(cellData -> cellData.getValue().episodeProperty());
      nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
      durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
      watchedColumn.setCellValueFactory(cellData -> cellData.getValue().watchedProperty());
      nameColumn.prefWidthProperty().bind(episodeTable.widthProperty().multiply(0.48));
   }

   public void initData(String basePath, PlayerLibrarySeries playerLibrarySeries, PlayerMainView playerMainView,
         PlayerLibraryView playerLibraryView)
   {
      this.basePath = basePath;
      this.playerLibrarySeries = playerLibrarySeries;
      this.playerMainView = playerMainView;
      this.playerLibraryView = playerLibraryView;
      loadEpisodeData();
      loadCoverImage();
   }

   private void loadEpisodeData()
   {
      List<PlayerLibraryView.EpisodeInfo> episodes = getEpisodesForSeries(playerLibrarySeries.getName());
      episodeTable.setItems(FXCollections.observableArrayList(episodes));
   }

   private void loadCoverImage()
   {
      final double coverWidth = 200;
      final double coverHeight = 300;

      Node coverView;
      File coverFile = playerLibraryView.findCoverFile(basePath, playerLibrarySeries.getName());
      if (coverFile != null)
      {
         try
         {
            Image coverImage = new Image(coverFile.toURI().toString(), coverWidth, coverHeight, false, true);
            ImageView imageView = new ImageView(coverImage);
            imageView.setFitWidth(coverWidth);
            imageView.setFitHeight(coverHeight);

            Rectangle clip = new Rectangle(coverWidth, coverHeight);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            imageView.setClip(clip);
            coverView = imageView;
         }
         catch (Exception e)
         {
            coverView = playerLibraryView.createCoverPlaceholder(playerLibrarySeries.getName(), coverWidth, coverHeight);
         }
      }
      else
      {
         coverView = playerLibraryView.createCoverPlaceholder(playerLibrarySeries.getName(), coverWidth, coverHeight);
      }
      coverContainer.getChildren().setAll(coverView);
   }

   private List<PlayerLibraryView.EpisodeInfo> getEpisodesForSeries(String seriesName)
   {
      return playerMainView.getVideoService().findAllBySeriesName(seriesName).stream()
            .map(PlayerLibraryView.EpisodeInfo::new)
            .sorted(Comparator.comparing(PlayerLibraryView.EpisodeInfo::getSeason)
                  .thenComparing(PlayerLibraryView.EpisodeInfo::getEpisode))
            .collect(Collectors.toList());
   }

   @FXML
   private void handlePlayAll()
   {
      PlayerConstants.Playback.PLAYLIST_TO_START = playerLibrarySeries.getName();
      if (playerMainView != null)
      {
         playerMainView.onPlayClicked();
      }
   }

   @FXML
   private void handleBackToLibrary()
   {
      playerLibraryView.showLibraryGrid();
   }
}