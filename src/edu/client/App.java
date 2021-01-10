package edu.client;

import edu.common.packet.LeaveGame;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.WindowEvent;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getClassLoader().getResource("edu/client/gui/views/MainGUI.fxml"));
        scene = new Scene(fxmlLoader.load(), 960, 640);
        stage.setScene(scene);
        stage.setMinHeight(640 + 39);
        stage.setMinWidth(960 + 16);
        stage.getIcons().add(new Image(getClass().getClassLoader().getResource("edu/client/gui/resources/AppIcon.png").toExternalForm()));
        stage.setTitle("Shimoku");
        stage.show();
        stage.setOnCloseRequest((WindowEvent t) -> {
            if (ClientMain.getRoom() != null)
                ClientMain.getClient().sendObject(new LeaveGame());
            ClientMain.getClient().close();
            Platform.exit();
            System.exit(0);
        });
    }
}