package application;

import java.sql.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;

public class NewUserController {

    @FXML private TextField firstNames;
    @FXML private TextField lastNames;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumber;
    @FXML private PasswordField passwords;
    @FXML private TextField verificationCodeField;
    @FXML private Label error_box;

    private String generatedCode;
    private boolean isCodeValid = false;
    private String codeEmail;

    private final String senderEmail = "josephjoshi218@gmail.com";
    private final String senderAppPassword = "xwmp jdju vwom dlyv";

    @FXML
    private void sendVerificationCode() {
        String email = emailField.getText().trim();

        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            error_box.setText("❌ Only Gmail addresses are allowed.");
            error_box.setStyle("-fx-text-fill: red;");
            error_box.setVisible(true);
            return;
        }

   
        if (isEmailExists(email)) {
            error_box.setText("❌ This Gmail address is already registered.");
            error_box.setStyle("-fx-text-fill: red;");
            error_box.setVisible(true);
            return;
        }

        
        generatedCode = String.valueOf(new Random().nextInt(900000) + 100000);
        isCodeValid = true;
        codeEmail = email;

        try {
            sendEmail(email, generatedCode);
            error_box.setText("✅ Code sent to " + email);
            error_box.setStyle("-fx-text-fill: green;");
            error_box.setVisible(true);


            new Timer().schedule(new TimerTask() {
                public void run() {
                    isCodeValid = false;
                }
            }, 60000);

        } catch (Exception e) {
            e.printStackTrace();
            error_box.setText("❌ Email sending failed.");
            error_box.setStyle("-fx-text-fill: red;");
            error_box.setVisible(true);
        }
    }
    private boolean isEmailExists(String email) {
        String query = "SELECT 1 FROM users1 WHERE email = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); 
        } catch (Exception e) {
            e.printStackTrace();
            return true; 
        }
    }


    private void sendEmail(String to, String code) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderAppPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("noreply@foodiehub.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("🔐 Your Verification Code for FoodieHub");

        String html =
            "<!DOCTYPE html>" +
            "<html><head>" +
            "<meta charset='UTF-8'>" +
            "<style>" +
            "  body { background-color: #f2f2f2; font-family: Arial, sans-serif; margin: 0; padding: 0; }" +
            "  .container { max-width: 600px; margin: 30px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
            "  h2 { color: #1a73e8; }" +
            "  .code { font-size: 42px; color: #ff5722; font-weight: bold; margin: 20px 0; }" +
            "  p { font-size: 16px; color: #444; }" +
            "  .footer { font-size: 12px; color: #888; margin-top: 30px; }" +
            "</style>" +
            "</head><body>" +
            "<div class='container'>" +
            "<h2> Welcome to <span style='color:#ff6f00;'>FoodieHub</span>! </h2>" +
            "<p>We're excited to have you join us! Here is your verification code:</p>" +
            "<div class='code'>" + code + "</div>" +
            "<p>This code is valid for <strong>1 minute</strong>. Please enter it in the app to complete your registration.</p>" +
            "<hr>" +
            "<p class='footer'>If you didn't request this code, please ignore this email.</p>" +
            "</div>" +
            "</body></html>";

        message.setContent(html, "text/html; charset=utf-8");
        Transport.send(message);
    }


    @FXML
    private void handleSignup(ActionEvent event) {
        String firstName = firstNames.getText().trim();
        String lastName = lastNames.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneNumber.getText().trim();
        String password = passwords.getText();
        String codeInput = verificationCodeField.getText().trim();

        // Check if any field is empty.
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() ||
            phone.isEmpty() || password.isEmpty() || codeInput.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation", "All fields are required.");
            return;
        }

        // Validate first name: only upper and lower case alphabets allowed.
        if (!firstName.matches("^[a-zA-Z]+$")) {
            showAlert(AlertType.ERROR, "Validation", "First name must contain only letters (A-Z or a-z).");
            return;
        }

        // Validate last name: only upper and lower case alphabets allowed.
        if (!lastName.matches("^[a-zA-Z]+$")) {
            showAlert(AlertType.ERROR, "Validation", "Last name must contain only letters (A-Z or a-z).");
            return;
        }

        // Validate username: only lowercase letters and digits allowed.
        if (!username.matches("^[a-z0-9]+$")) {
            showAlert(AlertType.ERROR, "Validation", "Username must contain only lowercase letters and digits (0-9).");
            return;
        }

        // Validate email: must be a valid gmail address (ending with @gmail.com)
        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            showAlert(AlertType.ERROR, "Validation", "Email must be a valid Gmail address ending with @gmail.com.");
            return;
        }

        // Validate password: length must be between 4 and 10 characters.
        if (!password.matches("^.{4,10}$")) {
            showAlert(AlertType.ERROR, "Validation", "Password length must be between 4 and 10 characters.");
            return;
        }

        // Validate phone: only digits allowed.
        if (!phone.matches("^[0-9]+$")) {
            showAlert(AlertType.ERROR, "Validation", "Phone number must contain only digits (0-9).");
            return;
        }

        // Validate the verification code.
        if (!isCodeValid || !email.equals(codeEmail) || !codeInput.equals(generatedCode)) {
            showAlert(AlertType.ERROR, "Invalid Code", "Code is incorrect or expired.");
            return;
        }

        // Check for duplicate username or email.
        if (isUsernameOrEmailExists(username, email)) {
            showAlert(AlertType.ERROR, "Duplicate Entry", "Username or Email already exists.");
            return;
        }

        // Insert user into the database.
        if (insertUser(firstName, lastName, username, email, password, phone)) {
            showAlert(AlertType.INFORMATION, "Success", "Registration complete! Please log in.");
            switchTo(event, "login.fxml");
        } else {
            showAlert(AlertType.ERROR, "Failed", "User could not be registered.");
        }
    }

    private boolean isUsernameOrEmailExists(String username, String email) {
        String query = "SELECT * FROM users1 WHERE username = ? OR email = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            return stmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean insertUser(String firstName, String lastName, String username, String email,
                               String password, String phone) {
        String query = "INSERT INTO users1 (first_name, last_name, username, email, password, phone, verification_status) " +
                       "VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, username);
            stmt.setString(4, email);
            stmt.setString(5, password);
            stmt.setString(6, phone);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

   
    @FXML
    private void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setTitle("FoodieHub");
            currentStage.setScene(new Scene(root, currentStage.getWidth(), currentStage.getHeight()));
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void switchTo(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setTitle("FoodieHub");
            currentStage.setScene(new Scene(root, currentStage.getWidth(), currentStage.getHeight()));
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
