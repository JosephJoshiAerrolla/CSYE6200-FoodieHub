	package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PublishedRecipesController {

    @FXML
    private VBox recipeResultBox;

    private int loggedInUserId;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
        System.out.println("Logged in user ID: " + loggedInUserId);
        loadPublishedRecipes(); 
    }

    public void loadPublishedRecipes() {
        recipeResultBox.getChildren().clear();

        VBox allContent = new VBox(30);
        allContent.setStyle("-fx-padding: 20;");

        Label otherUsersLabel = new Label("Recipes Published by Other Users");
        otherUsersLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #D84315;");
        FlowPane otherUserPane = new FlowPane(20, 20);
        otherUserPane.setPrefWrapLength(1000);

        try (Connection conn = database.getConnection()) {
            String query = "SELECT id, title, description, ingredients ,servings, image_path FROM published_recipes WHERE user_id != ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, this.loggedInUserId);
            ResultSet rs = stmt.executeQuery();

            boolean hasOthers = false;

            while (rs.next()) {
                hasOthers = true;
                VBox card = createRecipeCard(rs);
                otherUserPane.getChildren().add(card);
            }

            if (!hasOthers) {
                Label none = new Label("\uD83D\uDE15 No recipes published by other users yet.");
                none.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
                otherUserPane.getChildren().add(none);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        allContent.getChildren().addAll(otherUsersLabel, otherUserPane);
        recipeResultBox.getChildren().add(allContent);
    }

    private VBox createRecipeCard(ResultSet rs) {
        try {
            int recipeId = rs.getInt("id");
            String title = rs.getString("title");
            String desc = rs.getString("description");
            String serving = rs.getString("servings");
            String ingredients = rs.getString("ingredients");
            String imagePath = rs.getString("image_path");

            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #FFFDE7;" +
                    "-fx-border-color: #FF9800;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 10;" +
                    "-fx-pref-width: 250;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

            ImageView imageView = new ImageView();
            imageView.setFitWidth(230);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);

            if (imagePath != null && !imagePath.isBlank()) {
                File imageFile = new File(imagePath);
                imageView.setImage(imageFile.exists()
                        ? new Image(imageFile.toURI().toString())
                        : new Image("https://via.placeholder.com/230x150.png?text=No+Image"));
            } else {
                imageView.setImage(new Image("https://via.placeholder.com/230x150.png?text=No+Image"));
            }

            Label titleLabel = new Label("\uD83C\uDF7D️ " + title);
            titleLabel.setWrapText(true);
            titleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #E65100; -fx-font-weight: bold;");

            List<String> lines = Arrays.stream(ingredients.split("[\\r\\n,]+"))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

            String formatted = lines.stream().map(line -> "• " + line).collect(Collectors.joining("\n"));

            Label ingredientsLabel = new Label("\uD83E\uDED2 Ingredients:\n" + formatted);
            ingredientsLabel.setWrapText(true);
            ingredientsLabel.setStyle("-fx-text-fill: #5D4037; -fx-font-size: 13px;");

            Button viewBtn = new Button("View Details");
            viewBtn.setStyle("-fx-background-color: #29B6F6; -fx-text-fill: white;");
            viewBtn.setOnAction(e -> showRecipeDetails(title, formatted, desc, serving));

            VBox ratingSection = new VBox(5);
            if (!hasUserRated(recipeId, loggedInUserId)) {
                Label rateLabel = new Label("⭐ Rate this recipe:");
                ComboBox<Integer> ratingCombo = new ComboBox<>();
                ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
                ratingCombo.setPromptText("Select");

                Button submitRating = new Button("Submit");
                submitRating.setStyle("-fx-background-color: #FF7043; -fx-text-fill: white;");
                submitRating.setOnAction(e -> {
                    Integer rating = ratingCombo.getValue();
                    if (rating == null) {
                        new Alert(Alert.AlertType.WARNING, "Please select a rating").show();
                        return;
                    }
                    saveRating(recipeId, rating);
                    loadPublishedRecipes();
                });

                ratingSection.getChildren().addAll(rateLabel, ratingCombo, submitRating);
            } else {
                Label ratedMsg = new Label("✅ You've already rated this recipe.");
                ratedMsg.setStyle("-fx-text-fill: green; -fx-font-style: italic;");
                ratingSection.getChildren().add(ratedMsg);
            }

            card.getChildren().addAll(titleLabel, imageView, ingredientsLabel, viewBtn, ratingSection);
            return card;
        } catch (Exception e) {
            e.printStackTrace();
            return new VBox();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRecipeDetails(String title, String ingredients, String desc, String serving) {
        Stage stage = new Stage();
        VBox box = new VBox(20);
        box.setStyle("-fx-padding: 25; -fx-background-color: #FFFDE7;");

        Label titleLabel = new Label("\uD83C\uDF7D️ " + title);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #E65100;");

        Label ingredientsLabel = new Label("\uD83E\uDED2 Ingredients:");
        ingredientsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea ingredientsArea = new TextArea(ingredients);
        ingredientsArea.setWrapText(true);
        ingredientsArea.setEditable(false);
        ingredientsArea.setStyle("-fx-control-inner-background: #FFFDE7; -fx-font-size: 14px;");
        ingredientsArea.setPrefRowCount(5);

        Label descTitle = new Label("\uD83D\uDCDD Description:");
        descTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea descArea = new TextArea(desc);
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setStyle("-fx-control-inner-background: #FFFDE7; -fx-font-size: 14px;");
        descArea.setPrefRowCount(5);

        Label servingLabel = new Label("\uD83D\uDC65 Serves: " + serving);
        servingLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        box.getChildren().addAll(titleLabel, ingredientsLabel, ingredientsArea, descTitle, descArea, servingLabel);

        Scene scene = new Scene(box, 550, 500);
        stage.setTitle("Recipe Details");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void goBack(ActionEvent event) {
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

    @FXML
    private void handleMyRecipes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MyRecipes.fxml"));
            Parent root = loader.load();

            MyRecipesController controller = loader.getController();
            controller.setLoggedInUserId(this.loggedInUserId);
            controller.initUserData();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("\uD83D\uDCDA My Recipes");
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
