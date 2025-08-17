package com.filipmikolajzeglen.fmzvideoplayer.player.view;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
   @FXML private TextFlow consoleLogTextFlow;
   //@formatter:on

   @FXML
   public void initialize()
   {
      consoleLogTextFlow.setLineSpacing(8);
      consoleLogTextFlow.setBackground(Background.fill(Paint.valueOf("#ffffff")));
      consoleLogTextFlow.setStyle("-fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
      consoleLogTextFlow.prefWidthProperty().bind(consoleLogContent.widthProperty());
      consoleLogTextFlow.prefHeightProperty().bind(consoleLogContent.heightProperty());
      GuiLogAppender.setLogListener((formattedDate, level, message) -> {
         Platform.runLater(() -> {
            String color = switch (level)
            {
               case "INFO" -> "#1976d2";
               case "WARN" -> "#fbc02d";
               case "ERROR" -> "#d32f2f";
               default -> "#333333";
            };
            Text datePart = new Text("[" + formattedDate + "] ");
            datePart.setStyle("-fx-font-weight: bold;");
            Text levelPart = new Text(level);
            levelPart.setStyle("-fx-font-weight: bold; -fx-fill: " + color + ";");
            Text messagePart = new Text(" : " + message + "\n");
            consoleLogTextFlow.getChildren().addAll(datePart, levelPart, messagePart);
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
      private static TriConsumer<String, String, String> logListener;

      @Override
      protected void append(ILoggingEvent eventObject)
      {
         if (logListener != null)
         {
            Instant instant = eventObject.getInstant();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                  .withZone(ZoneId.systemDefault());
            String formattedDate = formatter.format(instant);
            logListener.accept(formattedDate, eventObject.getLevel().toString(), eventObject.getFormattedMessage());
         }
      }
   }

   @FunctionalInterface
   interface TriConsumer<A, B, C>
   {
      void accept(A a, B b, C c);
   }
}