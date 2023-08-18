module com.filip.tvscheduler.fmztvscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports com.filip.tvscheduler.fmztvscheduler;
    exports com.filip.tvscheduler.fmztvscheduler.fmzdatabase;
    exports com.filip.tvscheduler.fmztvscheduler.video to com.fasterxml.jackson.databind;
    opens com.filip.tvscheduler.fmztvscheduler.video to javafx.fxml;
}