package org.example.views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.controllers.LoginController;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;

public class LoginView {
    private Label expenseTrackerLabel = new Label("Finance Tracker");
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Button loginButton = new Button("Login");
    private Label signupLabel = new Label("Don't have an account? Click Here");

    public void show(){
        Scene scene = createScene();
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        new LoginController(this);
        ViewNavigator.switchViews(scene);
    }

    private Scene createScene(){
        javafx.scene.layout.HBox root = new javafx.scene.layout.HBox();
        root.getStyleClass().add("split-root");
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFFFF;");

        // --- LEFT SIDE (Dark Blue + Image) ---
        VBox leftSide = new VBox(20);
        leftSide.setStyle("-fx-background-color: #032b61;"); // Dark blue matching screenshot
        leftSide.setAlignment(Pos.CENTER);
        javafx.scene.layout.HBox.setHgrow(leftSide, javafx.scene.layout.Priority.ALWAYS);
        leftSide.setMaxWidth(Double.MAX_VALUE);
        
        try {
            javafx.scene.image.Image logoImg = new javafx.scene.image.Image(java.util.Objects.requireNonNull(getClass().getResourceAsStream("/images/finvora_logo.png")));
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logoImg);
            logoView.setPreserveRatio(true);
            logoView.setFitHeight(40);
            
            Label logoText = new Label("FINVORA");
            logoText.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: white; -fx-letter-spacing: 2px;");
            
            javafx.scene.layout.HBox logoBox = new javafx.scene.layout.HBox(15, logoView, logoText);
            logoBox.setAlignment(Pos.CENTER_LEFT);
            logoBox.setPadding(new javafx.geometry.Insets(30, 0, 40, 50));
            leftSide.getChildren().add(logoBox);
        } catch (Exception e) {
            System.err.println("Could not load logo image");
        }

        try {
            javafx.scene.image.Image heroImg = new javafx.scene.image.Image(java.util.Objects.requireNonNull(getClass().getResourceAsStream("/images/finvora_login_hero.png")));
            javafx.scene.image.ImageView heroView = new javafx.scene.image.ImageView(heroImg);
            heroView.setPreserveRatio(true);
            heroView.setFitHeight(350);
            leftSide.getChildren().add(heroView);
        } catch (Exception e) {
            System.err.println("Could not load login hero image");
        }
        
        Label quote = new Label("Get All Your Finances\nAt One Place.");
        quote.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-text-alignment: center;");
        quote.setPadding(new javafx.geometry.Insets(40, 0, 0, 0));
        leftSide.getChildren().add(quote);

        // --- RIGHT SIDE (Form) ---
        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setStyle("-fx-background-color: #FFFFFF;");
        javafx.scene.layout.HBox.setHgrow(rightSide, javafx.scene.layout.Priority.ALWAYS);
        rightSide.setMaxWidth(Double.MAX_VALUE);
        
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(400); 
        
        Label loginTitle = new Label("Login to your account");
        loginTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1A1A1A;");
        
        VBox headerBox = new VBox(10, loginTitle);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));

        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1A1A1A; -fx-font-weight: bold;");
        usernameField.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D1D5DB; -fx-border-radius: 4px; -fx-padding: 10px; -fx-font-size: 14px;");
        usernameField.setPromptText("Email Address");
        VBox emailBox = new VBox(5, emailLabel, usernameField);

        javafx.scene.layout.HBox passHeader = new javafx.scene.layout.HBox();
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1A1A1A; -fx-font-weight: bold;");
        Label forgotLabel = new Label("forgot password?");
        forgotLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #032b61; -fx-cursor: hand;");
        javafx.scene.layout.Region passSpacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(passSpacer, javafx.scene.layout.Priority.ALWAYS);
        passHeader.getChildren().addAll(passLabel, passSpacer, forgotLabel);

        passwordField.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D1D5DB; -fx-border-radius: 4px; -fx-padding: 10px; -fx-font-size: 14px;");
        passwordField.setPromptText("Enter password");
        VBox passBox = new VBox(5, passHeader, passwordField);

        javafx.scene.control.CheckBox rememberMe = new javafx.scene.control.CheckBox("Remember Me");
        rememberMe.setStyle("-fx-text-fill: #1A1A1A; -fx-padding: 10 0 20 0;");

        loginButton.setText("Sign In");
        loginButton.setStyle("-fx-background-color: #032b61; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 4px; -fx-padding: 12px;");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.HBox signupBox = new javafx.scene.layout.HBox(5);
        signupBox.setAlignment(Pos.CENTER);
        signupBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));
        Label noAcc = new Label("Don't have an account?");
        noAcc.setStyle("-fx-text-fill: #1A1A1A;");
        signupLabel.setText("Sign Up");
        signupLabel.setStyle("-fx-text-fill: #032b61; -fx-font-weight: bold; -fx-cursor: hand;");
        signupBox.getChildren().addAll(noAcc, signupLabel);

        formContainer.getChildren().addAll(headerBox, emailBox, passBox, rememberMe, loginButton, signupBox);
        rightSide.getChildren().add(formContainer);

        root.getChildren().addAll(leftSide, rightSide);
        return new Scene(root, Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);
    }
    public Label getExpenseTrackerLabel() {
        return expenseTrackerLabel;
    }

    public void setExpenseTrackerLabel(Label expenseTrackerLabel) {
        this.expenseTrackerLabel = expenseTrackerLabel;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(TextField usernameField) {
        this.usernameField = usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(PasswordField passwordField) {
        this.passwordField = passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public void setLoginButton(Button loginButton) {
        this.loginButton = loginButton;
    }

    public Label getSignupLabel() {
        return signupLabel;
    }

    public void setSignupLabel(Label signupLabel) {
        this.signupLabel = signupLabel;
    }
}
