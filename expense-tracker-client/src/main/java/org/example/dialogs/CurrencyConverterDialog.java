package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.example.utils.CurrencyRates;
import org.example.utils.ThemeManager;

public class CurrencyConverterDialog extends Dialog<Void> {

    private final ComboBox<String> fromBox = new ComboBox<>();
    private final ComboBox<String> toBox = new ComboBox<>();
    private final TextField amountField = new TextField();
    private final Label resultLabel = new Label("= 0");
    private final Button convertBtn = new Button("Convert");
    private final Button swapBtn = new Button("⇄");
    private final Button copyBtn = new Button("Copy");

    public CurrencyConverterDialog() {
        setTitle("Currency Converter");
        setHeaderText("Convert between world currencies");

        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(close);

        List<String> codes = Arrays.asList(
                "USD", "INR", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "CNY", "AED", "CHF",
                "ZAR", "NZD", "SEK", "NOK", "DKK", "HKD", "THB", "MYR", "KRW", "IDR", "PHP", "BRL", "RUB"
        );

        fromBox.getItems().addAll(codes);
        toBox.getItems().addAll(codes);
        fromBox.setValue("USD");
        toBox.setValue("INR");

        amountField.setPromptText("Amount");
        amountField.setText("1");

        resultLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");
        convertBtn.setDefaultButton(true);

        swapBtn.setOnAction(e -> {
            String temp = fromBox.getValue();
            fromBox.setValue(toBox.getValue());
            toBox.setValue(temp);
        });

        convertBtn.setOnAction(e -> doConvert());
        copyBtn.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(resultLabel.getText().replace("= ", ""));
            Clipboard.getSystemClipboard().setContent(cc);
        });

        HBox actions = new HBox(8, convertBtn, swapBtn, copyBtn);
        actions.setPadding(new Insets(4, 0, 0, 0));

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(14));

        gp.add(new Label("From:"),     0, 0); gp.add(fromBox,     1, 0);
        gp.add(new Label("To:"),       0, 1); gp.add(toBox,       1, 1);
        gp.add(new Label("Amount:"),   0, 2); gp.add(amountField, 1, 2);
        gp.add(actions,                1, 3);
        gp.add(new Label("Result:"),   0, 4); gp.add(resultLabel, 1, 4);

        getDialogPane().setContent(gp);

    }

    private void doConvert() {
        try {
            BigDecimal amt = new BigDecimal(amountField.getText().trim());
            if (amt.signum() < 0) throw new NumberFormatException();

            BigDecimal result = CurrencyRates.convert(fromBox.getValue(), toBox.getValue(), amt);
            resultLabel.setText("= " + result.stripTrailingZeros().toPlainString() + " " + toBox.getValue());

        } catch (Exception ex) {
            new Alert(Alert.AlertType.WARNING, "Enter a valid positive number.").showAndWait();
        }
    }
}