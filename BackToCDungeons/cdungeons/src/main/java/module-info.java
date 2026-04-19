module com.dungeons {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.xml;        // ← add this line

    exports com.dungeons to javafx.graphics;
    exports com.dungeons.screens to javafx.graphics, javafx.fxml;
}