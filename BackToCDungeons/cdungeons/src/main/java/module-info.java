module com.dungeons {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.xml;
    requires com.google.gson;

    opens com.dungeons.Controllers to javafx.fxml;

    exports com.dungeons to javafx.graphics;
    exports com.dungeons.screens to javafx.graphics, javafx.fxml;
    exports com.dungeons.dialogueManager to com.google.gson;
}