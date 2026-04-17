module com.dungeons {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.dungeons to javafx.fxml;
    exports com.dungeons;
}
