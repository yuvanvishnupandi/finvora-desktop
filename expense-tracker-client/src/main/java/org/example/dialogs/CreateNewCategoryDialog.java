package org.example.dialogs;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.example.models.User;
import org.example.utils.SqlUtil;
import org.example.utils.ThemeManager;
import org.example.utils.Utilitie;

public class CreateNewCategoryDialog extends CustomDialog {

    private TextField newCategoryTextField;
    private ColorPicker colorPicker;
    private Button createCategoryBtn;

    public CreateNewCategoryDialog(User user) {
        super(user);

        setTitle("Create New Category");

        VBox dialogContentBox = createDialogContentBox();
        getDialogPane().setContent(dialogContentBox);

    }

    private VBox createDialogContentBox() {
        VBox dialogContentBox = new VBox(20);

        newCategoryTextField = new TextField();
        newCategoryTextField.setPromptText("Enter Category Name");
        newCategoryTextField.getStyleClass().addAll("text-size-md", "field-background", "text-light-gray");

        colorPicker = new ColorPicker();
        colorPicker.getStyleClass().add("text-size-md");
        colorPicker.setMaxWidth(Double.MAX_VALUE);

        createCategoryBtn = new Button("Create");
        createCategoryBtn.getStyleClass().addAll("bg-light-blue", "text-size-md", "text-white");
        createCategoryBtn.setMaxWidth(Double.MAX_VALUE);

        createCategoryBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                String categoryName = newCategoryTextField.getText();
                String color = Utilitie.getHexColorValue(colorPicker);

                JsonObject userData = new JsonObject();
                userData.addProperty("id", user.getId());

                JsonObject transactionCategoryData = new JsonObject();
                transactionCategoryData.add("user", userData);
                transactionCategoryData.addProperty("categoryName", categoryName);
                transactionCategoryData.addProperty("categoryColor", color);

                boolean created = SqlUtil.postTransactionCategory(transactionCategoryData);
                if (created) {
                    Utilitie.showAlertDialog(Alert.AlertType.INFORMATION,
                            "Success: Created a Transaction Category");
                } else {
                    Utilitie.showAlertDialog(Alert.AlertType.ERROR,
                            "Error: Failed to create Transaction Category");
                }
            }
        });

        dialogContentBox.getChildren().addAll(newCategoryTextField, colorPicker, createCategoryBtn);
        return dialogContentBox;
    }
}