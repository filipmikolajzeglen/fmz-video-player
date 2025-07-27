package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerLibrarySeries;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoMetadataReader;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import lombok.Getter;

@Getter
public class PlayerQuickStartView
{
   //@formatter:off
   @FXML private VBox quickStartContent;
   @FXML private Label maxSeriesLabel;
   @FXML private Spinner<Integer> maxSeriesSpinner;
   @FXML private Label maxEpisodesLabel;
   @FXML private Spinner<Integer> maxEpisodesSpinner;
   @FXML private TextField videoMainSourceField;
   @FXML private Label videoMainSourcePrompt;
   @FXML private TableView<PlayerLibrarySeries> seriesTable;
   @FXML private TableColumn<PlayerLibrarySeries, String> seriesNameColumn;
   @FXML private TableColumn<PlayerLibrarySeries, Integer> episodeCountColumn;
   @FXML private TableColumn<PlayerLibrarySeries, String> totalWatchingTimeColumn;
   //@formatter:on

   @FXML
   public void initialize()
   {
      seriesNameColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.50));
      episodeCountColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.20));
      totalWatchingTimeColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.26));

      seriesNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
      episodeCountColumn.setCellValueFactory(data -> data.getValue().episodeCountProperty().asObject());
      totalWatchingTimeColumn.setCellValueFactory(data -> data.getValue().totalWatchingTimeProperty());

      videoMainSourceField.textProperty().addListener((obs, oldVal, newVal) -> updateSeriesData(newVal));
      videoMainSourcePrompt.visibleProperty().bind(Bindings.isEmpty(videoMainSourceField.textProperty()));

      quickStartContent.getProperties().put("controller", this);
   }

   private void updateSeriesData(String path)
   {
      List<PlayerLibrarySeries> series = getSeriesFolders(path);
      ObservableList<PlayerLibrarySeries> tableItems = FXCollections.observableArrayList(series);
      seriesTable.setItems(tableItems);

      startDurationCalculation(path, tableItems);
   }

   public List<PlayerLibrarySeries> getSeriesFolders(String mainPath)
   {
      File mainDir = new File(mainPath);
      if (mainDir.exists() && mainDir.isDirectory())
      {
         File[] dirs = mainDir.listFiles(File::isDirectory);
         if (dirs != null)
         {
            return Arrays.stream(dirs)
                  .filter(dir -> !dir.getName()
                        .equalsIgnoreCase(PlayerConstants.Paths.COMMERCIALS_FOLDER_NAME))
                  .map(dir -> new PlayerLibrarySeries(
                        dir.getName(),
                        countVideoFiles(dir)
                  ))
                  .sorted(Comparator.comparing(PlayerLibrarySeries::getName))
                  .collect(Collectors.toList());
         }
      }
      return List.of();
   }

   private int countVideoFiles(File dir)
   {
      File[] files = dir.listFiles(file -> {
         String name = file.getName().toLowerCase();
         return file.isFile() && (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv"));
      });
      return files == null ? 0 : files.length;
   }

   private void startDurationCalculation(String basePath, ObservableList<PlayerLibrarySeries> seriesList)
   {
      Task<Void> task = new Task<>()
      {
         @Override
         protected Void call()
         {
            for (PlayerLibrarySeries seriesInfo : seriesList)
            {
               if (isCancelled())
               {
                  break;
               }

               File seriesDir = new File(basePath, seriesInfo.getName());
               File[] videoFiles = listVideoFiles(seriesDir);
               long totalSeconds = Arrays.stream(videoFiles)
                     .mapToLong(VideoMetadataReader::getDurationInSeconds)
                     .sum();

               String formattedDuration = formatDuration(totalSeconds);
               Platform.runLater(() -> seriesInfo.setTotalWatchingTime(formattedDuration));
            }
            return null;
         }
      };
      new Thread(task).start();
   }

   private File[] listVideoFiles(File dir)
   {
      if (dir == null || !dir.isDirectory())
      {
         return new File[0];
      }
      return dir.listFiles(file -> {
         String name = file.getName().toLowerCase();
         return file.isFile() && (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv"));
      });
   }

   private String formatDuration(long totalSeconds)
   {
      if (totalSeconds <= 0)
      {
         return "0m";
      }
      long hours = totalSeconds / 3600;
      long minutes = (totalSeconds % 3600) / 60;
      if (hours > 0)
      {
         return String.format("%dh %02dm", hours, minutes);
      }
      else
      {
         return String.format("%dm", minutes);
      }
   }

   public int getMaxSingleSeriesPerDay()
   {
      return maxSeriesSpinner.getValue();
   }

   public int getMaxEpisodesPerDay()
   {
      return maxEpisodesSpinner.getValue();
   }

   public String getVideoMainSourcePath()
   {
      return videoMainSourceField.getText();
   }

   @FXML
   private void onChooseFolderClicked()
   {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("Select Video Source Folder");
      Window window = quickStartContent.getScene() != null ? quickStartContent.getScene().getWindow() : null;
      File selectedDirectory = directoryChooser.showDialog(window);
      if (selectedDirectory != null)
      {
         videoMainSourceField.setText(selectedDirectory.getAbsolutePath());
      }
   }
}
