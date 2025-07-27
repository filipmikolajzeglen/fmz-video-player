// Utwórz nowy plik:
// src/main/java/com/filipmikolajzeglen/fmzvideoplayer/player/view/RenameToolController.java
package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

public class RenameToolController
{
   //@formatter:off
   @FXML private Label selectedFolderLabel;
   @FXML private TextField ignoreTextField;
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

   private void generateNewNames()
   {
      String textToIgnore = ignoreTextField.getText();
      for (RenameFileItem item : fileItems)
      {
         String newName = generateSingleNewName(item.getOriginalName(), textToIgnore);
         item.setNewName(newName);
      }
   }

   private String generateSingleNewName(String originalName, String textToIgnore)
   {
      String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
      String extension = originalName.substring(originalName.lastIndexOf('.'));
      String tempName = baseName.replaceAll("[._]", " ");

      if (textToIgnore != null && !textToIgnore.isEmpty())
      {
         tempName = tempName.replaceAll("(?i)" + Pattern.quote(textToIgnore), "");
      }

      Pattern sXXeYYPattern = Pattern.compile("[sS](\\d{1,2})[eE](\\d{1,2})");
      Matcher matcher = sXXeYYPattern.matcher(tempName);
      int season = 1, episode = -1;

      if (matcher.find())
      {
         season = Integer.parseInt(matcher.group(1));
         episode = Integer.parseInt(matcher.group(2));
         tempName = tempName.replaceAll(sXXeYYPattern.pattern(), "");
      }
      else
      {
         Pattern numberPattern = Pattern.compile("(\\d+)");
         Matcher numberMatcher = numberPattern.matcher(tempName);
         if (numberMatcher.find())
         {
            episode = Integer.parseInt(numberMatcher.group(1));
            tempName = tempName.replaceFirst(numberMatcher.group(1), "");
         }
      }

      if (episode == -1)
      {
         return "Cannot process";
      }

      // 3. Usuwanie "śmieci" (uproszczone)
      String[] junkKeywords = { "1080p", "720p", "WEB-DL", "HDTV", "x264", "H.265", "AMZN" };
      for (String junk : junkKeywords)
      {
         int junkIndex = tempName.toLowerCase().indexOf(junk.toLowerCase());
         if (junkIndex != -1)
         {
            tempName = tempName.substring(0, junkIndex);
         }
      }

      // Usuwanie losowych kodów na końcu
      tempName = tempName.replaceAll("-[a-zA-Z0-9]{8,}$", "");

      // 4. Składanie nazwy
      String episodeTitle = Arrays.stream(tempName.trim().split("\\s+"))
            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
            .collect(Collectors.joining("-"));

      return String.format("S%02dE%02d-%s%s", season, episode, episodeTitle, extension);
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