package org.example.dialogs;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.example.components.TransactionComponent;
import org.example.controllers.DashboardController;
import org.example.models.Transaction;
import org.example.utils.SqlUtil;
import org.example.utils.ThemeManager;

import java.time.Month;
import java.util.List;

public class ViewTransactionsDialog extends CustomDialog {

    private final DashboardController dashboardController;
    private final String monthName;

    public ViewTransactionsDialog(DashboardController dashboardController, String monthName) {
        super(dashboardController.getUser());

        this.dashboardController = dashboardController;
        this.monthName = monthName;

        setTitle("View Transactions");
        setWidth(1000);
        setHeight(700);

        ScrollPane transactionScrollPane = createTransactionScrollPane();
        getDialogPane().setContent(transactionScrollPane);

    }

    private ScrollPane createTransactionScrollPane() {
        VBox vBox = new VBox(20);

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(getHeight() - 40);

        List<Transaction> transactions = SqlUtil.getAllTransactionsByUserId(
                dashboardController.getUser().getId(),
                dashboardController.getCurrentYear(),
                Month.valueOf(monthName).getValue()
        );

        if (transactions != null) {
            for (Transaction t : transactions) {
                TransactionComponent comp = new TransactionComponent(dashboardController, t);
                comp.getStyleClass().add("border-light-gray"); 
                vBox.getChildren().add(comp);
            }
        }

        return scrollPane;
    }
}