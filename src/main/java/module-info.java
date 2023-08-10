module com.filip.tvscheduler.fmztvscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    exports com.filip.tvscheduler.fmztvscheduler;
    opens com.filip.tvscheduler.fmztvscheduler.video to javafx.fxml;
}