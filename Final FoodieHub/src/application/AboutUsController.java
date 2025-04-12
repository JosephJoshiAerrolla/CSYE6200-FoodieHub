package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;

public class AboutUsController {

    @FXML private ImageView dev1Image;
    @FXML private ImageView dev2Image;
    @FXML private ImageView dev3Image;

    @FXML
    public void initialize() {
    	String imagePath1 = "/assets/"+"aruna.jpeg" ;
    	String imagePath2 = "/assets/"+"harsha.jpeg" ;
    	String imagePath3 = "/assets/"+"joseph.jpeg" ;
        dev1Image.setImage(new Image(imagePath1)); 
        dev2Image.setImage(new Image(imagePath2));
        dev3Image.setImage(new Image(imagePath3));
    }

    @FXML
    private void handleBack(ActionEvent event) {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
			Parent root = loader.load();

//			DashboardController controller = loader.getController();
//			controller.setLoggedInUserId(this.loggedInUserId);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
			stage.setTitle("FoodieHub");
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

    }
}