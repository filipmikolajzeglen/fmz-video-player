module com.filip.tvscheduler.fmztvscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports com.filipmikolajzeglen;
    exports com.filipmikolajzeglen.database;
    exports com.filipmikolajzeglen.video to com.fasterxml.jackson.databind;
    opens com.filipmikolajzeglen.video to javafx.fxml;
}