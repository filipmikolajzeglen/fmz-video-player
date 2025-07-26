package com.filipmikolajzeglen.fmzvideoplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.filipmikolajzeglen.fmzvideoplayer.database.FMZDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
   private VBox advancedContent; // teraz to fx:include
   @FXML
   private VBox aboutContent;
   @FXML
   private VBox tvScheduleContent; // <-- zostaje, bo to fx:include
   @FXML
   private Button playButton;
   @FXML
   private VBox quickStartContent; // zmiana typu na VBox
   @FXML
   private VBox consoleLogContent; // <-- dodaj to pole

   private QuickStartTabController quickStartTabController; // referencja do kontrolera

   // Dodaj pole do obsługi LibraryTabController
   private LibraryTabController libraryTabController;

   // Dodaj pole do obsługi ConsoleLogsTabController
   private ConsoleLogsTabController consoleLogsTabController;

   // Dodaj pole do obsługi TVScheduleTabController
   private TVScheduleTabController tvScheduleTabController;

   // Dodaj pole do obsługi AdvancedSettingsTabController
   private AdvancedSettingsTabController advancedSettingsTabController;

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

      // Pobierz kontroler TVScheduleTabController z tvScheduleContent
      if (tvScheduleContent != null) {
         tvScheduleTabController = (TVScheduleTabController) tvScheduleContent.getProperties().get("controller");
      }

      // Połącz kontrolery: propaguj referencję QuickStartTabController do TVScheduleTabController
      if (tvScheduleTabController != null && quickStartTabController != null) {
         tvScheduleTabController.setQuickStartTabController(quickStartTabController);
      }

      // Pobierz kontroler LibraryTabController z libraryContent
      if (libraryContent != null) {
         libraryTabController = (LibraryTabController) libraryContent.getProperties().get("controller");
         if (libraryTabController != null) {
            libraryTabController.setStartupConfigController(this);
         }
      }

      // Pobierz kontroler AdvancedSettingsTabController z advancedContent
      if (advancedContent != null) {
         advancedSettingsTabController = (AdvancedSettingsTabController) advancedContent.getProperties().get("controller");
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
      tabMapping.put(advancedTab, advancedContent); // advancedContent jako VBox z include
      tabMapping.put(aboutTab, aboutContent);
      tabMapping.put(tvScheduleTab, tvScheduleContent);
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
            if (tvScheduleTabController != null) {
               tvScheduleTabController.updateSeriesComboBox();
            }
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

      loadPlayerConfiguration();
   }

   // Pomocnicza metoda do pobierania node po fx:id (dla VBox z include)
   // NIE jest już potrzebna, można zostawić lub usunąć jeśli nieużywana
   private javafx.scene.Node getNodeById(String id) {
      // Przeszukaj drzewo sceny od playButton
      return playButton.getScene().lookup("#" + id);
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

         // Ustawienia AdvancedSettingsTab
         if (advancedSettingsTabController != null) {
            advancedSettingsTabController.getCommercialsEnabledCheckBox().setSelected(config.isCommercialsEnabled());
            if (config.getIconStyle() != null) {
               advancedSettingsTabController.getIconStyleComboBox().setValue(config.getIconStyle());
            }
            if (config.getPrimaryColor() != null) {
               advancedSettingsTabController.getPrimaryColorPicker().setValue(javafx.scene.paint.Color.web(config.getPrimaryColor()));
            }
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
            advancedSettingsTabController.getIconStyleComboBox().getValue(),
            advancedSettingsTabController.toHexString(advancedSettingsTabController.getPrimaryColorPicker().getValue()),
            advancedSettingsTabController.getCommercialsEnabledCheckBox().isSelected(),
            advancedSettingsTabController.getCommercialsCountSpinner().getValue(),
            tvScheduleTabController.getUseCustomScheduleCheckBox().isSelected(),
            new ArrayList<>(tvScheduleTabController.getScheduleListView().getItems())
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
      String selectedStyle = advancedSettingsTabController.getIconStyleComboBox().getValue();
      FMZVideoPlayerConfiguration.Icons.PATH_TO_ICONS = "Empty".equals(selectedStyle) ? "/svg/empty" : "/svg/filled";

      Color selectedColor = advancedSettingsTabController.getPrimaryColorPicker().getValue();
      FMZVideoPlayerConfiguration.UI.PRIMARY_COLOR = advancedSettingsTabController.toHexString(selectedColor);

      // Ustawienia dla Quick Start
      FMZVideoPlayerConfiguration.Playback.MAX_SINGLE_SERIES_PER_DAY = quickStartTabController.getMaxSingleSeriesPerDay();
      FMZVideoPlayerConfiguration.Playback.MAX_EPISODES_PER_DAY = quickStartTabController.getMaxEpisodesPerDay();
      FMZVideoPlayerConfiguration.Paths.VIDEO_MAIN_SOURCE = quickStartTabController.getVideoMainSourcePath();

      // Ustawienia dla reklam
      FMZVideoPlayerConfiguration.Playback.COMMERCIALS_ENABLED = advancedSettingsTabController.getCommercialsEnabledCheckBox().isSelected();
      FMZVideoPlayerConfiguration.Playback.COMMERCIAL_COUNT_BETWEEN_EPISODES = advancedSettingsTabController.getCommercialsCountSpinner().getValue();

      // Ustawienia dla harmonogramu TV
      boolean useCustomSchedule = tvScheduleTabController.getUseCustomScheduleCheckBox().isSelected();
      FMZVideoPlayerConfiguration.Playback.USE_CUSTOM_SCHEDULE = useCustomSchedule;
      if (useCustomSchedule)
      {
         FMZVideoPlayerConfiguration.Playback.CUSTOM_SCHEDULE = new ArrayList<>(tvScheduleTabController.getScheduleListView().getItems());
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
