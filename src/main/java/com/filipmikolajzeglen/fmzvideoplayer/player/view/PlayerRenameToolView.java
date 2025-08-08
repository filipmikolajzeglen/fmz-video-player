package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
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

   private void generateNewNames()
   {
      String[] textsToIgnore = Arrays.stream(ignoreTextField.getText().split(";"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);

      String seasonText = seasonTextField.getText();

      if (!seasonText.isEmpty())
      {
         try
         {
            Integer.parseInt(seasonText);
         }
         catch (NumberFormatException e)
         {
            for (RenameFileItem item : fileItems)
            {
               item.setNewName("");
            }
            return;
         }
      }

      fileItems.sort(Comparator.comparing(RenameFileItem::getOriginalName));

      // Wzorce do rozpoznawania sezonu i odcinka
      Pattern[] patterns = new Pattern[] {
            Pattern.compile("\\[(\\d+)x(\\d+)\\]\\s*(.*)", Pattern.CASE_INSENSITIVE), // [1x01] Tytuł
            Pattern.compile("S(\\d{2})\\s*E(\\d{2})\\s*-\\s*(.*)", Pattern.CASE_INSENSITIVE), // S01 E01 - Tytuł
            Pattern.compile("(\\d+)x(\\d+)\\s*-\\s*(.*)", Pattern.CASE_INSENSITIVE), // 1x01 - Tytuł
            Pattern.compile("S(\\d{2})E(\\d{2})[.\\s-]*(.*)", Pattern.CASE_INSENSITIVE), // S01E11. lub S01E11 - Tytuł
            Pattern.compile("S(\\d{2})\\s*Odcinek\\s*(\\d{1,2})\\s*-\\s*(.*)", Pattern.CASE_INSENSITIVE),
            // S01 Odcinek 12 - Tytuł
            Pattern.compile("E(\\d{3})[.\\s-]*(.*)", Pattern.CASE_INSENSITIVE), // E001. Tytuł
            Pattern.compile("Saban's Masked Rider - Episode (\\d+)", Pattern.CASE_INSENSITIVE),
            // Saban's Masked Rider - Episode 28
            Pattern.compile("S(\\d{2})E(\\d{2})", Pattern.CASE_INSENSITIVE), // S01E11
      };

      for (RenameFileItem item : fileItems)
      {
         String originalName = item.getOriginalName();
         String baseName =
               originalName.contains(".") ? originalName.substring(0, originalName.lastIndexOf('.')) : originalName;
         String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";

         int season = -1, episode = -1;
         String title = "";

         boolean matched = false;
         for (Pattern pattern : patterns)
         {
            Matcher matcher = pattern.matcher(baseName);
            if (matcher.find())
            {
               matched = true;
               if (pattern.pattern().contains("Saban's Masked Rider"))
               {
                  season = 1;
                  episode = Integer.parseInt(matcher.group(1));
               }
               else if (pattern.pattern().contains("E(\\d{3})"))
               {
                  int ep = Integer.parseInt(matcher.group(1));
                  season = ep / 100;
                  episode = ep % 100;
                  title = matcher.group(2);
               }
               else if (pattern.pattern().contains("S(\\d{2})\\s*Odcinek"))
               {
                  season = Integer.parseInt(matcher.group(1));
                  episode = Integer.parseInt(matcher.group(2));
                  title = matcher.group(3);
               }
               else if (matcher.groupCount() >= 3)
               {
                  season = Integer.parseInt(matcher.group(1));
                  episode = Integer.parseInt(matcher.group(2));
                  title = matcher.group(3);
               }
               else if (matcher.groupCount() == 2)
               {
                  season = Integer.parseInt(matcher.group(1));
                  episode = Integer.parseInt(matcher.group(2));
               }
               break;
            }
         }

         // Jeśli podano sezon ręcznie, nadpisz
         if (!seasonText.isEmpty())
         {
            season = Integer.parseInt(seasonText);
         }

         if (season == -1 || episode == -1)
         {
            item.setNewName("Cannot process");
            continue;
         }

         // Usuwanie niechcianych fragmentów z tytułu
         for (String textToIgnore : textsToIgnore)
         {
            title = title.replaceAll("(?i)" + Pattern.quote(textToIgnore), "").trim();
         }
         // Usuwanie typowych tagów jakościowych
         title = title.replaceAll("\\(.*?\\)", "")
               .replaceAll("(1080p|720p|BluRay|MULTi|AI4K|x265|DVDSync|AAC|H264|BTV-Bizanc|ROOT21)", "").trim();
         // Usuwanie kropek na spacje, potem zamiana spacji na myślniki
         title = title.replaceAll("[._]", " ").replaceAll("\\s+", " ").trim().replaceAll(" ", "-");

         // Usuwanie końcowych myślników
         title = title.replaceAll("^-+|-+$", "");

         String newName = String.format("S%02dE%02d", season, episode);
         if (!title.isEmpty())
         {
            newName += "-" + title;
         }
         newName += extension;

         item.setNewName(newName);
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