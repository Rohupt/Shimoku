package edu.client.gui.controllers;
/**
 * Created by Doston Hamrakulov
 */
import edu.client.EventListener;
import edu.client.events.GameEventAdapter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Controller for the top pane of the GUI.
 */
public class TopPaneController extends Controller {

    @FXML
    public Button playButton;
    @FXML
    public Button stopButton;
    @FXML
    public Button settingsButton;

    @Override
    public void initialise(EventListener listener) {
        this.setListener(listener);
        this.getListener().addListener(new GameEventAdapter() {
            @Override
            public void gameFinished() {
                playButton.setVisible(true);
                stopButton.setVisible(false);
                settingsButton.setDisable(false);
            }
            @Override
            public void gameStarted() {
                playButton.setVisible(false);
                stopButton.setVisible(true);
                settingsButton.setDisable(true);
            }
            @Override
            public void gameResumed() {
                playButton.setVisible(false);
                stopButton.setVisible(true);
                settingsButton.setDisable(true);
            }
        });
    }

    public void newGame() {
//        game.start();
    }

    public void stopGame() {
//        game.stop();
    }

    public void undo() {
//        game.undo();
    }

    public void openSettings() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("gui/views/SettingsPane.fxml"));
        Pane settingsPane = loader.load();
        Controller controller = loader.getController();
//        controller.initialise();
        Stage stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(new Scene(settingsPane));
        stage.getIcons().add(new Image(getClass().getClassLoader()
                .getResource("AppIcon.png").toExternalForm()));
        stage.setResizable(false);
        stage.show();
    }
}
