package com.filipmikolajzeglen.fmzvideoplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.filipmikolajzeglen.fmzvideoplayer.database.FMZDatabase;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerIcons;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class StartupConfigController
{
   private FMZDatabase<PlayerConfiguration> configDatabase;
   private Map<Toggle, Region> tabMapping;

   @FXML
   private ToggleGroup tabsGroup;
   @FXML
   private ToggleButton quickStartTab;
   @FXML
   private ToggleButton libraryTab;
   @FXML
   private ToggleButton advancedTab;
   @FXML
   private ToggleButton aboutTab;
   @FXML
   private ToggleButton consoleLogTab;
   @FXML
   private ToggleButton tvScheduleTab;
   @FXML
   private AnchorPane libraryContent;
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
   private VBox quickStartContent; // zmiana typu na VBox
   @FXML
   private VBox consoleLogContent; // <-- dodaj to pole

   private QuickStartTabController quickStartTabController; // referencja do kontrolera

   // Dodaj pole do obsługi LibraryTabController
   private LibraryTabController libraryTabController;

   // Dodaj pole do obsługi ConsoleLogsTabController
   private ConsoleLogsTabController consoleLogsTabController;

   @FXML
   private Button playButton;

   @FXML
   public void initialize()
   {
      // Pobierz kontroler ConsoleLogsTabController z consoleLogContent
      if (consoleLogContent != null) {
         consoleLogsTabController = (ConsoleLogsTabController) consoleLogContent.getProperties().get("controller");
      }

      // Pobierz kontroler QuickStartTabController z quickStartContent
      if (quickStartContent != null) {
         quickStartTabController = (QuickStartTabController) quickStartContent.getProperties().get("controller");
      }

      // Pobierz kontroler LibraryTabController z libraryContent
      if (libraryContent != null) {
         libraryTabController = (LibraryTabController) libraryContent.getProperties().get("controller");
         if (libraryTabController != null) {
            libraryTabController.setStartupConfigController(this);
         }
      }

      // Dodaj słuchacza do pola tekstowego ze ścieżką źródłową
      if (quickStartTabController != null) {
         quickStartTabController.getVideoMainSourceField().textProperty().addListener((obs, oldPath, newPath) -> {
            if (libraryTabController != null && newPath != null && !newPath.isEmpty()) {
               libraryTabController.refreshLibrary(
                     newPath,
                     quickStartTabController.getSeriesFolders(newPath)
               );
            }
         });
      }

      tabMapping = new HashMap<>();
      tabMapping.put(libraryTab, libraryContent);
      tabMapping.put(advancedTab, advancedContent);
      tabMapping.put(aboutTab, aboutContent);
      tabMapping.put(tvScheduleTab, tvScheduleContent);
      // Zamień consoleLogContent na VBox z include
      tabMapping.put(consoleLogTab, consoleLogContent);

      tabsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
         if (newToggle == null)
         {
            tabsGroup.selectToggle(oldToggle);
            return;
         }

         // NIE odświeżaj biblioteki tutaj!

         if (newToggle == tvScheduleTab)
         {
            updateSeriesComboBox();
         }

         tabMapping.values().forEach(pane -> {
            pane.setVisible(false);
            pane.setManaged(false);
         });
         Region selectedPane = tabMapping.get(newToggle);
         if (selectedPane != null)
         {
            selectedPane.setVisible(true);
            selectedPane.setManaged(true);
         }
      });

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

      loadPlayerConfiguration();
   }

   // Pomocnicza metoda do pobierania node po fx:id (dla VBox z include)
   // NIE jest już potrzebna, można zostawić lub usunąć jeśli nieużywana
   private javafx.scene.Node getNodeById(String id) {
      // Przeszukaj drzewo sceny od playButton
      return playButton.getScene().lookup("#" + id);
   }

   // Dodaj metodę do aktualizacji ComboBoxa z listą serii w TV Schedule
   private void updateSeriesComboBox() {
      if (quickStartTabController == null) return;
      List<SeriesInfo> seriesList = quickStartTabController.getSeriesFolders(quickStartTabController.getVideoMainSourcePath());
      seriesComboBox.getItems().clear();
      for (SeriesInfo series : seriesList) {
         seriesComboBox.getItems().add(series.getName());
      }
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

   private void loadPlayerConfiguration()
   {
      File configFile = getConfigFile();
      configDatabase = new FMZDatabase<>(PlayerConfiguration.class);
      configDatabase.setDatabaseName(configFile.getName().replace(".json", ""));
      configDatabase.setDirectoryPath(configFile.getParent());
      configDatabase.setTableName(FMZVideoPlayerConfiguration.Paths.CONFIG_TABLE_NAME);
      configDatabase.initialize();
      if (!configDatabase.findAll().isEmpty())
      {
         PlayerConfiguration config = configDatabase.findAll().get(0);

         // Ustawienia QuickStartTab
         if (quickStartTabController != null) {
            quickStartTabController.getMaxSeriesSpinner().getValueFactory().setValue(config.getMaxSingleSeriesPerDay());
            quickStartTabController.getMaxEpisodesSpinner().getValueFactory().setValue(config.getMaxEpisodesPerDay());
            quickStartTabController.getVideoMainSourceField().setText(config.getVideoMainSourcePath());
         }

         commercialsEnabledCheckBox.setSelected(config.isCommercialsEnabled());

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

      // Po wczytaniu konfiguracji i ustawieniu ścieżki, załaduj bibliotekę JEDEN raz
      if (libraryTabController != null && quickStartTabController != null) {
         libraryTabController.refreshLibrary(
            quickStartTabController.getVideoMainSourcePath(),
            quickStartTabController.getSeriesFolders(quickStartTabController.getVideoMainSourcePath())
         );
      }
   }

   private void savePlayerConfiguration()
   {
      PlayerConfiguration config = new PlayerConfiguration(
            quickStartTabController.getMaxSingleSeriesPerDay(),
            quickStartTabController.getMaxEpisodesPerDay(),
            quickStartTabController.getVideoMainSourcePath(),
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

   // Zmień widoczność metody na publiczną, aby LibraryTabController mógł ją wywołać
   public void onPlayClicked()
   {
      // 1. Zapisz konfigurację do pliku
      savePlayerConfiguration();

      // 2. Ustaw wartości w statycznym obiekcie konfiguracyjnym
      String selectedStyle = iconStyleComboBox.getValue();
      FMZVideoPlayerConfiguration.Icons.PATH_TO_ICONS = "Empty".equals(selectedStyle) ? "/svg/empty" : "/svg/filled";

      Color selectedColor = primaryColorPicker.getValue();
      FMZVideoPlayerConfiguration.UI.PRIMARY_COLOR = toHexString(selectedColor);

      // Ustawienia dla Quick Start
      FMZVideoPlayerConfiguration.Playback.MAX_SINGLE_SERIES_PER_DAY = quickStartTabController.getMaxSingleSeriesPerDay();
      FMZVideoPlayerConfiguration.Playback.MAX_EPISODES_PER_DAY = quickStartTabController.getMaxEpisodesPerDay();
      FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE = quickStartTabController.getVideoMainSourcePath();

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
}
