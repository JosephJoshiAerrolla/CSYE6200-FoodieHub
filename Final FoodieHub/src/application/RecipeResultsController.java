package application;

import com.google.gson.Gson;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeResultsController {

    @FXML private VBox recipeResultBox;
    private final String API_KEY = "b28f9180867b4b66b2e1c86351dd76ce";
    private List<String> selectedIngredients = new ArrayList<>();

    @FXML
    private Button profileButton;


    public void setSelectedIngredients(List<String> ingredients) {
        this.selectedIngredients = ingredients;
        fetchRecipes();
    }
    private int loggedInUserId;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    private int getLoggedInUserId() {
        return this.loggedInUserId;
    }


    private String loggedInUsername;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }


    private void fetchRecipes() {
        String ingredientParam = String.join(",", selectedIngredients);
        String url = "https://api.spoonacular.com/recipes/findByIngredients?ingredients=" + ingredientParam + "&number=40&apiKey=" + API_KEY;

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            }
        };

        task.setOnSucceeded(e -> {
            try {
                Gson gson = new Gson();
                APIRecipe[] recipes = gson.fromJson(task.getValue(), APIRecipe[].class);
                displayRecipes(recipes);
            } catch (Exception ex) {
                showError("❌ Failed to parse recipes.");
            }
        });

        task.setOnFailed(e -> showError("❌ Failed to fetch recipes."));
        new Thread(task).start();
    }
    
    private void displayRecipes(APIRecipe[] recipes) {
        recipeResultBox.getChildren().clear();

        Label info = new Label("✅ Recipes based on selected ingredients:");
        info.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #388E3C;");
        recipeResultBox.getChildren().add(info);

        FlowPane gridPane = new FlowPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPrefWrapLength(1000); // Adjust based on your scene width
        gridPane.setStyle("-fx-padding: 20;");

        for (APIRecipe recipe : recipes) {
            VBox card = new VBox(10);
            card.setStyle(
                "-fx-background-color: #FFFDE7;" +
                "-fx-border-color: #FF9800;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10;" +
                "-fx-pref-width: 250;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
            
            
            ImageView imageView = new ImageView();
            imageView.setFitWidth(230);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            if (recipe.image != null && !recipe.image.isEmpty()) {
                Image image = new Image(recipe.image, true);
                imageView.setImage(image);
            }
            
           
            Label title = new Label("🍽️ " + recipe.title);
            title.setWrapText(true);
            title.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: #E65100; -fx-font-weight: bold;");
            
           
            String missing = recipe.missedIngredients.stream()
                    .map(i -> i.name)
                    .collect(Collectors.joining(", "));
            String missingText = missing.isEmpty() ? "All ingredients available ✅" : "Missing: " + missing;
            Label status = new Label(missingText);
            status.setWrapText(true);
            status.setStyle("-fx-text-fill: #5D4037; -fx-font-size: 13px;");
            
            
            Button viewBtn = new Button("View Details");
            viewBtn.setStyle("-fx-background-color: #29B6F6; -fx-text-fill: white;");
            viewBtn.setOnAction(e -> fetchDetails(recipe.id));
            
            
            card.getChildren().addAll(imageView, title, status);
            
            
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            card.getChildren().add(spacer);
            
           
            HBox buttonContainer = new HBox(viewBtn);
            buttonContainer.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
            card.getChildren().add(buttonContainer);
            
            gridPane.getChildren().add(card);
        }

      
        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        scrollPane.setPadding(new Insets(0, 0, 0, 0)); // remove scroll padding
        scrollPane.setFitToHeight(true);

        VBox wrapper = new VBox(scrollPane);
        wrapper.setStyle("-fx-padding: 0 0 0 0; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        recipeResultBox.getChildren().add(wrapper);
    }



    private void fetchDetails(int recipeId) {
        String url = "https://api.spoonacular.com/recipes/" + recipeId + "/information?includeNutrition=true&apiKey=" + API_KEY;

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            }
        };

        task.setOnSucceeded(e -> {
            try {
                Gson gson = new Gson();
                RecipeDetails details = gson.fromJson(task.getValue(), RecipeDetails.class);
                
                showDetails(details);
            } catch (Exception ex) {
                showError("❌ Failed to parse recipe details.");
            }
        });

        task.setOnFailed(e -> showError("❌ Failed to fetch recipe details."));
        new Thread(task).start();
    }
    
    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            // ❌ Possibly missing this!
            controller.setLoggedInUserId(this.loggedInUserId);
            controller.setLoggedInUsername(this.loggedInUsername);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showDetails(RecipeDetails details) {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: #FFFDE7;");

        Label title = new Label("🍲 " + details.title);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        int originalServings = Math.max(details.servings, 1);
        Label servingLabel = new Label("Servings: " + originalServings);
        int[] servingCount = {originalServings};

        Button increment = new Button("+");
        Button decrement = new Button("-");
        HBox servingBox = new HBox(10, new Label("👥 Adjust servings:"), decrement, servingLabel, increment);
        servingBox.setStyle("-fx-padding: 10;");
        servingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        String rawInstructions = details.instructions != null ? details.instructions : "No instructions.";
        String cleanedInstructions = rawInstructions
                .replaceAll("(?i)<li>", "• ")
                .replaceAll("(?i)</li>", "\n")
                .replaceAll("(?i)<ol>|</ol>", "")
                .replaceAll("(?i)<br ?/?>", "\n")
                .replaceAll("&nbsp;", " ")
                .trim();

        TextArea instructions = new TextArea(cleanedInstructions);
        instructions.setWrapText(true);
        instructions.setEditable(false);


        TextArea nutrients = new TextArea();
        nutrients.setEditable(false);

        Runnable updateNutrients = () -> {
            StringBuilder nutrientText = new StringBuilder("Nutrients for " + servingCount[0] + " servings:\n");
            double multiplier = (double) servingCount[0] / originalServings;
            if (details.nutrition != null && details.nutrition.nutrients != null) {
                for (RecipeDetails.Nutrient n : details.nutrition.nutrients) {
                    nutrientText.append(n.name)
                            .append(": ")
                            .append(String.format("%.2f", n.amount * multiplier))
                            .append(" ")
                            .append(n.unit)
                            .append("\n");
                }
            }
            nutrients.setText(nutrientText.toString());
        };
        updateNutrients.run();

        increment.setOnAction(e -> {
            servingCount[0]++;
            servingLabel.setText("Servings: " + servingCount[0]);
            updateNutrients.run();
        });

        decrement.setOnAction(e -> {
            if (servingCount[0] > 1) {
                servingCount[0]--;
                servingLabel.setText("Servings: " + servingCount[0]);
                updateNutrients.run();
            }
        });

        Button save = new Button("💾 Save Recipe");
        save.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(details.title.replaceAll(" ", "_") + ".txt");
            File file = chooser.showSaveDialog(null);
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(details.title + "\n\nInstructions:\n" + details.instructions + "\n\n" + nutrients.getText());
                } catch (Exception ex) {
                    showError("❌ Error saving file.");
                }
            }
        });

        Button favorite = new Button("❤️ Add to Favorites");
        favorite.setOnAction(e -> {
            try (Connection conn = database.getConnection()) {
                String sql = "INSERT INTO favorites (user_id, recipe_id, recipe_title, instructions, nutrients, servings) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, this.loggedInUserId);
                stmt.setInt(2, details.id);         
                stmt.setString(3, details.title);
                stmt.setString(4, details.instructions);
                stmt.setString(5, nutrients.getText());
                stmt.setInt(6, servingCount[0]);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Recipe added to favorites!");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("❌ Failed to add to favorites.");
            }
        });

        HBox actionButtons = new HBox(10, save, favorite);
        root.getChildren().addAll(title, servingBox, instructions, nutrients, actionButtons);

        Scene scene = new Scene(root, 520, 520);
        stage.setTitle("Detailed Recipe");
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Favorites");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    private String scaleIngredientQuantities(String instructions, double scale) {
        if (instructions == null) return "No instructions.";

        Map<String, Double> unicodeMap = Map.ofEntries(
            Map.entry("¼", 0.25), Map.entry("½", 0.5), Map.entry("¾", 0.75),
            Map.entry("⅐", 0.142857), Map.entry("⅑", 0.111111), Map.entry("⅒", 0.1),
            Map.entry("⅓", 0.333333), Map.entry("⅔", 0.666667),
            Map.entry("⅕", 0.2), Map.entry("⅖", 0.4), Map.entry("⅗", 0.6), Map.entry("⅘", 0.8),
            Map.entry("⅙", 0.166667), Map.entry("⅚", 0.833333),
            Map.entry("⅛", 0.125), Map.entry("⅜", 0.375), Map.entry("⅝", 0.625), Map.entry("⅞", 0.875)
        );

        String pattern = "(?<![\\w.])((\\d+(\\.\\d+)?)|[¼½¾⅐⅑⅒⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞])\\s*(tsp|tbsp|cups?|grams?|g|ml|liters?|oz|ounces?|kg|pounds?)";

        StringBuffer result = new StringBuffer();
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(instructions);

        while (matcher.find()) {
            String quantityStr = matcher.group(1);
            String unit = matcher.group(4);

            double quantity;
            if (unicodeMap.containsKey(quantityStr)) {
                quantity = unicodeMap.get(quantityStr);
            } else {
                try {
                    quantity = Double.parseDouble(quantityStr);
                } catch (NumberFormatException e) {
                    quantity = 1.0;
                }
            }

            double scaled = Math.round(quantity * scale * 100.0) / 100.0;
            matcher.appendReplacement(result, scaled + " " + unit);
        }
        matcher.appendTail(result);
        return result.toString();
    }
    private void showError(String message) {
        recipeResultBox.getChildren().clear();
        Label error = new Label(message);
        error.setStyle("-fx-text-fill: red;");
        recipeResultBox.getChildren().add(error);
    }

    public static class APIRecipe {
        int id;
        String title;
        List<Ingredient> missedIngredients = new ArrayList<>();
        List<Ingredient> usedIngredients = new ArrayList<>();
        public String image; 
        static class Ingredient {
            String name;
        }
    }

    public static class RecipeDetails {
    	int id; 
        String title;
        String instructions;
        int servings;
        Nutrition nutrition;
        static class Nutrition {
            List<Nutrient> nutrients;
        }
        static class Nutrient {
            String name;
            double amount;
            String unit;
        }
    }
}
