package com.filipmikolajzeglen.fmzvideoplayer;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.filipmikolajzeglen.fmzvideoplayer.database.FMZDatabase;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerIcons;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class StartupConfigController
{
   private FMZDatabase<PlayerConfiguration> configDatabase;
   @FXML
   private TableView<SeriesInfo> seriesTable;
   @FXML
   private TableColumn<SeriesInfo, String> seriesNameColumn;
   @FXML
   private TableColumn<SeriesInfo, Integer> episodeCountColumn;
   @FXML
   private TextField videoMainSourceField;
   @FXML
   private Label maxSeriesLabel;
   @FXML
   private Spinner<Integer> maxSeriesSpinner;
   @FXML
   private Label maxEpisodesLabel;
   @FXML
   private Spinner<Integer> maxEpisodesSpinner;
   @FXML
   private Label videoMainSourcePrompt;
   @FXML
   private Button playButton;
   @FXML
   private ToggleGroup tabsGroup;
   @FXML
   private ToggleButton quickStartTab;
   @FXML
   private ToggleButton advancedTab;
   @FXML
   private ToggleButton aboutTab;
   @FXML
   private VBox quickStartContent;
   @FXML
   private VBox advancedContent;
   @FXML
   private VBox aboutContent;
   @FXML
   private ComboBox<String> iconStyleComboBox;
   @FXML
   private SVGPath previewPlayIcon;
   @FXML
   private SVGPath previewPauseIcon;
   @FXML
   private SVGPath previewNextIcon;
   @FXML
   private SVGPath previewVolumeIcon;
   @FXML
   private ColorPicker primaryColorPicker;
   @FXML
   private Slider colorPreviewSlider;

   @FXML
   public void initialize()
   {
      seriesNameColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.70));
      episodeCountColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.262));

      seriesNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
      episodeCountColumn.setCellValueFactory(
            data -> new SimpleIntegerProperty(data.getValue().getEpisodeCount()).asObject());
      videoMainSourceField.textProperty().addListener((obs, oldVal, newVal) -> updateSeriesTable(newVal));

      tabsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
         if (newToggle == null)
         {
            tabsGroup.selectToggle(oldToggle);
         }
         else if (newToggle.equals(quickStartTab))
         {
            showTab(quickStartContent);
         }
         else if (newToggle.equals(advancedTab))
         {
            showTab(advancedContent);
         }
         else if (newToggle.equals(aboutTab))
         {
            showTab(aboutContent);
         }
      });
      showTab(quickStartContent);
      playButton.disableProperty().bind(Bindings.isEmpty(videoMainSourceField.textProperty()));
      videoMainSourcePrompt.visibleProperty().bind(Bindings.isEmpty(videoMainSourceField.textProperty()));
      loadPlayerConfiguration();
      if (!videoMainSourceField.getText().isEmpty())
      {
         updateSeriesTable(videoMainSourceField.getText());
      }

      iconStyleComboBox.setValue("Filled");
      iconStyleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateIconPreview());
      updateIconPreview();

      primaryColorPicker.valueProperty().addListener((obs, oldColor, newColor) -> {
         if (newColor != null)
         {
            updateSliderPreviewColor(newColor);
         }
      });

      updateSliderPreviewColor(primaryColorPicker.getValue());
   }

   private void updateSliderPreviewColor(Color color)
   {
      String hexColor = toHexString(color);
      // Styl CSS, który koloruje ścieżkę slidera do połowy
      String style = String.format("-fx-background-color: linear-gradient(to right, %s 50%%, #D3D3D3 50%%);", hexColor);

      // Aplikujemy styl do "ścieżki" (track) slidera
      // Używamy lookup, aby znaleźć wewnętrzny element slidera
      if (colorPreviewSlider.lookup(".track") != null)
      {
         colorPreviewSlider.lookup(".track").setStyle(style);
      }
      else
      {
         // Jeśli .track nie jest jeszcze dostępny, spróbuj ponownie za chwilę
         // To zabezpieczenie na wypadek, gdyby skórka nie była gotowa
         colorPreviewSlider.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null && colorPreviewSlider.lookup(".track") != null)
            {
               colorPreviewSlider.lookup(".track").setStyle(style);
            }
         });
      }
   }

   private void updateIconPreview()
   {
      String selectedStyle = iconStyleComboBox.getValue();
      String pathPrefix = "Empty".equals(selectedStyle) ? "/svg/empty" : "/svg/filled";

      previewPlayIcon.setContent(VideoPlayerIcons.loadSvgContent(pathPrefix + "/play.svg"));
      previewPauseIcon.setContent(VideoPlayerIcons.loadSvgContent(pathPrefix + "/pause.svg"));
      previewNextIcon.setContent(VideoPlayerIcons.loadSvgContent(pathPrefix + "/next.svg"));
      previewVolumeIcon.setContent(VideoPlayerIcons.loadSvgContent(pathPrefix + "/volume2.svg"));
   }

   private void updateSeriesTable(String path)
   {
      List<SeriesInfo> series = getSeriesFolders(path);
      ObservableList<SeriesInfo> items = FXCollections.observableArrayList(series);
      seriesTable.setItems(items);
   }

   private List<SeriesInfo> getSeriesFolders(String mainPath)
   {
      File mainDir = new File(mainPath);
      if (mainDir.exists() && mainDir.isDirectory())
      {
         File[] dirs = mainDir.listFiles(File::isDirectory);
         if (dirs != null)
         {
            return Arrays.stream(dirs)
                  .map(dir -> new SeriesInfo(
                        dir.getName(),
                        countVideoFiles(dir)
                  ))
                  .sorted(Comparator.comparing(SeriesInfo::getName))
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

   private void loadPlayerConfiguration()
   {
      File configFile = getConfigFile();
      configDatabase = new FMZDatabase<>(PlayerConfiguration.class);
      configDatabase.setDatabaseName(configFile.getName().replace(".json", ""));
      configDatabase.setDirectoryPath(configFile.getParent());
      configDatabase.setTableName("Configuration");
      configDatabase.initialize();
      if (!configDatabase.findAll().isEmpty())
      {
         PlayerConfiguration config = configDatabase.findAll().get(0);
         maxSeriesSpinner.getValueFactory().setValue(config.getMaxSingleSeriesPerDay());
         maxEpisodesSpinner.getValueFactory().setValue(config.getMaxEpisodesPerDay());
         videoMainSourceField.setText(config.getVideoMainSourcePath());

         if (config.getIconStyle() != null)
         {
            iconStyleComboBox.setValue(config.getIconStyle());
         }
         if (config.getPrimaryColor() != null)
         {
            primaryColorPicker.setValue(Color.web(config.getPrimaryColor()));
         }
      }
   }

   private void savePlayerConfiguration()
   {
      PlayerConfiguration config = new PlayerConfiguration(
            maxSeriesSpinner.getValue(),
            maxEpisodesSpinner.getValue(),
            videoMainSourceField.getText(),
            iconStyleComboBox.getValue(),
            toHexString(primaryColorPicker.getValue())
      );
      configDatabase.saveAll(List.of(config));
   }

   private File getConfigFile()
   {
      String mainSource = videoMainSourceField.getText();
      if (mainSource == null || mainSource.isEmpty())
      {
         mainSource = FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE;
      }
      File dir = new File(mainSource).getParentFile();
      return new File(dir, FMZVideoPlayerConfiguration.Paths.FMZ_DATABASE_NAME + ".json");
   }

   private void showTab(VBox content)
   {
      quickStartContent.setVisible(false);
      quickStartContent.setManaged(false);
      advancedContent.setVisible(false);
      advancedContent.setManaged(false);
      aboutContent.setVisible(false);
      aboutContent.setManaged(false);
      content.setVisible(true);
      content.setManaged(true);
   }

   @FXML
   private void onChooseFolderClicked()
   {
      DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle("Wybierz folder z zasobami wideo");
      Stage stage = (Stage) videoMainSourceField.getScene().getWindow();
      File selectedDir = chooser.showDialog(stage);
      if (selectedDir != null)
      {
         videoMainSourceField.setText(selectedDir.getAbsolutePath());
      }
   }

   @FXML
   private void onPlayClicked()
   {
      String selectedStyle = iconStyleComboBox.getValue();
      if ("Empty".equals(selectedStyle))
      {
         FMZVideoPlayerConfiguration.Icons.PATH_TO_ICONS = "/svg/empty";
      }
      else
      {
         FMZVideoPlayerConfiguration.Icons.PATH_TO_ICONS = "/svg/filled";
      }

      Color selectedColor = primaryColorPicker.getValue();
      FMZVideoPlayerConfiguration.UI.PRIMARY_COLOR = toHexString(selectedColor);

      String mainSource = videoMainSourceField.getText();
      File file = new File(mainSource);
      FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE = mainSource;
      FMZVideoPlayerConfiguration.Paths.FMZ_DIRECTORY_PATH = file.getParent();
      FMZVideoPlayerConfiguration.Paths.FMZ_TABLE_NAME = file.getName();
      FMZVideoPlayerConfiguration.Playback.MAX_SINGLE_SERIES_PER_DAY = maxSeriesSpinner.getValue();
      FMZVideoPlayerConfiguration.Playback.MAX_EPISODES_PER_DAY = maxEpisodesSpinner.getValue();
      savePlayerConfiguration();
      Stage stage = (Stage) videoMainSourceField.getScene().getWindow();
      stage.close();
      FMZVideoPlayerApplication.launchMainPlayer();
   }

   private String toHexString(Color color)
   {
      int r = ((int) Math.round(color.getRed() * 255));
      int g = ((int) Math.round(color.getGreen() * 255));
      int b = ((int) Math.round(color.getBlue() * 255));
      return String.format("#%02X%02X%02X", r, g, b);
   }
}