package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import lombok.Getter;

public class PlayerRenameToolView
{
   //@formatter:off
   @FXML private Label selectedFolderLabel;
   @FXML private TextField ignoreTextField;
   @FXML private TextField seasonTextField;
   @FXML private TableView<RenameFileItem> filesTableView;
   @FXML private TableColumn<RenameFileItem, String> originalNameColumn;
   @FXML private TableColumn<RenameFileItem, String> newNameColumn;
   @FXML private Button applyButton;
   //@formatter:on

   private final ObservableList<RenameFileItem> fileItems = FXCollections.observableArrayList();
   private File selectedDirectory;

   @FXML
   public void initialize()
   {
      originalNameColumn.setCellValueFactory(cellData -> cellData.getValue().originalNameProperty());
      newNameColumn.setCellValueFactory(cellData -> cellData.getValue().newNameProperty());
      filesTableView.setItems(fileItems);

      filesTableView.setEditable(true);
      newNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

      newNameColumn.setOnEditCommit(event -> {
         RenameFileItem item = event.getRowValue();
         item.setNewName(event.getNewValue());
      });

      ignoreTextField.textProperty().addListener((obs, oldText, newText) -> generateNewNames());
      seasonTextField.textProperty().addListener((obs, oldText, newText) -> generateNewNames());
   }

   @FXML
   private void handleSelectFolder()
   {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("Select a folder with video files");
      File directory = directoryChooser.showDialog(selectedFolderLabel.getScene().getWindow());

      if (directory != null)
      {
         selectedDirectory = directory;
         selectedFolderLabel.setText(selectedDirectory.getAbsolutePath());
         loadFiles();
      }
   }

   private void loadFiles()
   {
      fileItems.clear();
      if (selectedDirectory != null)
      {
         File[] files = selectedDirectory.listFiles((dir, name) ->
               name.toLowerCase().endsWith(".mp4") ||
                     name.toLowerCase().endsWith(".mkv") ||
                     name.toLowerCase().endsWith(".avi"));

         if (files != null)
         {
            for (File file : files)
            {
               fileItems.add(new RenameFileItem(file));
            }
            generateNewNames();
         }
      }
   }

   private void generateNewNames() {
      String[] textsToIgnore = Arrays.stream(ignoreTextField.getText().split(";"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);

      String seasonText = seasonTextField.getText();

      if (!seasonText.isEmpty()) {
         try {
            Integer.parseInt(seasonText);
         } catch (NumberFormatException e) {
            for (RenameFileItem item : fileItems) item.setNewName("");
            return;
         }
      }

      fileItems.sort(Comparator.comparing(RenameFileItem::getOriginalName));

      Map<String, Integer> episodeNumberMap = new LinkedHashMap<>();
      int episodeCounter = 1;

      Pattern newFormatPattern = Pattern.compile("(\\d+)[.](\\d+)([a-zA-Z])?.*");
      for (RenameFileItem item : fileItems) {
         Matcher matcher = newFormatPattern.matcher(item.getOriginalName());
         if (matcher.find()) {
            String episodeKey = matcher.group(1) + "." + matcher.group(2);
            if (!episodeNumberMap.containsKey(episodeKey)) {
               episodeNumberMap.put(episodeKey, episodeCounter++);
            }
         }
      }

      Pattern sXXeYYPattern = Pattern.compile("[sS](\\d{1,2})[eE](\\d{1,2})");

      for (RenameFileItem item : fileItems) {
         String originalName = item.getOriginalName();
         String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
         String extension = originalName.substring(originalName.lastIndexOf('.'));

         int season = -1, episode = -1;
         String part = null, title = "";

         Matcher newFormatMatcher = newFormatPattern.matcher(baseName);
         Matcher sXXeYYMatcher = sXXeYYPattern.matcher(baseName);

         if (newFormatMatcher.find()) {
            season = Integer.parseInt(newFormatMatcher.group(1));
            String episodeKey = newFormatMatcher.group(1) + "." + newFormatMatcher.group(2);
            episode = episodeNumberMap.getOrDefault(episodeKey, -1);
            part = newFormatMatcher.group(3);
            int titleStartIndex = baseName.indexOf('-');
            title = (titleStartIndex != -1) ? baseName.substring(titleStartIndex + 1).trim() : "";
         } else if (sXXeYYMatcher.find()) {
            season = Integer.parseInt(sXXeYYMatcher.group(1));
            episode = Integer.parseInt(sXXeYYMatcher.group(2));
            title = baseName.replaceFirst(sXXeYYPattern.pattern(), "").trim();
         }

         if (!seasonText.isEmpty()) {
            season = Integer.parseInt(seasonText);
         }

         if (season == -1 || episode == -1) {
            item.setNewName("Cannot process");
            continue;
         }

         for (String textToIgnore : textsToIgnore) {
            title = title.replaceAll("(?i)" + Pattern.quote(textToIgnore), "").trim();
         }

         String partStr = (part != null && !part.isEmpty()) ? "-(" + part.toUpperCase() + ")" : "";
         String episodeTitle = title.replaceAll("\\s+", "-");

         item.setNewName(String.format("S%02dE%02d%s-%s%s", season, episode, partStr, episodeTitle, extension));
      }
   }

   @FXML
   private void handleApplyChanges()
   {
      for (RenameFileItem item : fileItems)
      {
         File oldFile = item.getOriginalFile();
         File newFile = new File(oldFile.getParent(), item.getNewName());
         if (oldFile.renameTo(newFile))
         {
            System.out.println("Renamed: " + oldFile.getName() + " -> " + newFile.getName());
         }
         else
         {
            System.err.println("Error renaming file: " + oldFile.getName());
         }
      }
      loadFiles(); // Odśwież widok po zmianie
   }

   static class RenameFileItem
   {
      @Getter
      private final File originalFile;
      private final StringProperty originalName;
      private final StringProperty newName;

      public RenameFileItem(File originalFile)
      {
         this.originalFile = originalFile;
         this.originalName = new SimpleStringProperty(originalFile.getName());
         this.newName = new SimpleStringProperty(""); // Początkowo puste
      }

      public String getOriginalName()
      {
         return originalName.get();
      }

      public StringProperty originalNameProperty()
      {
         return originalName;
      }

      public String getNewName()
      {
         return newName.get();
      }

      public void setNewName(String newName)
      {
         this.newName.set(newName);
      }

      public StringProperty newNameProperty()
      {
         return newName;
      }
   }
}