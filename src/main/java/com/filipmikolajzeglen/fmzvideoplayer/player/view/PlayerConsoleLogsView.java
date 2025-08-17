package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.util.function.Consumer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@Slf4j
@Getter
public class PlayerConsoleLogsView
{
   //@formatter:off
   @FXML private VBox consoleLogContent;
   @FXML private TextArea consoleLogTextArea;
   //@formatter:on

   @FXML
   public void initialize() {
      GuiLogAppender.setLogListener(plainMessage -> {
         Platform.runLater(() -> {
            consoleLogTextArea.appendText(plainMessage + "\n");
         });
      });

      Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
      GuiLogAppender guiAppender = new GuiLogAppender();
      guiAppender.setContext(rootLogger.getLoggerContext());
      guiAppender.start();
      rootLogger.addAppender(guiAppender);
   }

   static class GuiLogAppender extends AppenderBase<ILoggingEvent>
   {
      @Setter
      private static Consumer<String> logListener;

      @Override
      protected void append(ILoggingEvent eventObject) {
         if (logListener != null) {
            String formatted = String.format(
                  "%s %s : %s",
                  eventObject.getLevel(),
                  eventObject.getLoggerName(),
                  eventObject.getFormattedMessage()
            );
            logListener.accept(formatted);
         }
      }
   }
}
