package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Getter
public class PlayerConsoleLogsView
{
   //@formatter:off
   @FXML private VBox consoleLogContent;
   @FXML private TextArea consoleLogTextArea;
   //@formatter:on

   @FXML
   public void initialize()
   {
      consoleLogContent.getProperties().put("controller", this);

      Logger.addListener(plainMessage -> {
         Platform.runLater(() -> {
            consoleLogTextArea.appendText(plainMessage + "\n");
         });
      });
   }
}
