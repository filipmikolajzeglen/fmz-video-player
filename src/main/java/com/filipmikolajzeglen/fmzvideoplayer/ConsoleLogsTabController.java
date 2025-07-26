package com.filipmikolajzeglen.fmzvideoplayer;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Getter
public class ConsoleLogsTabController
{
    @FXML
    private VBox consoleLogContent;
    @FXML
    private TextArea consoleLogTextArea;

    @FXML
    public void initialize()
    {
        // Dodaj referencję do kontrolera w properties VBox
        consoleLogContent.getProperties().put("controller", this);

        Logger.addListener(plainMessage -> {
            Platform.runLater(() -> {
                consoleLogTextArea.appendText(plainMessage + "\n");
            });
        });
    }
}
