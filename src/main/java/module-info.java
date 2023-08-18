module com.filip.tvscheduler.fmztvscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires static lombok;

    exports com.filipmikolajzeglen.fmzvideoplayer;
    exports com.filipmikolajzeglen.fmzvideoplayer.database;
    exports com.filipmikolajzeglen.fmzvideoplayer.video to com.fasterxml.jackson.databind;
    opens com.filipmikolajzeglen.fmzvideoplayer.video to javafx.fxml;
}