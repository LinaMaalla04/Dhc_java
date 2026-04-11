package tn.dhc.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {
        try {
<<<<<<< HEAD
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
=======
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
>>>>>>> d47e962 (Events CRUD)
            Scene scene = new Scene(loader.load());

            stage.setTitle("Inscription 📝");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}