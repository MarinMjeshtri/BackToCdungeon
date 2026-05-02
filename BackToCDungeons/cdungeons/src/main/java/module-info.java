module com.dungeons {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.xml;
    requires com.google.gson;
    requires java.desktop;

    opens com.dungeons.Controllers to javafx.fxml;
    opens com.dungeons.screens to javafx.fxml;
    opens com.dungeons to javafx.graphics, javafx.fxml;
    opens com.dungeons.systems.CombatSystem to javafx.graphics;
    opens com.dungeons.characters to com.google.gson;
    opens com.dungeons.systems.items to com.google.gson;


    exports com.dungeons to javafx.graphics;
    exports com.dungeons.screens to javafx.graphics, javafx.fxml;
    exports com.dungeons.systems.CombatSystem to javafx.graphics; // ← add this
    exports com.dungeons.dialogueManager to com.google.gson;
}