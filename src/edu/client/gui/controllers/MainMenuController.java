package edu.client.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author honor
 */
public class MainMenuController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private Button hostGameBtn;
    @FXML
    private Button joinGameBtn;
    @FXML
    private Label statusLabel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
