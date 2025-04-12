package application;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MyRecipesController {
	@FXML private VBox recipeList;
	private int loggedInUserId;

	public void setLoggedInUserId(int userId) {
		this.loggedInUserId = userId;
		System.out.println("Logged setter in user ID: " + loggedInUserId);
	}
	
	public void initUserData() {
	    loadUserRecipes();
	}

	private void loadUserRecipes() {
		System.out.println("Loading user recipes...");
		System.out.println("Logged in user ID: " + loggedInUserId);
	    recipeList.getChildren().clear();
	    try (Connection conn = database.getConnection()) {
	    	String sql = "SELECT id, title, description, servings, ingredients, instructions, image_path FROM published_recipes WHERE user_id = ?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, loggedInUserId); 
	        ResultSet rs = stmt.executeQuery();
	        
	        boolean hasRecipes = false;

	        while (rs.next()) {
	        	hasRecipes = true;
	        	
	        	int recipeId = rs.getInt("id");
	        	String title = rs.getString("title");
	            String description = rs.getString("description");
	            String serving = rs.getString("servings");
	            String ingredients = rs.getString("ingredients");
	            String imagePath = rs.getString("image_path");


	            VBox card = new VBox(5);
	            card.setStyle(
	                "-fx-background-color: #FFFDE7;" +
	                "-fx-border-color: #FF9800;" +
	                "-fx-border-radius: 10;" +
	                "-fx-background-radius: 10;" +
	                "-fx-padding: 10;"
	            );
	            
	            ImageView imageView = new ImageView();
                imageView.setFitWidth(230);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);

                try {
                    if (imagePath != null && !imagePath.isBlank()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            imageView.setImage(new Image(imageFile.toURI().toString()));
                        } else {
                            imageView.setImage(new Image("https://via.placeholder.com/230x150.png?text=No+Image"));
                        }
                    } else {
                        imageView.setImage(new Image("https://via.placeholder.com/230x150.png?text=No+Image"));
                    }
                } catch (Exception e) {
                    imageView.setImage(new Image("https://via.placeholder.com/230x150.png?text=No+Image"));
                }
                

	            Label titleLabel = new Label("🍽️ " + title);
	            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #E65100;");
	            
	            Button deleteBtn = new Button(" Remove");
	            deleteBtn.setStyle(
	                "-fx-background-color: #D32F2F;" +
	                "-fx-text-fill: white;" +
	                "-fx-font-size: 12px;" +
	                "-fx-padding: 4 10 4 10;" +
	                "-fx-background-radius: 6;" +
	                "-fx-cursor: hand;"
	            );
	            deleteBtn.setPrefWidth(90);

	            deleteBtn.setTooltip(new Tooltip("Delete this recipe"));
	            deleteBtn.setOnAction(e -> {
	                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	                alert.setTitle("Confirm Deletion");
	                alert.setHeaderText("Are you sure?");
	                alert.setContentText("This will delete the recipe and all associated ratings and favorites.");

	                alert.showAndWait().ifPresent(response -> {
	                    if (response == ButtonType.OK) {
	                        deleteRecipe(recipeId);
	                        recipeList.getChildren().remove(card); // 🔥 Remove this recipe's UI card immediately
	                    }
	                });
	            });


	            HBox titleBar = new HBox();
	            titleBar.setSpacing(10);
	            titleBar.setStyle("-fx-alignment: CENTER_LEFT; -fx-justify-content: space-between;");
	            Region spacer = new Region();
	            HBox.setHgrow(spacer, Priority.ALWAYS);
	            titleBar.getChildren().addAll(titleLabel, spacer, deleteBtn);


	            Label servingLabel = new Label("👥 Serves: " + serving);
	            servingLabel.setStyle("-fx-font-size: 13px;");

	            Label ingLabel = new Label("🧂 Ingredients:\n" +
	                Arrays.stream(ingredients.split("[\\r\\n]+"))
	                      .map(line -> "• " + line.trim())
	                      .collect(Collectors.joining("\n"))
	            );
	            ingLabel.setWrapText(true);
	            ingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5D4037;");

	            Label descLabel = new Label("📝 " + description);
	            descLabel.setWrapText(true);
	            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");
	         
	            Label ratingLabel = new Label("⭐ Rating: " + String.format("%.1f", getAverageRating(recipeId)) +
	                    " (" + getTotalRatings(recipeId) + " votes)");
	            ratingLabel.setStyle("-fx-text-fill: #FF6F00; -fx-font-weight: bold;");

	            card.getChildren().addAll(titleBar, imageView, ingLabel, descLabel, servingLabel,ratingLabel);
	            recipeList.getChildren().add(card);
	        }
	        
	        if (!hasRecipes) {
	            Label emptyMsg = new Label("😕 You haven't published any recipes yet.");
	            emptyMsg.setStyle("-fx-font-size: 15px; -fx-text-fill: #757575;");
	            recipeList.getChildren().add(emptyMsg);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void deleteRecipe(int recipeId) {
	    try (Connection conn = database.getConnection()) {

	        
	        try (PreparedStatement delRatings = conn.prepareStatement(
	                "DELETE FROM recipe_ratings WHERE recipe_id = ?")) {
	            delRatings.setInt(1, recipeId);
	            delRatings.executeUpdate();
	        }

	        try (PreparedStatement delFavs = conn.prepareStatement(
	                "DELETE FROM favorites WHERE recipe_id = ?")) {
	            delFavs.setInt(1, recipeId);
	            delFavs.executeUpdate();
	        }

	        try (PreparedStatement stmt = conn.prepareStatement(
	                "DELETE FROM published_recipes WHERE id = ?")) {
	            stmt.setInt(1, recipeId);
	            int rows = stmt.executeUpdate();

	            if (rows > 0) {
	                showAlert("🗑️ Recipe deleted successfully.");
	            } else {
	                showAlert("⚠️ Could not delete the recipe.");
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        showAlert("❌ Error occurred during deletion:\n" + e.getMessage());
	    }
	}

	
	private void showAlert(String msg) {
	    Alert alert = new Alert(Alert.AlertType.INFORMATION);
	    alert.setTitle("Info");
	    alert.setHeaderText(null);
	    alert.setContentText(msg);
	    alert.showAndWait();
	}

	private double getAverageRating(int recipeId) {
	    try (Connection conn = database.getConnection()) {
	        String sql = "SELECT AVG(rating) AS avg FROM recipe_ratings WHERE recipe_id = ?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, recipeId);
	        ResultSet rs = stmt.executeQuery();
	        return rs.next() ? rs.getDouble("avg") : 0.0;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0.0;
	    }
	}

	private int getTotalRatings(int recipeId) {
	    try (Connection conn = database.getConnection()) {
	        String sql = "SELECT COUNT(*) AS count FROM recipe_ratings WHERE recipe_id = ?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, recipeId);
	        ResultSet rs = stmt.executeQuery();
	        return rs.next() ? rs.getInt("count") : 0;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}

    
	@FXML
	private void goBack(javafx.event.ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("PublishedRecipes.fxml"));
	        Parent root = loader.load();

	        PublishedRecipesController controller = loader.getController();
	        controller.setLoggedInUserId(this.loggedInUserId);  // 👈 this automatically loads recipes

	        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	        stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
	        stage.setTitle("FoodieHub");
	        stage.show();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


}
