package application;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernames;

    @FXML
    private PasswordField passwords;

    @FXML
    protected void handleLogin(ActionEvent event) throws IOException {
        String username = usernames.getText();
        String password = passwords.getText();

        if ("admin".equals(username) && "1234".equals(password)) {
        	switchToDashboard(event);
        } else {
            showAlert("Invalid Credentials.");
        }
    }
    
    
    private void switchToDashboard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Result");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void goToRegister() throws IOException {
        switchToScene(); // Correct path
    }
    
    private void switchToScene() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("signUp.fxml"));
        System.out.println("Switching to signUp.fxml"); // Debug statement
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

