
package org.example.views;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.dialogs.SetBudgetDialog;
import org.example.models.Budget;
import org.example.models.Transaction;
import org.example.models.User;
import org.example.utils.BudgetStore;
import org.example.utils.SqlUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BudgetProgressView extends Stage {

    private final User user;
    private final TableView<Row> table = new TableView<>();

    public BudgetProgressView(User user, Budget singleBudget) {
        this(user, singleBudget == null ? new ArrayList<>() : List.of(singleBudget));
    }

    public BudgetProgressView(User user, List<Budget> budgets) {
        this.user = user;
        setTitle("Budget Progress – " + (user != null ? user.getEmail() : ""));

        TableColumn<Row, String> category = new TableColumn<>("Category");
        category.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().category));

        TableColumn<Row, String> period = new TableColumn<>("Period");
        period.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().period));

        TableColumn<Row, String> limit = new TableColumn<>("Limit");
        limit.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().limit));

        TableColumn<Row, String> spent = new TableColumn<>("Spent");
        spent.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().spent));

        TableColumn<Row, String> remaining = new TableColumn<>("Remaining");
        remaining.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().remaining));

        TableColumn<Row, Number> progress = new TableColumn<>("Progress");
        progress.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().ratio));
        progress.setCellFactory(progressBarCellFactory());

        TableColumn<Row, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().status));

        table.getColumns().addAll(category, period, limit, spent, remaining, progress, status);

        ToolBar tools = buildToolbar();
        BorderPane root = new BorderPane(table);
        root.setTop(new HBox(tools));
        root.setStyle("-fx-background-color:#23272a; -fx-padding:10;");
        BorderPane.setMargin(tools, new Insets(0,0,10,0));

        setScene(new Scene(root, 900, 480));

        refreshTable(); 
        if (budgets != null && !budgets.isEmpty()) {
            
            budgets.forEach(b -> {
                if (BudgetStore.getById(user.getId(), b.getId()) == null) BudgetStore.add(user.getId(), b);
            });
            refreshTable();
        }

        table.setRowFactory(t -> {
            TableRow<Row> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty() && e.getClickCount() == 2) editSelected();
            });
            return r;
        });
    }

    private ToolBar buildToolbar() {
        Button add = new Button("Add");
        Button edit = new Button("Edit");
        Button del = new Button("Delete");
        Button refresh = new Button("Refresh");

        add.setOnAction(e -> {
            SetBudgetDialog dlg = new SetBudgetDialog(user);
            Budget res = dlg.showAndWait().orElse(null);
            if (res != null) {
                res.setSpentAmount(computeSpentFor(user, res));
                BudgetStore.add(user.getId(), res);
                refreshTable();
            }
        });

        edit.setOnAction(e -> editSelected());

        del.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                new Alert(Alert.AlertType.INFORMATION, "Select a budget to delete").showAndWait();
                return;
            }
            BudgetStore.removeById(user.getId(), row.id);
            refreshTable();
        });

        refresh.setOnAction(e -> refreshTable());

        ToolBar tb = new ToolBar(add, edit, del, refresh);
        tb.setStyle("-fx-background-color:#2b2f34;");
        return tb;
        }

    private void editSelected() {
        Row row = table.getSelectionModel().getSelectedItem();
        if (row == null) {
            new Alert(Alert.AlertType.INFORMATION, "Select a budget to edit").showAndWait();
            return;
        }
        Budget original = BudgetStore.getById(user.getId(), row.id);
        if (original == null) return;
        SetBudgetDialog dlg = new SetBudgetDialog(user, original);
        Budget updated = dlg.showAndWait().orElse(null);
        if (updated != null) {
            updated.setId(original.getId());
            updated.setSpentAmount(computeSpentFor(user, updated));
            BudgetStore.update(user.getId(), updated);
            refreshTable();
        }
    }

    private void refreshTable() {
        table.getItems().clear();
        List<Budget> all = BudgetStore.getBudgets(user.getId());
        for (Budget b : all) {
            b.setSpentAmount(computeSpentFor(user, b));
            table.getItems().add(Row.from(b));
        }
    }

    private BigDecimal computeSpentFor(User user, Budget b) {
        if (b == null || user == null) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;

        switch (b.getPeriodType()) {
            case MONTHLY -> {
                List<Transaction> tx = SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), b.getMonth());
                sum = sum.add(sumCategory(tx, b.getCategory()));
            }
            case QUARTERLY -> {
                int q = b.getQuarter() == null ? 1 : b.getQuarter();
                int start = (q - 1) * 3 + 1;
                for (int m = start; m <= start + 2; m++) {
                    List<Transaction> tx = SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), m);
                    sum = sum.add(sumCategory(tx, b.getCategory()));
                }
            }
            case YEARLY -> {
                List<Transaction> tx = SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), null);
                sum = sum.add(sumCategory(tx, b.getCategory()));
            }
        }
        return sum.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal sumCategory(List<Transaction> tx, String categoryName) {
        BigDecimal s = BigDecimal.ZERO;
        if (tx == null || categoryName == null) return s;
        String wanted = categoryName.trim();
        for (Transaction t : tx) {
            if (!"expense".equalsIgnoreCase(t.getTransactionType())) continue;
            if (t.getTransactionCategory() == null) continue;
            String cat = t.getTransactionCategory().getCategoryName();
            if (cat != null && cat.trim().equalsIgnoreCase(wanted)) {
                s = s.add(BigDecimal.valueOf(t.getTransactionAmount()));
            }
        }
        return s;
    }

    private static javafx.util.Callback<TableColumn<Row, Number>, TableCell<Row, Number>> progressBarCellFactory() {
        return col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar(0);
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    double v = Math.max(0, Math.min(1, value.doubleValue()));
                    bar.setProgress(v);
                    bar.setPrefWidth(140);
                    setGraphic(bar);
                }
            }
        };
    }

    private static class Row {
        final Long id;
        final String category;
        final String period;
        final String limit;
        final String spent;
        final String remaining;
        final double ratio;
        final String status;

        Row(Long id, String category, String period, String limit, String spent, String remaining, double ratio, String status) {
            this.id = id; this.category = category; this.period = period;
            this.limit = limit; this.spent = spent; this.remaining = remaining;
            this.ratio = ratio; this.status = status;
        }

        static Row from(Budget b) {
            BigDecimal lim = b.getLimitAmount() == null ? BigDecimal.ZERO : b.getLimitAmount();
            BigDecimal sp  = b.getSpentAmount() == null ? BigDecimal.ZERO : b.getSpentAmount();
            BigDecimal rem = lim.subtract(sp);

            double r = 0.0;
            if (lim.signum() > 0) {
                r = sp.divide(lim, 6, java.math.RoundingMode.HALF_UP).doubleValue();
                if (r < 0) r = 0; if (r > 1) r = 1;
            }

            String sts = sp.compareTo(lim) > 0 ? "Limit exceeded" : "OK";

            return new Row(
                    b.getId(),
                    b.getCategory(),
                    b.getPeriodLabel(),
                    money(lim),
                    money(sp),
                    money(rem),
                    r,
                    sts
            );
        }

        private static String money(BigDecimal v) {
            if (v == null) return "₹0.00";
            return "₹" + v.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}