package org.example.views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.controllers.LoginController;
import org.example.controllers.SignUpController;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;

public class SignUpView {
    private Label expenseTrackerLabel = new Label("Finvora");
    private TextField nameField = new TextField();
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private PasswordField rePasswordField = new PasswordField();
    private Button registerButton = new Button("Register");
    private Label loginLabel = new Label("Already have an account? Login here");

    public void show(){
        Scene scene = createScene();
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        new SignUpController(this);
        ViewNavigator.switchViews(scene);
    }

    private Scene createScene(){
        javafx.scene.layout.HBox root = new javafx.scene.layout.HBox();
        root.getStyleClass().add("split-root");
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F8FAFC;");

        // --- LEFT SIDE (Light Blue + Image) ---
        VBox leftSide = new VBox();
        leftSide.setStyle("-fx-background-color: #F1F5F9;"); 
        leftSide.setAlignment(Pos.CENTER);
        javafx.scene.layout.HBox.setHgrow(leftSide, javafx.scene.layout.Priority.ALWAYS);
        leftSide.setMaxWidth(Double.MAX_VALUE);
        
        try {
            javafx.scene.image.Image heroImg = new javafx.scene.image.Image(java.util.Objects.requireNonNull(getClass().getResourceAsStream("/images/finvora_register_hero.png")));
            javafx.scene.image.ImageView heroView = new javafx.scene.image.ImageView(heroImg);
            heroView.setPreserveRatio(true);
            heroView.setFitHeight(500);
            leftSide.getChildren().add(heroView);
        } catch (Exception e) {
            System.err.println("Could not load register hero image");
        }

        // --- RIGHT SIDE (Form) ---
        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setStyle("-fx-background-color: #FFFFFF;");
        javafx.scene.layout.HBox.setHgrow(rightSide, javafx.scene.layout.Priority.ALWAYS);
        rightSide.setMaxWidth(Double.MAX_VALUE);
        
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);
        
        try {
            javafx.scene.image.Image logoImg = new javafx.scene.image.Image(java.util.Objects.requireNonNull(getClass().getResourceAsStream("/images/finvora_logo.png")));
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logoImg);
            logoView.setPreserveRatio(true);
            logoView.setFitHeight(80);
            VBox logoBox = new VBox(logoView);
            logoBox.setAlignment(Pos.CENTER);
            logoBox.setPadding(new javafx.geometry.Insets(0, 0, 10, 0));
            formContainer.getChildren().add(logoBox);
        } catch (Exception e) {
            System.err.println("Could not load logo image");
        }
        
        Label subTitle = new Label("CREATE A FINVORA ACCOUNT");
        subTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1A1A1A;");
        
        VBox headerBox = new VBox(subTitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new javafx.geometry.Insets(0, 0, 20, 0));

        String inputStyle = "-fx-background-color: #FFFFFF; -fx-border-color: #1A1A1A; -fx-border-radius: 4px; -fx-padding: 12px; -fx-font-size: 14px;";

        nameField.setStyle(inputStyle);
        nameField.setPromptText("Full Name");

        usernameField.setStyle(inputStyle);
        usernameField.setPromptText("Work Email");

        passwordField.setStyle(inputStyle);
        passwordField.setPromptText("Create Password");

        rePasswordField.setStyle(inputStyle);
        rePasswordField.setPromptText("Confirm Password");

        javafx.scene.control.CheckBox termsBox = new javafx.scene.control.CheckBox("I agree to the Terms of Service and Privacy Policy");
        termsBox.setWrapText(true);
        termsBox.setStyle("-fx-text-fill: #1A1A1A; -fx-font-size: 12px; -fx-padding: 10 0 10 0;");

        registerButton.setText("REGISTER");
        registerButton.setStyle("-fx-background-color: linear-gradient(to right, #032b61, #008080); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 20px; -fx-padding: 12px;");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.HBox loginBox = new javafx.scene.layout.HBox(5);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));
        Label alreadyAcc = new Label("Already have an account?");
        alreadyAcc.setStyle("-fx-text-fill: #1A1A1A;");
        loginLabel.setText("Sign In");
        loginLabel.setStyle("-fx-text-fill: #008080; -fx-font-weight: bold; -fx-cursor: hand;");
        loginBox.getChildren().addAll(alreadyAcc, loginLabel);

        formContainer.getChildren().addAll(headerBox, nameField, usernameField, passwordField, rePasswordField, termsBox, registerButton, loginBox);
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

    public TextField getNameField() {
        return nameField;
    }

    public void setNameField(TextField nameField) {
        this.nameField = nameField;
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

    public PasswordField getRePasswordField() {
        return rePasswordField;
    }

    public void setRePasswordField(PasswordField rePasswordField) {
        this.rePasswordField = rePasswordField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public void setRegisterButton(Button registerButton) {
        this.registerButton = registerButton;
    }

    public Label getLoginLabel() {
        return loginLabel;
    }

    public void setLoginLabel(Label loginLabel) {
        this.loginLabel = loginLabel;
    }
}
