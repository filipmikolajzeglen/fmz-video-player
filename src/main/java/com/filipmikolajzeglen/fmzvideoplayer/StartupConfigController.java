package com.filipmikolajzeglen.fmzvideoplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class StartupConfigController
{
   private FMZDatabase<PlayerConfiguration> configDatabase;
   private Map<Toggle, VBox> tabMapping;
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
   private ToggleButton tvScheduleTab;
   @FXML
   private VBox quickStartContent;
   @FXML
   private VBox advancedContent;
   @FXML
   private VBox aboutContent;
   @FXML
   private VBox tvScheduleContent;
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
   private CheckBox commercialsEnabledCheckBox;
   @FXML
   private Spinner<Integer> commercialsCountSpinner;
   @FXML
   private CheckBox useCustomScheduleCheckBox;
   @FXML
   private ComboBox<String> seriesComboBox;
   @FXML
   private ListView<String> scheduleListView;
   @FXML
   private GridPane scheduleGridPane;
   @FXML
   private HBox scheduleButtonsHBox;

   @FXML
   public void initialize()
   {
      seriesNameColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.70));
      episodeCountColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.262));

      seriesNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
      episodeCountColumn.setCellValueFactory(
            data -> new SimpleIntegerProperty(data.getValue().getEpisodeCount()).asObject());
      videoMainSourceField.textProperty().addListener((obs, oldVal, newVal) -> updateSeriesData(newVal));

      tabMapping = new HashMap<>();
      tabMapping.put(quickStartTab, quickStartContent);
      tabMapping.put(advancedTab, advancedContent);
      tabMapping.put(aboutTab, aboutContent);
      tabMapping.put(tvScheduleTab, tvScheduleContent);

      tabsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
         if (newToggle == null)
         {
            tabsGroup.selectToggle(oldToggle);
            return;
         }
         tabMapping.values().forEach(pane -> {
            pane.setVisible(false);
            pane.setManaged(false);
         });
         VBox selectedPane = tabMapping.get(newToggle);
         if (selectedPane != null)
         {
            selectedPane.setVisible(true);
            selectedPane.setManaged(true);
         }
      });

      playButton.disableProperty().bind(Bindings.isEmpty(videoMainSourceField.textProperty()));
      videoMainSourcePrompt.visibleProperty().bind(Bindings.isEmpty(videoMainSourceField.textProperty()));
      loadPlayerConfiguration();
      if (!videoMainSourceField.getText().isEmpty())
      {
         updateSeriesData(videoMainSourceField.getText());
      }

      iconStyleComboBox.setValue("Filled");
      iconStyleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateIconPreview());
      updateIconPreview();

      commercialsCountSpinner.disableProperty().bind(Bindings.not(commercialsEnabledCheckBox.selectedProperty()));
      primaryColorPicker.valueProperty().addListener((obs, oldColor, newColor) -> {
         if (newColor != null)
         {
            updateSliderPreviewColor(newColor);
         }
      });

      updateSliderPreviewColor(primaryColorPicker.getValue());

      scheduleGridPane.disableProperty().bind(useCustomScheduleCheckBox.selectedProperty().not());
      scheduleListView.disableProperty().bind(useCustomScheduleCheckBox.selectedProperty().not());
      scheduleButtonsHBox.disableProperty().bind(useCustomScheduleCheckBox.selectedProperty().not());
   }

   private void updateSliderPreviewColor(Color color)
   {
      String hexColor = toHexString(color);
      String style = String.format("-fx-background-color: linear-gradient(to right, %s 50%%, #D3D3D3 50%%);", hexColor);

      if (colorPreviewSlider.lookup(".track") != null)
      {
         colorPreviewSlider.lookup(".track").setStyle(style);
      }
      else
      {
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

   private void updateSeriesData(String path)
   {
      List<SeriesInfo> series = getSeriesFolders(path);
      ObservableList<SeriesInfo> tableItems = FXCollections.observableArrayList(series);
      seriesTable.setItems(tableItems);

      List<String> seriesNames = series.stream()
            .map(SeriesInfo::getName)
            .sorted()
            .collect(Collectors.toList());
      ObservableList<String> comboItems = FXCollections.observableArrayList(seriesNames);
      seriesComboBox.setItems(comboItems);
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
                  .filter(dir -> !dir.getName()
                        .equalsIgnoreCase(FMZVideoPlayerConfiguration.Paths.COMMERCIALS_FOLDER_NAME))
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
         commercialsEnabledCheckBox.setSelected(config.isAdsEnabled());

         if (config.getIconStyle() != null)
         {
            iconStyleComboBox.setValue(config.getIconStyle());
         }
         if (config.getPrimaryColor() != null)
         {
            primaryColorPicker.setValue(Color.web(config.getPrimaryColor()));
         }

         useCustomScheduleCheckBox.setSelected(config.isUseCustomSchedule());
         if (config.getCustomSchedule() != null)
         {
            scheduleListView.setItems(FXCollections.observableArrayList(config.getCustomSchedule()));
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
            toHexString(primaryColorPicker.getValue()),
            commercialsEnabledCheckBox.isSelected(),
            commercialsCountSpinner.getValue(),
            useCustomScheduleCheckBox.isSelected(),
            new ArrayList<>(scheduleListView.getItems())
      );
      configDatabase.saveAll(List.of(config));
   }

   private File getConfigFile()
   {
      String appDataPath = System.getenv("APPDATA");
      File configDir = new File(appDataPath, "FMZVideoPlayer");
      if (!configDir.exists())
      {
         configDir.mkdirs();
      }
      return new File(configDir, FMZVideoPlayerConfiguration.Paths.FMZ_DATABASE_NAME + ".json");
   }

   @FXML
   private void onChooseFolderClicked()
   {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("Select Video Source Folder");
      File selectedDirectory = directoryChooser.showDialog(videoMainSourceField.getScene().getWindow());
      if (selectedDirectory != null)
      {
         videoMainSourceField.setText(selectedDirectory.getAbsolutePath());
      }
   }

   @FXML
   private void onPlayClicked()
   {
      // 1. Zapisz konfigurację do pliku
      savePlayerConfiguration();

      // 2. Ustaw wartości w statycznym obiekcie konfiguracyjnym
      String selectedStyle = iconStyleComboBox.getValue();
      FMZVideoPlayerConfiguration.Icons.PATH_TO_ICONS = "Empty".equals(selectedStyle) ? "/svg/empty" : "/svg/filled";

      Color selectedColor = primaryColorPicker.getValue();
      FMZVideoPlayerConfiguration.UI.PRIMARY_COLOR = toHexString(selectedColor);

      String mainSource = videoMainSourceField.getText();
      File file = new File(mainSource);
      FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE = mainSource;
      FMZVideoPlayerConfiguration.Paths.FMZ_DIRECTORY_PATH = file.getParent();
      FMZVideoPlayerConfiguration.Paths.FMZ_TABLE_NAME = file.getName();

      // Ustawienia dla Quick Start
      FMZVideoPlayerConfiguration.Playback.MAX_SINGLE_SERIES_PER_DAY = maxSeriesSpinner.getValue();
      FMZVideoPlayerConfiguration.Playback.MAX_EPISODES_PER_DAY = maxEpisodesSpinner.getValue();

      // Ustawienia dla reklam
      FMZVideoPlayerConfiguration.Playback.COMMERCIALS_ENABLED = commercialsEnabledCheckBox.isSelected();
      FMZVideoPlayerConfiguration.Playback.COMMERCIAL_COUNT_BETWEEN_EPISODES = commercialsCountSpinner.getValue();

      // Ustawienia dla harmonogramu TV
      boolean useCustomSchedule = useCustomScheduleCheckBox.isSelected();
      FMZVideoPlayerConfiguration.Playback.USE_CUSTOM_SCHEDULE = useCustomSchedule;
      if (useCustomSchedule)
      {
         FMZVideoPlayerConfiguration.Playback.CUSTOM_SCHEDULE = new ArrayList<>(scheduleListView.getItems());
      }
      else
      {
         FMZVideoPlayerConfiguration.Playback.CUSTOM_SCHEDULE = null;
      }

      // 3. Zamknij okno konfiguracji i uruchom odtwarzacz
      Stage stage = (Stage) playButton.getScene().getWindow();
      stage.hide();
      FMZVideoPlayerApplication.launchMainPlayer(stage);
   }

   @FXML
   private void onAddSeriesToSchedule()
   {
      String selectedSeries = seriesComboBox.getValue();
      if (selectedSeries != null && !selectedSeries.isEmpty())
      {
         scheduleListView.getItems().add(selectedSeries);
      }
   }

   @FXML
   private void onMoveSeriesUp()
   {
      int selectedIndex = scheduleListView.getSelectionModel().getSelectedIndex();
      if (selectedIndex > 0)
      {
         ObservableList<String> items = scheduleListView.getItems();
         String item = items.remove(selectedIndex);
         items.add(selectedIndex - 1, item);
         scheduleListView.getSelectionModel().select(selectedIndex - 1);
      }
   }

   @FXML
   private void onMoveSeriesDown()
   {
      int selectedIndex = scheduleListView.getSelectionModel().getSelectedIndex();
      ObservableList<String> items = scheduleListView.getItems();
      if (selectedIndex != -1 && selectedIndex < items.size() - 1)
      {
         String item = items.remove(selectedIndex);
         items.add(selectedIndex + 1, item);
         scheduleListView.getSelectionModel().select(selectedIndex + 1);
      }
   }

   @FXML
   private void onRemoveSeriesFromSchedule()
   {
      int selectedIndex = scheduleListView.getSelectionModel().getSelectedIndex();
      if (selectedIndex != -1)
      {
         scheduleListView.getItems().remove(selectedIndex);
      }
   }

   private String toHexString(Color color)
   {
      return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
   }
}