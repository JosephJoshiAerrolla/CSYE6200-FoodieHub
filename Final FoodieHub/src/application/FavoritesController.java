package application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.scene.control.DateCell;
import java.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FavoritesController {

    @FXML private VBox favoritesContainer;
    private int loggedInUserId;
    private Voice currentVoice;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
        loadFavorites();
    }
    

    private void stopSpeaking() {
        try {
            if (currentVoice != null) {
                Voice toStop = currentVoice;
                currentVoice = null; 

                new Thread(() -> {
                    try {
                        toStop.deallocate();
                      
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                System.out.println(" No voice was active.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "🔇 Error while stopping speech.").show();
        }
    }
    


    private void openMealPlanner(int favoriteId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mealPlanner.fxml"));
            Parent root = loader.load();

            MealPlannerController controller = loader.getController();
            controller.setUserAndFavoriteId(loggedInUserId, favoriteId); 

            Stage stage = new Stage();
            stage.setTitle("📅 Plan a Meal");
            stage.setScene(new Scene(root, 400, 300));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "❌ Failed to open meal planner.").show();
        }
    }




    private void loadFavorites() {
        favoritesContainer.getChildren().clear();

      
        Button exportAllBtn = new Button("📄 Export All");
        exportAllBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        HBox topBar = new HBox(exportAllBtn);
        topBar.setStyle("-fx-alignment: center-right; -fx-padding: 5;");
        favoritesContainer.getChildren().add(topBar);

        exportAllBtn.setOnAction(e -> exportAllToPDF());

        try (Connection conn = database.getConnection()) {
            String query = "SELECT id, recipe_id, recipe_title, instructions, nutrients, servings FROM favorites WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, loggedInUserId);
            ResultSet rs = stmt.executeQuery();

            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                int favoriteId = rs.getInt("id");
                int recipeId = rs.getInt("recipe_id");
                String titleText = rs.getString("recipe_title");
                String instructionText = rs.getString("instructions");
                String nutrientText = rs.getString("nutrients");
                int servingsCount = rs.getInt("servings");

                Label title = new Label("🍽️ " + titleText);
                Label servings = new Label("👥 Servings: " + servingsCount);

                String cleanedInstructions = (instructionText != null) ? instructionText
                        .replaceAll("(?i)<li>", "• ")
                        .replaceAll("(?i)</li>", "\n")
                        .replaceAll("(?i)<ol>|</ol>", "")
                        .replaceAll("(?i)<br ?/?>", "\n")
                        .replaceAll("&nbsp;", " ")
                        .trim() : "No instructions available.";

                TextArea instructions = new TextArea(cleanedInstructions);
                instructions.setEditable(false);
                instructions.setWrapText(true);
                instructions.setPrefHeight(100);

                TextArea nutrients = new TextArea((nutrientText != null) ? nutrientText : "No nutrient info available.");
                nutrients.setEditable(false);
                nutrients.setWrapText(true);
                nutrients.setPrefHeight(100);

                Button speakBtn = new Button("🗣️ Speak");
                speakBtn.setStyle("-fx-background-color: #42A5F5; -fx-text-fill: white;");
                speakBtn.setOnAction(e -> speak(cleanedInstructions));

                Button stopBtn = new Button("🛑 Stop");
                stopBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
                stopBtn.setOnAction(e -> stopSpeaking());

                Button removeBtn = new Button("🗑️ Remove");
                removeBtn.setStyle("-fx-background-color: #E53935; -fx-text-fill: white;");

                Button planMealBtn = new Button("🗓️ Plan");
                planMealBtn.setStyle("-fx-background-color: #66BB6A; -fx-text-fill: white;");
                planMealBtn.setOnAction(e -> openMealPlanner(favoriteId));  

                VBox box = new VBox();
                box.setStyle("-fx-padding: 10; -fx-border-color: #FFB74D; -fx-background-color: #FFF3E0; -fx-border-radius: 10; -fx-background-radius: 10;");

                removeBtn.setOnAction(e -> {
                    try (Connection delConn = database.getConnection()) {
                        // First delete any associated meal plans
                        String deleteMealPlan = "DELETE FROM meal_plan WHERE favorite_id = ?";
                        PreparedStatement mealPlanStmt = delConn.prepareStatement(deleteMealPlan);
                        mealPlanStmt.setInt(1, favoriteId);
                        mealPlanStmt.executeUpdate();

                        // Then delete the favorite
                        String deleteFavorite = "DELETE FROM favorites WHERE id = ?";
                        PreparedStatement delStmt = delConn.prepareStatement(deleteFavorite);
                        delStmt.setInt(1, favoriteId);
                        int rowsDeleted = delStmt.executeUpdate();

                        if (rowsDeleted > 0) {
                            favoritesContainer.getChildren().remove(box);
                            new Alert(Alert.AlertType.INFORMATION, "✅ Recipe removed from favorites.").show();
                        } else {
                            new Alert(Alert.AlertType.WARNING, "⚠️ Recipe not found or already deleted.").show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "❌ Failed to delete from database.").show();
                    }
                });



                HBox actionButtons = new HBox(10, speakBtn, stopBtn, planMealBtn, removeBtn);
                box.getChildren().addAll(title, servings, instructions, nutrients, actionButtons);

                favoritesContainer.getChildren().add(box);
            }

            if (!hasResults) {
                favoritesContainer.getChildren().add(new Label("⚠️ No favorites found."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            favoritesContainer.getChildren().add(new Label("❌ Error loading favorites."));
        }
    }

    private void exportAllToPDF() {
        try {
            FileChooser dirChooser = new FileChooser();
            dirChooser.setTitle("Choose Folder to Save Recipe PDFs");
            dirChooser.setInitialFileName("AllFavorites.pdf");
            dirChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File tempFile = dirChooser.showSaveDialog(null);

            if (tempFile == null) return;
            File outputFolder = tempFile.getParentFile();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLDITALIC);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            com.itextpdf.text.Font separatorFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC);

            try (Connection conn = database.getConnection()) {
                String query = "SELECT recipe_title, instructions, nutrients, servings FROM favorites WHERE user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, loggedInUserId);
                ResultSet rs = stmt.executeQuery();

                boolean foundAny = false;
                String today = java.time.LocalDate.now().toString();

                while (rs.next()) {
                    foundAny = true;

                    String title = rs.getString("recipe_title");
                    String instructions = rs.getString("instructions");
                    String nutrients = rs.getString("nutrients");
                    int servings = rs.getInt("servings");

                    String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
                    File file = new File(outputFolder, cleanTitle + "_" + today + ".pdf");

                    Document doc = new Document();
                    PdfWriter.getInstance(doc, new FileOutputStream(file));
                    doc.open();

                    doc.add(new Paragraph("🍽️ " + title, titleFont));
                    doc.add(new Paragraph("👥 Servings: " + servings + "\n", normalFont));

                    String cleanInstructions = (instructions != null) ? instructions
                            .replaceAll("(?i)<li>", "• ")
                            .replaceAll("(?i)</li>", "\n")
                            .replaceAll("(?i)<ol>|</ol>", "")
                            .replaceAll("(?i)<br ?/?>", "\n")
                            .replaceAll("&nbsp;", " ")
                            .trim() : "No instructions available.";

                    doc.add(new Paragraph("📋 Instructions:", sectionFont));
                    doc.add(new Paragraph(cleanInstructions + "\n", normalFont));

                    doc.add(new Paragraph("🥦 Nutrients:", sectionFont));
                    doc.add(new Paragraph((nutrients != null) ? nutrients : "No nutrient info available.", normalFont));

                    doc.add(new Paragraph("\n──────────────────────────────\n", separatorFont));
                    doc.close();
                }

                if (foundAny) {
                    new Alert(Alert.AlertType.INFORMATION, "✅ Recipes exported individually to:\n" + outputFolder.getAbsolutePath()).show();
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "⚠️ No favorite recipes to export.").show();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "❌ Error exporting PDFs.").show();
        }
    }



    private void speak(String text) {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager vm = VoiceManager.getInstance();
            Voice localVoice = vm.getVoice("kevin16");

            if (localVoice != null) {
                localVoice.allocate();
                currentVoice = localVoice; 

                new Thread(() -> {
                    try {
                        localVoice.speak(text);
                        localVoice.deallocate();

                        // Only clear currentVoice if it’s still the one we set
                        if (currentVoice == localVoice) {
                            currentVoice = null;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();

            } else {
                throw new RuntimeException("Voice not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "🔊 Failed to use text-to-speech.").show();
        }
    }

    
    @FXML
    private void goBack(javafx.event.ActionEvent event) {
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
}
