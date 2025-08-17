package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.filipmikolajzeglen.fmzvideoplayer.VideoPlayerApplication;
import com.filipmikolajzeglen.fmzvideoplayer.database.Database;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.video.Video;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayerMainView
{
   private Database<PlayerConfiguration> configDatabase;
   private Map<Toggle, Region> tabMapping;

   //@formatter:off
   @FXML private ToggleGroup tabsGroup;
   @FXML private ToggleButton quickStartTab;
   @FXML private ToggleButton libraryTab;
   @FXML private ToggleButton advancedTab;
   @FXML private ToggleButton aboutTab;
   @FXML private ToggleButton consoleLogTab;
   @FXML private ToggleButton tvScheduleTab;
   @FXML private ToggleButton reNameToolTab;
   @FXML private AnchorPane libraryContent;
   @FXML private VBox advancedContent;
   @FXML private VBox aboutContent;
   @FXML private VBox tvScheduleContent;
   @FXML private BorderPane reNameToolContent;
   @FXML private Button playButton;
   @FXML private VBox quickStartContent;
   @FXML private VBox consoleLogContent;

   @Getter private VideoService videoService;
   //@formatter:on

   private PlayerQuickStartView playerQuickStartView;
   private PlayerLibraryView playerLibraryView;
   private PlayerConsoleLogsView playerConsoleLogsView;
   private PlayerTVScheduleView playerTvScheduleView;
   private PlayerAdvancedSettingsView playerAdvancedSettingsView;

   @FXML
   public void initialize()
   {
      if (consoleLogContent != null)
      {
         playerConsoleLogsView = (PlayerConsoleLogsView) consoleLogContent.getProperties().get("controller");
      }

      if (quickStartContent != null)
      {
         playerQuickStartView = (PlayerQuickStartView) quickStartContent.getProperties().get("controller");
      }

      if (tvScheduleContent != null)
      {
         playerTvScheduleView = (PlayerTVScheduleView) tvScheduleContent.getProperties().get("controller");
      }

      if (playerTvScheduleView != null && playerQuickStartView != null)
      {
         playerTvScheduleView.setPlayerQuickStartView(playerQuickStartView);
      }

      if (advancedContent != null)
      {
         playerAdvancedSettingsView = (PlayerAdvancedSettingsView) advancedContent.getProperties().get("controller");
      }

      if (playerQuickStartView != null)
      {
         // Listener will trigger service and configuration initialization after setting the path.
         playerQuickStartView.getVideoMainSourceField().textProperty().addListener((obs, oldPath, newPath) -> {
            if (newPath != null && !newPath.isEmpty())
            {
               // 1. Create VideoService if it does not exist yet.
               if (videoService == null)
               {
                  this.videoService = createVideoPlayerService();
               }

               // 2. If configDatabase is null, create it and save the configuration.
               if (configDatabase == null)
               {
                  configDatabase = Database.getInstance(PlayerConfiguration.class);
                  configDatabase.ensureFileExists();
                  savePlayerConfiguration();
               }

               // 3. Refresh the library view.
               if (playerLibraryView != null)
               {
                  playerLibraryView.refreshLibrary(newPath, playerQuickStartView.getSeriesFolders(newPath));
               }
            }
         });
      }

      if (libraryContent != null)
      {
         playerLibraryView = (PlayerLibraryView) libraryContent.getProperties().get("controller");
         if (playerLibraryView != null)
         {
            playerLibraryView.setStartupConfigController(this);
         }
      }

      tabMapping = new HashMap<>();
      tabMapping.put(libraryTab, libraryContent);
      tabMapping.put(advancedTab, advancedContent);
      tabMapping.put(aboutTab, aboutContent);
      tabMapping.put(tvScheduleTab, tvScheduleContent);
      tabMapping.put(consoleLogTab, consoleLogContent);
      tabMapping.put(reNameToolTab, reNameToolContent);

      tabsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
         if (newToggle == null)
         {
            tabsGroup.selectToggle(oldToggle);
            return;
         }

         if (newToggle == tvScheduleTab)
         {
            if (playerTvScheduleView != null)
            {
               playerTvScheduleView.updateSeriesComboBox();
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

      // Nasłuchiwanie zmian w polach i automatyczny zapis konfiguracji
      if (playerQuickStartView != null && playerAdvancedSettingsView != null && playerTvScheduleView != null)
      {
         // QuickStart
         playerQuickStartView.getMaxSeriesSpinner().valueProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
         playerQuickStartView.getMaxEpisodesSpinner().valueProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
         playerQuickStartView.getVideoMainSourceField().textProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());

         // Advanced
         playerAdvancedSettingsView.getIconStyleComboBox().valueProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
         playerAdvancedSettingsView.getPrimaryColorPicker().valueProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
         playerAdvancedSettingsView.getCommercialsEnabledCheckBox().selectedProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
         playerAdvancedSettingsView.getCommercialsCountSpinner().valueProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
         playerAdvancedSettingsView.getCommercialsPathField().textProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());

         // TV Schedule
         playerTvScheduleView.getUseCustomScheduleCheckBox().selectedProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
         playerTvScheduleView.getScheduleListView().itemsProperty().addListener((obs, oldVal, newVal) -> savePlayerConfiguration());
      }

      // Move this call to the end of the method.
      loadPlayerConfiguration();
   }

   private VideoService createVideoPlayerService()
   {
      Database<Video> database = Database.getInstance(Video.class);
      VideoService videoService = new VideoService(database);
      videoService.initialize();
      return videoService;
   }

   private void loadPlayerConfiguration()
   {
      File configFile = getConfigFile();
      if (!configFile.exists())
      {
         log.info("Configuration file does not exist: {}", configFile.getAbsolutePath());
         return;
      }

      configDatabase = Database.getInstance(PlayerConfiguration.class);

      if (!configDatabase.readAll().isEmpty())
      {
         PlayerConfiguration config = configDatabase.readAll().getFirst();

         if (playerQuickStartView != null)
         {
            log.info("Loading configuration from file: {}", configFile.getAbsolutePath());
            playerQuickStartView.getMaxSeriesSpinner().getValueFactory().setValue(config.getMaxSingleSeriesPerDay());
            playerQuickStartView.getMaxEpisodesSpinner().getValueFactory().setValue(config.getMaxEpisodesPerDay());
            playerQuickStartView.getVideoMainSourceField().setText(config.getVideoMainSourcePath());
         }

         if (playerAdvancedSettingsView != null)
         {
            playerAdvancedSettingsView.getCommercialsEnabledCheckBox().setSelected(config.isCommercialsEnabled());
            if (config.getIconStyle() != null)
            {
               playerAdvancedSettingsView.getIconStyleComboBox().setValue(config.getIconStyle());
            }
            if (config.getPrimaryColor() != null)
            {
               playerAdvancedSettingsView.getPrimaryColorPicker()
                     .setValue(javafx.scene.paint.Color.web(config.getPrimaryColor()));
            }
            if (config.getCommercialsPath() != null)
            {
               playerAdvancedSettingsView.getCommercialsPathField().setText(config.getCommercialsPath());
               PlayerConstants.Paths.COMMERCIALS_PATH = config.getCommercialsPath();
            }
         }
      }
   }

   private File getConfigFile()
   {
      File configDir = new File(PlayerConstants.Paths.APP_DATA_DIRECTORY);
      if (!configDir.exists())
      {
         configDir.mkdirs();
      }
      var configFileName = String.format("%s_%s.json",
            PlayerConstants.Paths.FMZ_DATABASE_NAME, PlayerConstants.Paths.CONFIG_TABLE_NAME);
      return new File(configDir, configFileName);
   }

   private void savePlayerConfiguration()
   {
      PlayerConfiguration config = new PlayerConfiguration(
            playerQuickStartView.getMaxSingleSeriesPerDay(),
            playerQuickStartView.getMaxEpisodesPerDay(),
            playerQuickStartView.getVideoMainSourcePath(),
            playerAdvancedSettingsView.getIconStyleComboBox().getValue(),
            playerAdvancedSettingsView.toHexString(playerAdvancedSettingsView.getPrimaryColorPicker().getValue()),
            playerAdvancedSettingsView.getCommercialsEnabledCheckBox().isSelected(),
            playerAdvancedSettingsView.getCommercialsCountSpinner().getValue(),
            playerTvScheduleView.getUseCustomScheduleCheckBox().isSelected(),
            new ArrayList<>(playerTvScheduleView.getScheduleListView().getItems()),
            playerAdvancedSettingsView.getCommercialsPathField().getText()
      );
      configDatabase.update(config);
   }

   public void onPlayClicked()
   {
      savePlayerConfiguration();

      String selectedStyle = playerAdvancedSettingsView.getIconStyleComboBox().getValue();
      PlayerConstants.Icons.PATH_TO_ICONS = "Empty".equals(selectedStyle) ? "/svg/empty" : "/svg/filled";

      Color selectedColor = playerAdvancedSettingsView.getPrimaryColorPicker().getValue();
      PlayerConstants.UI.PRIMARY_COLOR = playerAdvancedSettingsView.toHexString(selectedColor);

      PlayerConstants.Playback.MAX_SINGLE_SERIES_PER_DAY = playerQuickStartView.getMaxSingleSeriesPerDay();
      PlayerConstants.Playback.MAX_EPISODES_PER_DAY = playerQuickStartView.getMaxEpisodesPerDay();
      PlayerConstants.Paths.VIDEO_MAIN_SOURCE = playerQuickStartView.getVideoMainSourcePath();

      PlayerConstants.Playback.COMMERCIALS_ENABLED =
            playerAdvancedSettingsView.getCommercialsEnabledCheckBox().isSelected();
      PlayerConstants.Playback.COMMERCIAL_COUNT_BETWEEN_EPISODES =
            playerAdvancedSettingsView.getCommercialsCountSpinner().getValue();

      boolean useCustomSchedule = playerTvScheduleView.getUseCustomScheduleCheckBox().isSelected();
      PlayerConstants.Playback.USE_CUSTOM_SCHEDULE = useCustomSchedule;
      if (useCustomSchedule)
      {
         PlayerConstants.Playback.CUSTOM_SCHEDULE =
               new ArrayList<>(playerTvScheduleView.getScheduleListView().getItems());
      }
      else
      {
         PlayerConstants.Playback.CUSTOM_SCHEDULE = null;
      }

      Stage stage = (Stage) playButton.getScene().getWindow();
      stage.hide();
      VideoPlayerApplication.launchMainPlayer(stage);
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
