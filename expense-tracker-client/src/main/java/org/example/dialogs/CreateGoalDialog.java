package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.example.models.SavingsGoal;
import org.example.utils.ThemeManager;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGoalDialog extends Dialog<SavingsGoal> {
    public CreateGoalDialog() {
        setTitle("Create New Goal");
        setHeaderText("Enter your savings goal details:");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        getDialogPane().setPrefWidth(400);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField nameField = new TextField();
        nameField.setPromptText("Goal name");

        TextField targetAmountField = new TextField();
        targetAmountField.setPromptText("Target amount");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Deadline");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Target Amount:"), 0, 1);
        grid.add(targetAmountField, 1, 1);
        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(deadlinePicker, 1, 2);

        getDialogPane().setContent(grid);

        setResultConverter(new Callback<ButtonType, SavingsGoal>() {
            @Override
            public SavingsGoal call(ButtonType buttonType) {
                if (buttonType == createButtonType) {
                    try {
                        String name = nameField.getText();
                        BigDecimal target = new BigDecimal(targetAmountField.getText());
                        LocalDate date = deadlinePicker.getValue();

                        return new SavingsGoal(name, target, BigDecimal.ZERO, date);
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, "Invalid inputs!").showAndWait();
                        return null;
                    }
                }
                return null;
            }
        });
    }
}