package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;

import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConfiguration;
import com.filipmikolajzeglen.fmzvideoplayer.player.PlayerConstants;
import com.filipmikolajzeglen.fmzvideoplayer.video.Video;
import com.filipmikolajzeglen.fmzvideoplayer.video.effect.VideoIconsEffect;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class PlayerAdvancedSettingsView
{
   //@formatter:off
   @FXML private VBox advancedContent;
   @FXML private ComboBox<String> iconStyleComboBox;
   @FXML private SVGPath previewPlayIcon;
   @FXML private SVGPath previewPauseIcon;
   @FXML private SVGPath previewNextIcon;
   @FXML private SVGPath previewVolumeIcon;
   @FXML private ColorPicker primaryColorPicker;
   @FXML private Slider colorPreviewSlider;
   @FXML private CheckBox commercialsEnabledCheckBox;
   @FXML private Spinner<Integer> commercialsCountSpinner;
   @FXML private TextField commercialsPathField;
   @FXML private Button browseCommercialsPathButton;
   @FXML private Button deleteVideoDbButton;
   @FXML private Button restoreDefaultsButton;
   //@formatter:on

   @FXML
   public void initialize()
   {
      advancedContent.getProperties().put("controller", this);

      iconStyleComboBox.setValue("Filled");
      iconStyleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateIconPreview());
      updateIconPreview();

      commercialsCountSpinner.disableProperty().bind(Bindings.not(commercialsEnabledCheckBox.selectedProperty()));
      commercialsPathField.disableProperty().bind(Bindings.not(commercialsEnabledCheckBox.selectedProperty()));
      browseCommercialsPathButton.disableProperty().bind(Bindings.not(commercialsEnabledCheckBox.selectedProperty()));

      primaryColorPicker.valueProperty().addListener((obs, oldColor, newColor) -> {
         if (newColor != null)
         {
            updateSliderPreviewColor(newColor);
         }
      });
      updateSliderPreviewColor(primaryColorPicker.getValue());
   }

   @FXML
   private void onBrowseCommercialsPath()
   {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("Select a folder with commercial files");
      File directory = directoryChooser.showDialog(commercialsPathField.getScene().getWindow());

      if (directory != null)
      {
         commercialsPathField.setText(directory.getAbsolutePath());
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

      previewPlayIcon.setContent(VideoIconsEffect.loadSvgContent(pathPrefix + "/play.svg"));
      previewPauseIcon.setContent(VideoIconsEffect.loadSvgContent(pathPrefix + "/pause.svg"));
      previewNextIcon.setContent(VideoIconsEffect.loadSvgContent(pathPrefix + "/next.svg"));
      previewVolumeIcon.setContent(VideoIconsEffect.loadSvgContent(pathPrefix + "/volume2.svg"));
   }

   public String toHexString(Color color)
   {
      return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
   }

   @FXML
   private void onDeleteVideoDb(ActionEvent event) {
      File videoFileJson = getConfigFile(Video.class.getSimpleName());
      if (videoFileJson.exists()) {
         var removed = videoFileJson.delete();
         log.info("Video database file deleted: {}", removed);
      }
   }

   @FXML
   private void onRestoreDefaults(ActionEvent event) {
      File configFileJson = getConfigFile(PlayerConfiguration.class.getSimpleName());
      if (configFileJson.exists()) {
         var removed = configFileJson.delete();
         log.info("Player configuration file deleted: {}", removed);
      }
   }

   @SuppressWarnings("ResultOfMethodCallIgnored")
   private File getConfigFile(String configTableName)
   {
      File configDir = new File(PlayerConstants.Paths.APP_DATA_DIRECTORY);
      if (!configDir.exists())
      {
         configDir.mkdirs();
      }
      var configFileName = String.format("%s_%s.json",
            PlayerConstants.Paths.FMZ_DATABASE_NAME, configTableName);
      return new File(configDir, configFileName);
   }
}