package com.filipmikolajzeglen.fmzvideoplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.filipmikolajzeglen.fmzvideoplayer.database.FMZDatabase;
import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoMetadataReader;
import com.filipmikolajzeglen.fmzvideoplayer.video.VideoPlayerIcons;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class StartupConfigController
{
   private static final Logger LOGGER = new Logger();

   private FMZDatabase<PlayerConfiguration> configDatabase;
   private Map<Toggle, Region> tabMapping;
   @FXML
   private TableView<SeriesInfo> seriesTable;
   @FXML
   private TableColumn<SeriesInfo, String> seriesNameColumn;
   @FXML
   private TableColumn<SeriesInfo, Integer> episodeCountColumn;
   @FXML
   private TableColumn<SeriesInfo, String> totalWatchingTimeColumn;
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
   private VBox quickStartContent;
   @FXML
   private ScrollPane libraryContent;
   @FXML
   private TilePane libraryTilePane;
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
   private VBox consoleLogContent;
   @FXML
   private TextArea consoleLogTextArea;

   /**
    * Inner class representing information about a series.
    */
   public static class SeriesInfo
   {
      private final SimpleStringProperty name;
      private final SimpleIntegerProperty episodeCount;
      private final SimpleStringProperty totalWatchingTime;

      public SeriesInfo(String name, int episodeCount)
      {
         this.name = new SimpleStringProperty(name);
         this.episodeCount = new SimpleIntegerProperty(episodeCount);
         this.totalWatchingTime = new SimpleStringProperty("...");
      }

      public String getName()
      {
         return name.get();
      }

      public SimpleStringProperty nameProperty()
      {
         return name;
      }

      public int getEpisodeCount()
      {
         return episodeCount.get();
      }

      public SimpleIntegerProperty episodeCountProperty()
      {
         return episodeCount;
      }

      public String getTotalWatchingTime()
      {
         return totalWatchingTime.get();
      }

      public SimpleStringProperty totalWatchingTimeProperty()
      {
         return totalWatchingTime;
      }

      public void setTotalWatchingTime(String time)
      {
         this.totalWatchingTime.set(time);
      }
   }

   @FXML
   public void initialize()
   {
      // Zarejestruj słuchacza, który będzie aktualizował pole TextArea
      Logger.addListener(plainMessage -> {
         Platform.runLater(() -> {
            consoleLogTextArea.appendText(plainMessage + "\n");
         });
      });

      // Ustawienie szerokości kolumn
      seriesNameColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.50));
      episodeCountColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.20));
      totalWatchingTimeColumn.prefWidthProperty().bind(seriesTable.widthProperty().multiply(0.26));

      // Bindowanie danych do kolumn
      seriesNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
      episodeCountColumn.setCellValueFactory(data -> data.getValue().episodeCountProperty().asObject());
      totalWatchingTimeColumn.setCellValueFactory(data -> data.getValue().totalWatchingTimeProperty());

      videoMainSourceField.textProperty().addListener((obs, oldVal, newVal) -> updateSeriesData(newVal));

      tabMapping = new HashMap<>();
      tabMapping.put(quickStartTab, quickStartContent);
      tabMapping.put(libraryTab, libraryContent);
      tabMapping.put(advancedTab, advancedContent);
      tabMapping.put(aboutTab, aboutContent);
      tabMapping.put(tvScheduleTab, tvScheduleContent);
      tabMapping.put(consoleLogTab, consoleLogContent);

      tabsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
         if (newToggle == null)
         {
            tabsGroup.selectToggle(oldToggle);
            return;
         }

         // Jeśli wybrano zakładkę Biblioteka, odśwież widok
         if (newToggle == libraryTab)
         {
            // Zresetuj do głównego widoku kafelków
            libraryContent.setContent(libraryTilePane);
            // Odśwież dane
            updateSeriesData(videoMainSourceField.getText());
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

      // Start duration calculation in the background
      startDurationCalculation(path, tableItems);

      List<String> seriesNames = series.stream()
            .map(SeriesInfo::getName)
            .sorted()
            .collect(Collectors.toList());
      ObservableList<String> comboItems = FXCollections.observableArrayList(seriesNames);
      seriesComboBox.setItems(comboItems);

      updateLibraryView(path, series);
   }

   private void updateLibraryView(String basePath, List<SeriesInfo> series)
   {
      libraryTilePane.getChildren().clear();
      for (SeriesInfo seriesInfo : series)
      {
         VBox seriesTile = createSeriesTile(basePath, seriesInfo);
         libraryTilePane.getChildren().add(seriesTile);
      }
   }

   private VBox createSeriesTile(String basePath, SeriesInfo seriesInfo)
   {
      VBox tileContainer = new VBox(5);
      tileContainer.setAlignment(Pos.TOP_CENTER);
      tileContainer.setOnMouseClicked(event -> {
         if (event.getButton() == MouseButton.PRIMARY)
         {
            showSeriesDetailView(basePath, seriesInfo);
         }
      });

      Node coverView;
      File coverFile = findCoverFile(basePath, seriesInfo.getName());

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
            LOGGER.error("Failed to load cover image: " + coverFile.getPath());
            coverView = createCoverPlaceholder(seriesInfo.getName());
         }
      }
      else
      {
         coverView = createCoverPlaceholder(seriesInfo.getName());
      }

      Label titleLabel = new Label(seriesInfo.getName());
      titleLabel.setWrapText(true);
      titleLabel.setTextAlignment(TextAlignment.CENTER);

      // Kluczowe rozwiązanie: Wrapper `StackPane` z narzuconą szerokością.
      // `StackPane` domyślnie wycentruje `titleLabel` wewnątrz siebie.
      StackPane titleWrapper = new StackPane(titleLabel);
      titleWrapper.setPrefWidth(150); // Ustawia szerokość identyczną jak dla okładki.

      tileContainer.getChildren().addAll(coverView, titleWrapper);
      return tileContainer;
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

      // Usuwa wszystko, co nie jest literą, cyfrą ani białą spacją.
      String cleanedName = name.replaceAll("[^a-zA-Z0-9\\s]", "");
      String[] words = cleanedName.trim().split("\\s+");

      // Filtruj puste stringi, które mogły powstać
      List<String> validWords = Arrays.stream(words)
            .filter(w -> !w.isEmpty())
            .collect(Collectors.toList());

      if (validWords.isEmpty())
      {
         return "?";
      }

      if (validWords.size() > 1)
      {
         // Dwa lub więcej słów: pierwsze litery pierwszych dwóch słów.
         String firstInitial = validWords.get(0).substring(0, 1);
         String secondInitial = validWords.get(1).substring(0, 1);
         return (firstInitial + secondInitial).toUpperCase();
      }
      else
      {
         // Jedno słowo.
         String word = validWords.get(0);
         if (word.length() > 1)
         {
            // Zwróć pierwsze dwie litery.
            return word.substring(0, 2).toUpperCase();
         }
         else
         {
            // Zwróć jedną literę.
            return word.substring(0, 1).toUpperCase();
         }
      }
   }

   private Color generateRandomColor()
   {
      Random random = new Random();
      double hue = random.nextDouble() * 360;
      double saturation = 0.5 + random.nextDouble() * 0.2; // 0.5-0.7 for pleasant colors
      double brightness = 0.5 + random.nextDouble() * 0.2; // 0.5-0.7 to avoid too light/dark
      return Color.hsb(hue, saturation, brightness);
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
      configDatabase.setTableName(FMZVideoPlayerConfiguration.Paths.CONFIG_TABLE_NAME);
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

   private void showSeriesDetailView(String basePath, SeriesInfo seriesInfo)
   {
      BorderPane detailView = new BorderPane();
      detailView.setPadding(new Insets(10));

      TableView<EpisodeInfo> episodeTable = new TableView<>();
      TableColumn<EpisodeInfo, String> nameColumn = new TableColumn<>("Tytuł odcinka");
      nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
      episodeTable.getColumns().add(nameColumn);
      nameColumn.prefWidthProperty().bind(episodeTable.widthProperty().multiply(0.98));

      List<EpisodeInfo> episodes = getEpisodesForSeries(basePath, seriesInfo.getName());
      episodeTable.setItems(FXCollections.observableArrayList(episodes));
      detailView.setCenter(episodeTable);

      VBox rightPane = new VBox(10);
      rightPane.setAlignment(Pos.TOP_CENTER);
      rightPane.setPadding(new Insets(0, 0, 0, 10));

      Node coverView;
      File coverFile = findCoverFile(basePath, seriesInfo.getName());
      if (coverFile != null)
      {
         try
         {
            Image coverImage = new Image(coverFile.toURI().toString(), 150, 225, false, true);
            coverView = new ImageView(coverImage);
         }
         catch (Exception e)
         {
            coverView = createCoverPlaceholder(seriesInfo.getName());
         }
      }
      else
      {
         coverView = createCoverPlaceholder(seriesInfo.getName());
      }

      Button playAllButton = new Button("Odtwórz wszystko");
      playAllButton.setOnAction(event -> {
         FMZVideoPlayerConfiguration.Playback.PLAYLIST_TO_START = seriesInfo.getName();
         onPlayClicked();
      });

      Button backButton = new Button("Wróć do biblioteki");
      backButton.setOnAction(event -> libraryContent.setContent(libraryTilePane));

      rightPane.getChildren().addAll(coverView, playAllButton, backButton);
      detailView.setRight(rightPane);

      libraryContent.setContent(detailView);
   }

   private void startDurationCalculation(String basePath, ObservableList<SeriesInfo> seriesList)
   {
      Task<Void> task = new Task<>()
      {
         @Override
         protected Void call()
         {
            for (SeriesInfo seriesInfo : seriesList)
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
}