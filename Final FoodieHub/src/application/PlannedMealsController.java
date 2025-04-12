package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlannedMealsController extends UserControllerBase {

    @FXML private ListView<String> plannedMealsList;

    @Override
    public void setUserId(int id) {
        super.setUserId(id);    
        loadUserData();      
    }

    @Override
    public void loadUserData() {
        loadPlannedMeals();      
    }

    private void loadPlannedMeals() {
        try (Connection conn = database.getConnection()) {
            String query = """
                SELECT f.recipe_title, f.instructions, m.planned_date
                FROM meal_plan m
                JOIN favorites f ON m.favorite_id = f.id
                WHERE m.user_id = ?
                ORDER BY m.planned_date
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, getUserId());  // use inherited getter
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String recipe = rs.getString("recipe_title");
                String date = rs.getString("planned_date");
                String instructions = rs.getString("instructions");

                String cleanedInstructions = (instructions != null) ? instructions
                        .replaceAll("(?i)<li>", "• ")
                        .replaceAll("(?i)</li>", "\n")
                        .replaceAll("(?i)<ol>|</ol>", "")
                        .replaceAll("(?i)<br ?/?>", "\n")
                        .replaceAll("&nbsp;", " ")
                        .trim() : "No description available.";

                String displayText = "🍽️ " + recipe + "  —  📅 " + date + "\n\n📋 Description:\n" + cleanedInstructions;

                plannedMealsList.getItems().add(displayText);
            }

            if (plannedMealsList.getItems().isEmpty()) {
                plannedMealsList.getItems().add("⚠️ No planned meals found.");
            }

            plannedMealsList.setCellFactory(lv -> new ListCell<>() {
                private final Label label = new Label();

                {
                    label.setWrapText(true);
                    label.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
                    label.setMaxWidth(Double.MAX_VALUE);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        label.setText(item);
                        label.setPrefWidth(plannedMealsList.getWidth() - 20);
                        setGraphic(label);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "❌ Failed to load planned meals.").show();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setLoggedInUserId(getUserId()); 

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("FoodieHub");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
