module com.dungeons {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.xml;
    requires com.google.gson;

    opens com.dungeons.Controllers to javafx.fxml;
    opens com.dungeons.screens to javafx.fxml;        // ← add this
    opens com.dungeons to javafx.graphics, javafx.fxml; // ← add this

    exports com.dungeons to javafx.graphics;
    exports com.dungeons.screens to javafx.graphics, javafx.fxml;
    exports com.dungeons.dialogueManager to com.google.gson;
    opens com.dungeons.characters to com.google.gson;
}