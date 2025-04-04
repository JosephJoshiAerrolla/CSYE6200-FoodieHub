package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class DashboardController {

    @FXML
    private VBox recipeContainer;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> categoryDropdown;

    
    @FXML
    private TilePane ingredientPane;

    @FXML
    private Button generateButton;
    
    
    private final Map<String, String> allIngredients = Map.ofEntries(
    	    Map.entry("Egg", "egg.jpeg"),
    	    Map.entry("Bread", "bread.jpeg"),
    	    Map.entry("Maggie", "maggie.jpeg"),
    	    Map.entry("Pasta", "pasta.jpeg"),
    	    Map.entry("Pancake", "pancake.jpeg"),
    	    Map.entry("Mushroom", "mushroom.jpeg"),
    	    Map.entry("Paneer", "paneer.jpeg"),
    	    Map.entry("Chicken", "chicken.jpeg"),
    	    Map.entry("Fish", "fish.jpeg"),
    	    Map.entry("Rice", "rice.jpeg"),
    	    Map.entry("Tomato", "tomato.jpeg"),
    	    Map.entry("Onion", "onion.jpeg")
    	);

    
    @FXML
    public void initialize() {
        // Show all ingredients on start
        loadIngredients(categoryMap.get("All"));

        // Handle dropdown change
        categoryDropdown.setOnAction(e -> {
            String selectedCategory = categoryDropdown.getValue();
            List<String> itemsToShow = categoryMap.getOrDefault(selectedCategory, new ArrayList<>());
            loadIngredients(itemsToShow);
        });
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

    }

    // Function to populate ingredient cards
    private void loadIngredients(List<String> ingredientNames) {
        ingredientPane.getChildren().clear();

        for (String name : ingredientNames) {
            String imagePath = "/assets/" + allIngredients.get(name);
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

            // Toggle selection styling
            final boolean[] selected = {false};
            card.setOnMouseClicked(e -> {
                selected[0] = !selected[0];
                card.setStyle(selected[0]
                    ? "-fx-background-color: #FFF8E1; -fx-border-color: #43A047; -fx-border-width: 2px; -fx-border-radius: 10; -fx-background-radius: 10;"
                    : "-fx-background-color: #FFF8E1; -fx-border-color: #FB8C00; -fx-border-radius: 10; -fx-background-radius: 10;");
            });

            ingredientPane.getChildren().add(card);
        }
    }
    
    private final Map<String, List<String>> categoryMap = Map.of(
    	    "All", new ArrayList<>(allIngredients.keySet()),
    	    "Breakfast", List.of("Egg", "Bread", "Pancake"),
    	    "Lunch", List.of("Rice", "Chicken", "Paneer", "Fish"),
    	    "Dinner", List.of("Pasta", "Onion", "Tomato", "Mushroom"),
    	    "Snacks", List.of("Maggie", "Bread", "Paneer"),
    	    "Desserts", List.of("Pancake"),
    	    "Drinks", List.of() // add more if needed
    	);



    @FXML
    private void handleSearch() {
    	String query = searchField.getText().toLowerCase();

        // If no category is selected, default to "All"
        String selectedCategory = categoryDropdown.getValue();
        if (selectedCategory == null) {
            selectedCategory = "All";
        }

        // Get the ingredients list for the selected category
        List<String> currentCategoryIngredients = categoryMap.getOrDefault(selectedCategory, new ArrayList<>());

        // If search query is empty, load all ingredients for the category
        if (query.isEmpty()) {
            loadIngredients(currentCategoryIngredients);
            return;
        }

        // Filter the list based on the search query
        List<String> matchingIngredients = currentCategoryIngredients.stream()
                .filter(name -> name.toLowerCase().contains(query))
                .toList();

        loadIngredients(matchingIngredients);
    }

    private void addRecipeCard(String title, String description) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 15; -fx-border-color: #FFB74D; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setMinHeight(Region.USE_PREF_SIZE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font("Arial", 18));
        titleLabel.setTextFill(Color.web("#F57C00"));

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #555;");

        Button viewBtn = new Button("View Recipe");
        viewBtn.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white;");
        viewBtn.setOnAction(e -> {
            // TODO: Show detailed view or open new scene
            System.out.println("Clicked on: " + title);
        });

        card.getChildren().addAll(titleLabel, descLabel, viewBtn);
        recipeContainer.getChildren().add(card);
    }
    
    @FXML
    private void handleGenerate() {
        System.out.println("Generate button clicked.");
        // TODO: Show matching recipes or perform generation logic
    }

}

