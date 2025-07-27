package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
      videoService = createVideoPlayerService();

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

      if (libraryContent != null)
      {
         playerLibraryView = (PlayerLibraryView) libraryContent.getProperties().get("controller");
         if (playerLibraryView != null)
         {
            playerLibraryView.setStartupConfigController(this);
         }
      }

      if (advancedContent != null)
      {
         playerAdvancedSettingsView = (PlayerAdvancedSettingsView) advancedContent.getProperties().get("controller");
      }

      if (playerQuickStartView != null)
      {
         playerQuickStartView.getVideoMainSourceField().textProperty().addListener((obs, oldPath, newPath) -> {
            if (playerLibraryView != null && newPath != null && !newPath.isEmpty())
            {
               playerLibraryView.refreshLibrary(
                     newPath,
                     playerQuickStartView.getSeriesFolders(newPath)
               );
            }
         });
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

      loadPlayerConfiguration();
   }

   private VideoService createVideoPlayerService()
   {
      Database<Video> database = new Database<>(Video.class);
      database.setDatabaseName(PlayerConstants.Paths.FMZ_DATABASE_NAME);
      database.setTableName(PlayerConstants.Paths.FMZ_TABLE_NAME);
      database.setDirectoryPath(PlayerConstants.Paths.APP_DATA_DIRECTORY);
      VideoService videoService = new VideoService(database);
      videoService.initializeFMZDB();
      return videoService;
   }

   private void loadPlayerConfiguration()
   {
      File configFile = getConfigFile();
      configDatabase = new Database<>(PlayerConfiguration.class);
      configDatabase.setDatabaseName(configFile.getName().replace(".json", ""));
      configDatabase.setDirectoryPath(configFile.getParent());
      configDatabase.setTableName(PlayerConstants.Paths.CONFIG_TABLE_NAME);
      configDatabase.initialize();
      if (!configDatabase.findAll().isEmpty())
      {
         PlayerConfiguration config = configDatabase.findAll().get(0);

         if (playerQuickStartView != null)
         {
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
         }
      }

      if (playerLibraryView != null && playerQuickStartView != null)
      {
         playerLibraryView.refreshLibrary(
               playerQuickStartView.getVideoMainSourcePath(),
               playerQuickStartView.getSeriesFolders(playerQuickStartView.getVideoMainSourcePath())
         );
      }
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
            new ArrayList<>(playerTvScheduleView.getScheduleListView().getItems())
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
      return new File(configDir, PlayerConstants.Paths.FMZ_DATABASE_NAME + ".json");
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
