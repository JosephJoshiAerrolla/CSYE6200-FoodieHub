package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class AddRecipeController {

	@FXML
	private TextField titleField;
	@FXML
	private TextField ingredientInput;
	@FXML
	private ListView<String> ingredientList;
	@FXML
	private TextArea descriptionArea;
	@FXML
	private TextField servingField;
	@FXML
	private Label imagePathLabel;
	@FXML
	private ImageView previewImage;

	private final ObservableList<String> ingredients = FXCollections.observableArrayList();
	private String selectedImagePath = null;
	private int loggedInUserId;

	public void setLoggedInUserId(int userId) {
		this.loggedInUserId = userId;
		System.out.println("Logged-in user ID: " + userId);
	}

	@FXML
	public void initialize() {
		ingredientList.setItems(ingredients);
	}

	@FXML
	private void handleAddIngredient() {
		String item = ingredientInput.getText().trim();
		if (!item.isEmpty()) {
			ingredients.add(item);
			ingredientInput.clear();
		}
	}

	@FXML
	private void handleUploadImage(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Recipe Image");
		fileChooser.getExtensionFilters()
				.addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			selectedImagePath = file.getAbsolutePath();
			imagePathLabel.setText(file.getName());
			previewImage.setImage(new Image(file.toURI().toString()));
		}
	}

	@FXML
	private void handlePublish(ActionEvent event) {
		String title = titleField.getText().trim();
		String description = descriptionArea.getText().trim();
		String formattedIngredients = ingredients.stream().map(String::trim).filter(s -> !s.isEmpty())
				.collect(Collectors.joining("\n"));
		String servingText = servingField.getText().trim();

		if (title.isEmpty() || description.isEmpty() || formattedIngredients.isEmpty() || servingText.isEmpty()) {
			showAlert("⚠️ Please fill in all fields including ingredients and serving size.");
			return;
		}

		int servings;
		try {
			servings = Integer.parseInt(servingText);
		} catch (NumberFormatException e) {
			showAlert("⚠️ Serving size must be a number.");
			return;
		}

		try (Connection conn = database.getConnection()) {
			String sql = "INSERT INTO published_recipes (user_id, title, description, ingredients, instructions, servings, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, loggedInUserId);
			stmt.setString(2, title);
			stmt.setString(3, description);
			stmt.setString(4, formattedIngredients);
			stmt.setString(5, description); // using description as instructions (you can change if needed)
			stmt.setInt(6, servings);
			stmt.setString(7, selectedImagePath);

			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				showAlert("🎉 Recipe published successfully!");
				clearForm();
			} else {
				showAlert("⚠️ Something went wrong. Recipe not saved.");
			}

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("❌ Failed to save recipe: " + e.getMessage());
		}
	}

	@FXML
	private void handleCancel(ActionEvent event) {
		clearForm();
	}

	@FXML
	private void handleClose(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
			Parent root = loader.load();

			DashboardController controller = loader.getController();
			controller.setLoggedInUserId(this.loggedInUserId);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
			stage.setTitle("FoodieHub");
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearForm() {
		titleField.clear();
		descriptionArea.clear();
		servingField.clear();
		ingredientInput.clear();
		ingredients.clear();
		imagePathLabel.setText("No image selected");
		previewImage.setImage(null);
		selectedImagePath = null;
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
