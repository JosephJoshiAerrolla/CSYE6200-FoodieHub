package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.*;

public class ProfileController {

	@FXML
	private Label usernameLabel;
	@FXML
	private Label firstNameLabel;
	@FXML
	private Label lastNameLabel;
	@FXML
	private Label emailLabel;

	private String username;

	private int loggedInUserId;

	private int userId;

	public void setUserId(int id) {
		this.userId = id;
		loadUserData();
	}

	private void loadUserData() {
		String query = "SELECT username, first_name, last_name, email FROM users1 WHERE id = ?";
		try (Connection conn = database.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, userId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				usernameLabel.setText(rs.getString("username"));
				firstNameLabel.setText(rs.getString("first_name"));
				lastNameLabel.setText(rs.getString("last_name"));
				emailLabel.setText(rs.getString("email"));
			} else {
				usernameLabel.setText("⚠️ User not found");
				firstNameLabel.setText("");
				lastNameLabel.setText("");
				emailLabel.setText("");
			}

		} catch (Exception e) {
			e.printStackTrace();
			usernameLabel.setText("❌ Error loading profile.");
		}
	}

	@FXML
	private void goBack(ActionEvent event) {
		try {
			Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			double width = currentStage.getWidth();
			double height = currentStage.getHeight();

			FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
			Parent root = loader.load();

			DashboardController controller = loader.getController();
			controller.setLoggedInUserId(this.userId);

			Scene scene = new Scene(root, width, height);
			currentStage.setScene(scene);
			currentStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}