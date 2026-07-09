package org.example.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.example.models.Budget;
import org.example.models.Transaction;
import org.example.models.TransactionCategory;
import org.example.models.User;
import org.example.utils.SqlUtil;
import org.example.utils.ThemeManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class SetBudgetDialog extends Dialog<Budget> {

    private final User user;
    private final Budget original;

    private final ComboBox<TransactionCategory> categoryCombo = new ComboBox<>();
    private final TextField limitField = new TextField();
    private final ComboBox<Budget.PeriodType> periodTypeCombo = new ComboBox<>();
    private final Spinner<Integer> yearSpinner =
            new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2100, Year.now().getValue()));
    private final ComboBox<String> periodValueCombo = new ComboBox<>();

    public SetBudgetDialog(User user) {
        this(user, null);
    }

    public SetBudgetDialog(User user, Budget toEdit) {
        this.user = user;
        this.original = toEdit;

        setTitle(toEdit == null ? "Set Budget" : "Edit Budget");
        setHeaderText(toEdit == null ? "Create a budget limit" : "Edit budget limit");

        ButtonType okBtn = new ButtonType(toEdit == null ? "Save" : "Update", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        List<TransactionCategory> cats = SqlUtil.getAllTransactionCategoriesByUser(user);
        categoryCombo.setItems(FXCollections.observableArrayList(cats));
        categoryCombo.setPromptText("Select Category");
        categoryCombo.setConverter(new StringConverter<>() {
            @Override public String toString(TransactionCategory c) {
                return c == null ? "" : c.getCategoryName();
            }
            @Override public TransactionCategory fromString(String s) { return null; }
        });

        limitField.setPromptText("Limit Amount (₹)");

        periodTypeCombo.getItems().addAll(Budget.PeriodType.MONTHLY, Budget.PeriodType.QUARTERLY, Budget.PeriodType.YEARLY);
        periodTypeCombo.setValue(Budget.PeriodType.MONTHLY);

        form.add(new Label("Category:"), 0, 0); form.add(categoryCombo, 1, 0);
        form.add(new Label("Limit:"), 0, 1); form.add(limitField, 1, 1);
        form.add(new Label("Period:"), 0, 2); form.add(periodTypeCombo, 1, 2);
        form.add(new Label("Year:"), 0, 3); form.add(yearSpinner, 1, 3);
        form.add(new Label("Month/Quarter:"), 0, 4); form.add(periodValueCombo, 1, 4);

        getDialogPane().setContent(form);

        periodTypeCombo.valueProperty().addListener((obs, o, n) -> {
            updatePeriodValueItems(n);
            autoPickLatestPeriod(categoryCombo.getValue());
        });

        categoryCombo.valueProperty().addListener((obs, o, n) -> autoPickLatestPeriod(n));
        yearSpinner.valueProperty().addListener((obs, o, n) -> autoPickLatestPeriod(categoryCombo.getValue()));

        if (toEdit == null) {
            updatePeriodValueItems(Budget.PeriodType.MONTHLY);
            autoPickLatestPeriod(null);
        } else {
            TransactionCategory match = cats.stream()
                    .filter(c -> c.getCategoryName() != null &&
                                 c.getCategoryName().trim().equalsIgnoreCase(toEdit.getCategory()))
                    .findFirst().orElse(null);
            categoryCombo.setValue(match);
            limitField.setText(toEdit.getLimitAmount() == null ? "" : toEdit.getLimitAmount().toPlainString());
            periodTypeCombo.setValue(toEdit.getPeriodType());
            yearSpinner.getValueFactory().setValue(toEdit.getYear());
            updatePeriodValueItems(toEdit.getPeriodType());

            if (toEdit.getPeriodType() == Budget.PeriodType.MONTHLY && toEdit.getMonth() != null) {
                periodValueCombo.setValue(String.format("%02d", toEdit.getMonth()));
            } else if (toEdit.getPeriodType() == Budget.PeriodType.QUARTERLY && toEdit.getQuarter() != null) {
                periodValueCombo.setValue("Q" + toEdit.getQuarter());
            }
        }

        final Button okButton = (Button) getDialogPane().lookupButton(okBtn);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (!validateInputs()) evt.consume();
        });

        setResultConverter(bt -> {
            if (bt != okBtn) return null;

            TransactionCategory chosen = categoryCombo.getValue();
            BigDecimal limit;
            try {
                limit = new BigDecimal(limitField.getText().trim());
                if (limit.signum() <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.WARNING, "Enter a valid positive limit amount.").showAndWait();
                return null;
            }

            Budget.PeriodType type = periodTypeCombo.getValue();
            Integer month = null, quarter = null;

            if (type == Budget.PeriodType.MONTHLY) {
                month = parseMonth(periodValueCombo.getValue());
            } else if (type == Budget.PeriodType.QUARTERLY) {
                quarter = parseQuarter(periodValueCombo.getValue());
            }

            Budget b = new Budget();
            if (original != null) b.setId(original.getId());
            b.setUserEmail(user.getEmail());
            b.setCategory(chosen.getCategoryName());
            b.setLimitAmount(limit);
            b.setSpentAmount(BigDecimal.ZERO);
            b.setYear(yearSpinner.getValue());
            b.setPeriodType(type);
            b.setMonth(month);
            b.setQuarter(quarter);
            return b;
        });
    }

    private void updatePeriodValueItems(Budget.PeriodType type) {
        int curMonth = LocalDate.now().getMonthValue();
        int curQuarter = (curMonth - 1) / 3 + 1;
        periodValueCombo.getItems().clear();
        periodValueCombo.setDisable(false);

        if (type == Budget.PeriodType.MONTHLY) {
            periodValueCombo.getItems().addAll(
                    IntStream.rangeClosed(1, 12).mapToObj(m -> String.format("%02d", m)).toList()
            );
            periodValueCombo.setValue(String.format("%02d", curMonth));
        } else if (type == Budget.PeriodType.QUARTERLY) {
            periodValueCombo.getItems().addAll("Q1", "Q2", "Q3", "Q4");
            periodValueCombo.setValue("Q" + curQuarter);
        } else {
            periodValueCombo.setDisable(true);
            periodValueCombo.setPromptText("N/A");
        }
    }

    private void autoPickLatestPeriod(TransactionCategory cat) {
        if (cat == null) return;
        int year = yearSpinner.getValue();
        List<Transaction> txAll = SqlUtil.getAllTransactionsByUserId(user.getId(), year, null);
        if (txAll == null || txAll.isEmpty()) return;

        String match = cat.getCategoryName().trim();

        Transaction latest = txAll.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getTransactionType()))
                .filter(t -> t.getTransactionCategory() != null &&
                        t.getTransactionCategory().getCategoryName().trim().equalsIgnoreCase(match))
                .max(Comparator.comparing(Transaction::getTransactionDate))
                .orElse(null);

        if (latest == null) return;

        int m = latest.getTransactionDate().getMonthValue();
        switch (periodTypeCombo.getValue()) {
            case MONTHLY -> {
                String mm = String.format("%02d", m);
                if (!periodValueCombo.getItems().contains(mm)) updatePeriodValueItems(Budget.PeriodType.MONTHLY);
                periodValueCombo.setValue(mm);
            }
            case QUARTERLY -> {
                int q = (m - 1) / 3 + 1;
                String qq = "Q" + q;
                if (!periodValueCombo.getItems().contains(qq)) updatePeriodValueItems(Budget.PeriodType.QUARTERLY);
                periodValueCombo.setValue(qq);
            }
            case YEARLY -> {}
        }
    }

    private Integer parseMonth(String v) {
        try {
            return v == null ? null : Integer.parseInt(v);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseQuarter(String v) {
        return switch (v) {
            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;
            default -> null;
        };
    }

    private boolean validateInputs() {
        if (categoryCombo.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a category").showAndWait();
            return false;
        }
        if (limitField.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Limit amount is required").showAndWait();
            return false;
        }
        try {
            new BigDecimal(limitField.getText().trim());
        } catch (Exception ex) {
            new Alert(Alert.AlertType.WARNING, "Limit must be a valid number").showAndWait();
            return false;
        }

        Budget.PeriodType type = periodTypeCombo.getValue();
        if (type == Budget.PeriodType.MONTHLY && periodValueCombo.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Select a month for Monthly budget").showAndWait();
            return false;
        }
        if (type == Budget.PeriodType.QUARTERLY && periodValueCombo.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Select a quarter for Quarterly budget").showAndWait();
            return false;
        }

        return true;
    }
}