package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private VBox recipeContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryDropdown;
    @FXML private TilePane ingredientPane;
    @FXML private Button generateButton;
    @FXML private BorderPane root;

    private final List<String> selectedIngredients = new ArrayList<>();
    private int loggedInUserId;
    private String loggedInUsername;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }
    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }
    private final Map<String, String> allIngredients = Map.ofEntries(
        Map.entry("Egg", "egg.jpeg"),
        Map.entry("Bacon", "bacon.jpeg"),
        Map.entry("Bread", "bread.jpeg"),
        Map.entry("Spaghetti", "spaghetti.jpeg"),
        Map.entry("Pasta", "pasta.jpeg"),
        Map.entry("Shrimp", "shrimp.jpeg"),
        Map.entry("Mushroom", "mushroom.jpeg"),
        Map.entry("Paneer", "paneer.jpeg"),
        Map.entry("Chicken", "chicken.jpeg"),
        Map.entry("Fish", "fish.jpeg"),
        Map.entry("Rice", "rice.jpeg"),
        Map.entry("Tomato", "tomato.jpeg"),
        Map.entry("Onion", "onion.jpeg"),
        Map.entry("Potato", "potato.jpeg"),
        Map.entry("Corn", "corn.jpeg"),
        Map.entry("Spinach", "spinach.jpeg"),
        Map.entry("Carrot", "carrot.jpeg"),
        Map.entry("Ground Beef", "beef.jpeg"),
        Map.entry("Ground Pork", "pork.jpeg"),
        Map.entry("Avocado", "avacado.jpeg"),
        Map.entry("Tofu", "tofu.jpeg"),
        Map.entry("Cilantro", "cilantro.jpeg"),
        Map.entry("Milk", "milk.jpeg"),
        Map.entry("Yogurt", "yogurt.jpeg"),
        Map.entry("Basil", "basil.jpeg"),
        Map.entry("Cheese", "cheese.jpeg"),
        Map.entry("Bell Pepper", "capsicum.jpeg"),
        Map.entry("Peas", "peas.jpeg"),
        Map.entry("Zucchini", "zucchini.jpeg"),
        Map.entry("Lettuce", "lettuce.jpeg")
    );

    private final Map<String, List<String>> categoryMap = Map.of(
        "All", new ArrayList<>(allIngredients.keySet()),
        "Breakfast", List.of("Egg", "Bread"),
        "Lunch", List.of("Rice", "Chicken", "Paneer", "Fish", "Pasta", "Spinach", "Corn", "Tofu", "Peas"),
        "Dinner", List.of("Pasta", "Onion", "Tomato", "Mushroom", "Rice", "Chicken", "Paneer", "Fish", "Spinach", "Corn", "Tofu", "Peas")
    );

    @FXML
    public void initialize() {
        categoryDropdown.getItems().setAll(categoryMap.keySet());
        categoryDropdown.setValue("All"); // Default selection
        loadIngredients(categoryMap.get("All"));

        categoryDropdown.setOnAction(e -> {
            String selectedCategory = categoryDropdown.getValue();
            List<String> itemsToShow = categoryMap.getOrDefault(selectedCategory, new ArrayList<>());
            loadIngredients(itemsToShow);
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
    }

    private void loadIngredients(List<String> ingredientNames) {
        ingredientPane.getChildren().clear();

        for (String name : ingredientNames) {
            String imageFileName = allIngredients.get(name);
            if (imageFileName == null) {
                System.out.println("⚠️ Skipping missing image for: " + name);
                continue;
            }

            String imagePath = "/assets/" + imageFileName;
            Image img = new Image(getClass().getResourceAsStream(imagePath));
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(60);
            imageView.setFitHeight(60);

            Label label = new Label(name);
            label.setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");

            VBox card = new VBox(10, imageView, label);
            card.setAlignment(Pos.CENTER);
            card.setPrefSize(100, 120);
            card.setStyle("-fx-background-color: #FFF8E1; -fx-border-color: #FB8C00; -fx-border-radius: 10; -fx-background-radius: 10;");

            final boolean[] selected = {false};
            card.setOnMouseClicked(e -> {
                selected[0] = !selected[0];

                if (selected[0]) {
                    selectedIngredients.add(name);
                } else {
                    selectedIngredients.remove(name);
                }

                card.setStyle(selected[0]
                    ? "-fx-background-color: #FFF8E1; -fx-border-color: #43A047; -fx-border-width: 2px; -fx-border-radius: 10; -fx-background-radius: 10;"
                    : "-fx-background-color: #FFF8E1; -fx-border-color: #FB8C00; -fx-border-radius: 10; -fx-background-radius: 10;");
            });

            ingredientPane.getChildren().add(card);
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        String selectedCategory = categoryDropdown.getValue();
        if (selectedCategory == null) selectedCategory = "All";

        List<String> currentCategoryIngredients = categoryMap.getOrDefault(selectedCategory, new ArrayList<>());

        if (query.isEmpty()) {
            loadIngredients(currentCategoryIngredients);
            return;
        }

        List<String> matchingIngredients = currentCategoryIngredients.stream()
                .filter(name -> name.toLowerCase().contains(query))
                .collect(Collectors.toList());

        loadIngredients(matchingIngredients);
    }

    @FXML
    private void handleGenerate() {
        try {
            List<String> selected = new ArrayList<>();
            for (Node node : ingredientPane.getChildren()) {
                if (node instanceof VBox box && box.getStyle().contains("#43A047")) {
                    Label label = (Label) box.getChildren().get(1);
                    selected.add(label.getText());
                }
            }

            if (selected.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one ingredient.");
                alert.show();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("recipeResults.fxml"));
            Parent root = loader.load();

            RecipeResultsController controller = loader.getController();
            controller.setSelectedIngredients(selected);
            controller.setLoggedInUserId(this.loggedInUserId);

            Stage stage = (Stage)((Node)generateButton).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToProfile(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUserId(this.loggedInUserId);

            Scene scene = new Scene(root, width, height);
            currentStage.setScene(scene);
            currentStage.setTitle("FoodieHub");
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("FoodieHub");
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToFavorites(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("favorites.fxml"));
            Parent root = loader.load();

            FavoritesController controller = loader.getController();
            controller.setLoggedInUserId(this.loggedInUserId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("❤️ Your Favorite Recipes");
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToAboutUs(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("aboutUs.fxml"));
            Parent root = loader.load();

            AboutUsController controller = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("About Us");
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPlannedMeals(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("plannedMeals.fxml"));
            Parent root = loader.load();

            PlannedMealsController controller = loader.getController();
            controller.setUserId(this.loggedInUserId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("📅 Your Planned Meals");
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddRecipe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddRecipe.fxml"));
            Parent root = loader.load();

            AddRecipeController controller = loader.getController();
            controller.setLoggedInUserId(this.loggedInUserId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("❤️ Publish Your Recipe");
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PublishedRecipes.fxml"));
            Parent root = loader.load();

            PublishedRecipesController controller = loader.getController();
            controller.setLoggedInUserId(this.loggedInUserId);
            controller.loadPublishedRecipes();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("❤️ Recipes");
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
