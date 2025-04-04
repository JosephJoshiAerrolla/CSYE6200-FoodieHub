package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class NewUserController {
    @FXML
    private TextField firstNames;

    @FXML
    private TextField lastNames;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneNumber;

    @FXML
    private PasswordField passwords;

    @FXML
    private PasswordField confirmPasswords;
    
    @FXML
    private void handleSignup() {
        String firstName = firstNames.getText();
        String lastName = lastNames.getText();
        String email = emailField.getText();
        String phone = phoneNumber.getText();
        String password = passwords.getText();
        String confirmPassword = confirmPasswords.getText();

        // Validate mandatory fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "All fields are mandatory!");
            return;
        }

        // Validate phone number
        if (!phone.matches("\\d{10}")) {
            showAlert(AlertType.ERROR, "Validation Error", "Phone number must be exactly 10 digits.");
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Validation Error", "Entered password do not match.");
            return;
        }

        // Generate OTP
     
    }
    
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
