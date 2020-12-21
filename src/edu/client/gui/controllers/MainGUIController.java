package edu.client.gui.controllers;

import edu.client.gui.views.BoardPane;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author honor
 */
public class MainGUIController implements Initializable {

    //<editor-fold defaultstate="collapsed" desc="Element declarations">
    @FXML
    private GridPane hostPane;
    @FXML
    private Label hostNameLabel;
    @FXML
    private Label player1GameTimeLabel;
    @FXML
    private Label player1MoveTimeLabel;
    @FXML
    private GridPane guestPane;
    @FXML
    private Label guestNameLabel;
    @FXML
    private Label turnLabel;
    @FXML
    private GridPane gamePane;
    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private Button createBtn;
    @FXML
    private Button joinBtn;
    @FXML
    private CheckBox gameTimingCheckBox;
    @FXML
    private ComboBox<Integer> gameTimeComboBox;
    @FXML
    private CheckBox moveTimingCheckBox;
    @FXML
    private ComboBox<Integer> moveTimeComboBox;
    @FXML
    private ComboBox<Integer> sizeComboBox;
    @FXML
    private Button confirmBtn;
    @FXML
    private Button discardBtn;
    @FXML
    private Label gameCodeLabel;
    @FXML
    private Button leaveBtn;
    @FXML
    private Button surrenderBtn;
    @FXML
    private Button drawBtn;
    @FXML
    private Label guestGameTimeLabel;
    @FXML
    private Label guestMoveTimeLabel;
    @FXML
    private Button startBtn;
    @FXML
    private BoardPane board;
    //</editor-fold>

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    

    @FXML
    private void gameTimingEnabled(ActionEvent event) {
    }

    @FXML
    private void moveTimingEnabled(ActionEvent event) {
    }
    
}
