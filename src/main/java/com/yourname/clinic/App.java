package com.yourname.clinic;

import com.yourname.clinic.db.DbInitializer;
import com.yourname.clinic.ui.MainShellView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        DbInitializer.init();
        stage.setTitle("Clinic Manager");
        stage.setScene(new Scene(new MainShellView(), 1200, 760));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
