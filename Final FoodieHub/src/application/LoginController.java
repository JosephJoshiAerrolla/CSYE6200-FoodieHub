package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernames;

    @FXML
    private PasswordField passwords;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernames.getText().trim();
        String password = passwords.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("❌ Please enter both username and password.");
            return;
        }

        try (Connection conn = database.getConnection()) {
            String sql = "SELECT * FROM users1 WHERE username = ? AND password = ? AND verification_status = TRUE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
          
                int userId = rs.getInt("id");

             
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
                Parent root = loader.load();

                DashboardController controller = loader.getController();
                controller.setLoggedInUserId(userId); 
                controller.setLoggedInUsername(username); 

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle("FoodieHub");
                stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
                stage.show();

            } else {
                showAlert("❌ Invalid credentials or email not verified.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Database error: " + e.getMessage());
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signUp.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setTitle("FoodieHub");
            currentStage.setScene(new Scene(root, currentStage.getWidth(), currentStage.getHeight()));
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Result");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
