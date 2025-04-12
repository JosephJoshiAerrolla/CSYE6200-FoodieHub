package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;

public class HomeController {

    @FXML private VBox homeContainer;
    private int loggedInUserId;

    public void setUserId(int userId) {
        this.loggedInUserId = userId;
        loadHomeContent();
    }

    private void loadHomeContent() {
        homeContainer.getChildren().clear();

        addSection("📌 Your Published Recipes", true);
        addSection("🌐 Published by Other Users", false);
    }
    private void addSection(String titleText, boolean isUserRecipes) {
        Label sectionTitle = new Label(titleText);
        sectionTitle.setFont(new Font(18));
        sectionTitle.setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
        VBox sectionBox = new VBox(15);
        sectionBox.getChildren().add(sectionTitle);

        // Divider
        Separator divider = new Separator();
        divider.setPrefWidth(800);
        divider.setStyle("-fx-background-color: #FF6F00; -fx-border-color: #FF6F00;");
        sectionBox.getChildren().add(divider);

        String query = isUserRecipes
            ? "SELECT p.*, u.username FROM published_recipes p JOIN users1 u ON p.user_id = u.id WHERE p.user_id = ?"
            : "SELECT p.*, u.username FROM published_recipes p JOIN users1 u ON p.user_id = u.id WHERE p.user_id != ?";

        try (Connection conn = database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, loggedInUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int recipeId = rs.getInt("id");
                String title = rs.getString("title");
                String desc = rs.getString("description");
                String imagePath = rs.getString("image_path");
                String username = rs.getString("username");
                int servings = rs.getInt("servings");
                String instructions = rs.getString("instructions");

                Image img = new Image(new File("recipe_images/" + imagePath).toURI().toString(), 150, 150, true, true);
                ImageView imageView = new ImageView(img);
                imageView.setFitHeight(150);
                imageView.setFitWidth(150);

                Label titleLabel = new Label("🍽️ " + title);
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                Label descLabel = new Label(desc);
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-text-fill: #444;");
                Label servingsLabel = new Label("👥 Servings: " + servings);

                Label avgRatingLabel = new Label("⭐ " + String.format("%.1f", getAverageRating(recipeId)) + " (" + getTotalRatings(recipeId) + " votes)");
                avgRatingLabel.setStyle("-fx-text-fill: #FF6F00; -fx-font-weight: bold;");

                Label userLabel = !isUserRecipes ? new Label("👤 Published by: " + username) : null;
                if (userLabel != null) userLabel.setStyle("-fx-text-fill: #616161;");

                TextArea instructionsArea = new TextArea(instructions);
                instructionsArea.setWrapText(true);
                instructionsArea.setEditable(false);
                instructionsArea.setPrefHeight(100);
                instructionsArea.setStyle("-fx-control-inner-background: #fffbe6; -fx-border-color: #ddd;");

                VBox rightBox = new VBox(6, titleLabel, descLabel, servingsLabel, avgRatingLabel);
                if (userLabel != null) rightBox.getChildren().add(userLabel);
                rightBox.getChildren().add(instructionsArea);

                
                if (isUserRecipes) {
                	Button removeBtn = new Button("❌ Remove");
                	removeBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 4 10 4 10; -fx-background-radius: 6;");
                	removeBtn.setPrefWidth(90);

                    removeBtn.setOnAction(e -> {
                        boolean confirm = confirmDeletion(title);
                        if (confirm) {
                            removePublishedRecipe(recipeId);
                            loadHomeContent();
                        }
                    });
                    rightBox.getChildren().add(removeBtn);
                } else {
                    if (!hasUserRated(recipeId, loggedInUserId)) {
                        Label rateLabel = new Label("⭐ Rate this recipe:");
                        ComboBox<Integer> ratingCombo = new ComboBox<>();
                        ratingCombo.getItems().addAll(1, 2, 3, 4, 5);

                        Button submitBtn = new Button("Submit");
                        submitBtn.setStyle("-fx-background-color: #FB8C00; -fx-text-fill: white;");
                        HBox ratingBox = new HBox(10, rateLabel, ratingCombo, submitBtn);

                        submitBtn.setOnAction(e -> {
                            Integer rating = ratingCombo.getValue();
                            if (rating == null) {
                                new Alert(Alert.AlertType.WARNING, "⚠️ Select a rating").show();
                                return;
                            }
                            saveRating(recipeId, rating);
                            loadHomeContent();
                        });

                        rightBox.getChildren().add(ratingBox);
                    } else {
                        Label alreadyRated = new Label("✅ You've rated this recipe");
                        alreadyRated.setStyle("-fx-text-fill: green; -fx-font-style: italic;");
                        rightBox.getChildren().add(alreadyRated);
                    }
                }

                HBox recipeCard = new HBox(20, imageView, rightBox);
                recipeCard.setStyle("-fx-background-color: #FFECB3; -fx-padding: 15; -fx-border-color: #FFB74D; -fx-border-radius: 10; -fx-background-radius: 10;");
                recipeCard.setPrefWidth(800);

                sectionBox.getChildren().add(recipeCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        homeContainer.getChildren().add(sectionBox);
    }

    private boolean confirmDeletion(String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Remove: " + title);
        alert.setContentText("This will remove the recipe.\nDo you want to proceed?");
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void removePublishedRecipe(int recipeId) {
        try (Connection conn = database.getConnection()) {
            PreparedStatement delMeals = conn.prepareStatement("DELETE FROM meal_plan WHERE favorite_id IN (SELECT id FROM favorites WHERE recipe_id = ?)");
            delMeals.setInt(1, recipeId);
            delMeals.executeUpdate();

            PreparedStatement delFavs = conn.prepareStatement("DELETE FROM favorites WHERE recipe_id = ?");
            delFavs.setInt(1, recipeId);
            delFavs.executeUpdate();

            PreparedStatement delRatings = conn.prepareStatement("DELETE FROM recipe_ratings WHERE recipe_id = ?");
            delRatings.setInt(1, recipeId);
            delRatings.executeUpdate();

            PreparedStatement delPub = conn.prepareStatement("DELETE FROM published_recipes WHERE id = ?");
            delPub.setInt(1, recipeId);
            delPub.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "✅ Recipe removed successfully!").show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "❌ Failed to remove recipe!").show();
        }
    }

private boolean hasUserRated(int recipeId, int userId) {
    try (Connection conn = database.getConnection()) {
        String sql = "SELECT 1 FROM recipe_ratings WHERE recipe_id = ? AND rated_by = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, recipeId);
        stmt.setInt(2, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next(); 
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
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
    private void goBackToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("FoodieHub");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveRating(int recipeId, int rating) {
        try (Connection conn = database.getConnection()) {
            String check = "SELECT * FROM recipe_ratings WHERE recipe_id = ? AND rated_by = ?";
            PreparedStatement checkStmt = conn.prepareStatement(check);
            checkStmt.setInt(1, recipeId);
            checkStmt.setInt(2, loggedInUserId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String update = "UPDATE recipe_ratings SET rating = ? WHERE recipe_id = ? AND rated_by = ?";
                PreparedStatement updateStmt = conn.prepareStatement(update);
                updateStmt.setInt(1, rating);
                updateStmt.setInt(2, recipeId);
                updateStmt.setInt(3, loggedInUserId);
                updateStmt.executeUpdate();
            } else {
                String insert = "INSERT INTO recipe_ratings (recipe_id, rated_by, rating) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insert);
                insertStmt.setInt(1, recipeId);
                insertStmt.setInt(2, loggedInUserId);
                insertStmt.setInt(3, rating);
                insertStmt.executeUpdate();
            }

            new Alert(Alert.AlertType.INFORMATION, "✅ Rating submitted.").show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "❌ Failed to submit rating.").show();
        }
    }
}
