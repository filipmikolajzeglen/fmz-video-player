module com.filip.tvscheduler.fmztvscheduler {
   requires com.fasterxml.jackson.core;
   requires com.fasterxml.jackson.databind;

   requires static lombok;
   requires java.xml;
   requires jcodec;
   requires humble.video.all;
   requires humble.video.noarch;
   requires javafx.fxml;
   requires javafx.graphics;
   requires javafx.controls;
   requires javafx.media;
   requires java.desktop;
   requires uk.co.caprica.vlcj;
   requires uk.co.caprica.vlcj.javafx;

   exports com.filipmikolajzeglen.fmzvideoplayer;
   exports com.filipmikolajzeglen.fmzvideoplayer.database;
   exports com.filipmikolajzeglen.fmzvideoplayer.video;

   opens com.filipmikolajzeglen.fmzvideoplayer.video to javafx.fxml, com.fasterxml.jackson.databind;
   opens com.filipmikolajzeglen.fmzvideoplayer to javafx.fxml, com.fasterxml.jackson.databind;
   exports com.filipmikolajzeglen.fmzvideoplayer.player;
   opens com.filipmikolajzeglen.fmzvideoplayer.player to com.fasterxml.jackson.databind, javafx.fxml;
   exports com.filipmikolajzeglen.fmzvideoplayer.player.view;
   opens com.filipmikolajzeglen.fmzvideoplayer.player.view to com.fasterxml.jackson.databind, javafx.fxml;
   exports com.filipmikolajzeglen.fmzvideoplayer.video.view;
   opens com.filipmikolajzeglen.fmzvideoplayer.video.view to com.fasterxml.jackson.databind, javafx.fxml;
   exports com.filipmikolajzeglen.fmzvideoplayer.video.audio;
   opens com.filipmikolajzeglen.fmzvideoplayer.video.audio to com.fasterxml.jackson.databind, javafx.fxml;
   exports com.filipmikolajzeglen.fmzvideoplayer.video.effect;
   opens com.filipmikolajzeglen.fmzvideoplayer.video.effect to com.fasterxml.jackson.databind, javafx.fxml;
   opens com.filipmikolajzeglen.fmzvideoplayer.demo to javafx.graphics;
}