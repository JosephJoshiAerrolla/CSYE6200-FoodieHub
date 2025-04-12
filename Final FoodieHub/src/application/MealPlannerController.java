package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class MealPlannerController extends AbstractPlannerController {

    @FXML
    private DatePicker datePicker;

    private int favoriteId;

    public void setUserAndFavoriteId(int userId, int favoriteId) {
        setUserId(userId);  
        this.favoriteId = favoriteId;
    }

    @FXML
    private void initialize() {
       
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #999;");
                }
            }
        });

        datePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSavePlan() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            new Alert(Alert.AlertType.WARNING, "⚠️ Please select a date.").show();
            return;
        }

        try (Connection conn = database.getConnection()) {

           
            PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM users1 WHERE id = ?");
            checkUser.setInt(1, getUserId());
            ResultSet rs = checkUser.executeQuery();
            if (!rs.next()) {
                new Alert(Alert.AlertType.ERROR, "❌ Invalid user ID. Cannot plan a meal.").show();
                return;
            }

            // Insert into meal_plan
            String sql = "INSERT INTO meal_plan (user_id, favorite_id, planned_date) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, getUserId());  // Use from abstract class
            stmt.setInt(2, favoriteId);
            stmt.setDate(3, java.sql.Date.valueOf(date));
            stmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "✅ Meal planned successfully!").show();
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "❌ Failed to save meal plan.").show();
        }
    }

    @Override
    public void saveToDatabase() {
        handleSavePlan(); 
    }
}
