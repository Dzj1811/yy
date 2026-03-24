package com.yourname.clinic;

import com.yourname.clinic.db.DbInitializer;
import com.yourname.clinic.ui.MainShellView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            DbInitializer.init();
            stage.setTitle("Clinic Manager");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/imagetb.png")));
            stage.setScene(new Scene(new MainShellView(), 1200, 760));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "启动失败：\n" + e).showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}