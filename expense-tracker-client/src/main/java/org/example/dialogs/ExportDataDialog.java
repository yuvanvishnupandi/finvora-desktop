package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.models.User;
import org.example.utils.ThemeManager;

import java.time.LocalDate;

public class ExportDataDialog extends Dialog<ExportDataDialog.ExportOptions> {

    private final CheckBox cbTransactions = new CheckBox("Transactions");
    private final CheckBox cbCategories = new CheckBox("Categories");
    private final CheckBox cbBudgets = new CheckBox("Budgets");

    private final DatePicker startDate = new DatePicker();
    private final DatePicker endDate = new DatePicker();

    public ExportDataDialog(User user) {
        setTitle("Export Data (CSV)");
        setHeaderText("Choose what to export and an optional date range");

        ButtonType okBtn = new ButtonType("Export", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        cbTransactions.setSelected(true);

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(15));

        gp.add(new Label("Include:"), 0, 0);
        gp.add(cbTransactions, 1, 0);
        gp.add(cbCategories, 1, 1);
        gp.add(cbBudgets, 1, 2);

        gp.add(new Label("Start date:"), 0, 3);
        gp.add(startDate, 1, 3);

        gp.add(new Label("End date:"), 0, 4);
        gp.add(endDate, 1, 4);

        getDialogPane().setContent(gp);

        final Button okButton = (Button) getDialogPane().lookupButton(okBtn);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (!cbTransactions.isSelected() && !cbCategories.isSelected() && !cbBudgets.isSelected()) {
                new Alert(Alert.AlertType.WARNING, "Select at least one item to export.").showAndWait();
                evt.consume();
                return;
            }

            LocalDate s = startDate.getValue();
            LocalDate e = endDate.getValue();
            if (s != null && e != null && e.isBefore(s)) {
                new Alert(Alert.AlertType.WARNING, "End date must be on or after start date.").showAndWait();
                evt.consume();
            }
        });

        setResultConverter(bt -> {
            if (bt != okBtn) return null;

            return new ExportOptions(
                    cbTransactions.isSelected(),
                    cbCategories.isSelected(),
                    cbBudgets.isSelected(),
                    startDate.getValue(),
                    endDate.getValue()
            );
        });
    }

    public static class ExportOptions {
        public final boolean transactions;
        public final boolean categories;
        public final boolean budgets;
        public final LocalDate start;
        public final LocalDate end;

        public ExportOptions(boolean transactions, boolean categories, boolean budgets,
                             LocalDate start, LocalDate end) {
            this.transactions = transactions;
            this.categories = categories;
            this.budgets = budgets;
            this.start = start;
            this.end = end;
        }
    }
}