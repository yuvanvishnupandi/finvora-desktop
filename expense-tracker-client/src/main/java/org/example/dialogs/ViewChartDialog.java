package org.example.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.example.models.MonthlyFinance;
import org.example.models.Transaction;
import org.example.models.User;
import org.example.utils.SqlUtil;
import org.example.utils.ThemeManager;

import java.math.BigDecimal;
import java.util.*;

public class ViewChartDialog extends CustomDialog {

    private final User user;
    private final ObservableList<MonthlyFinance> monthlyFinances;
    private final int year;

    public ViewChartDialog(User user, ObservableList<MonthlyFinance> monthlyFinances, int year) {
        super(user);
        this.user = user;
        this.monthlyFinances = monthlyFinances;
        this.year = year;

        setTitle("View Chart");
        setWidth(1100);
        setHeight(750);

        VBox leftBox = new VBox(8);
        leftBox.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(false);
        barChart.getStyleClass().add("text-size-md");
        barChart.setLegendVisible(true);
        barChart.setTitle("Income vs Expense — " + year);

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");

        for (MonthlyFinance mf : monthlyFinances) {
            incomeSeries.getData().add(new XYChart.Data<>(mf.getMonth(), mf.getIncome()));
            expenseSeries.getData().add(new XYChart.Data<>(mf.getMonth(), mf.getExpense()));
        }

        barChart.getData().addAll(incomeSeries, expenseSeries);
        leftBox.getChildren().add(barChart);

        VBox rightBox = new VBox(8);
        rightBox.setPadding(new Insets(10));

        List<PieChart.Data> slices = buildExpenseByCategorySlices(user.getId(), year);
        if (slices.isEmpty()) {
            rightBox.getChildren().add(new Label("No expenses found for " + year));
        } else {
            PieChart pie = new PieChart(FXCollections.observableArrayList(slices));
            pie.setTitle("Expenses by Category — " + year);
            pie.setLegendVisible(true);
            pie.setLabelsVisible(true);
            rightBox.getChildren().add(pie);

            Platform.runLater(() -> {
                
                pie.lookupAll(".chart-title").forEach(title -> title.setStyle("-fx-text-fill: #BEB9B9;"));
                pie.lookupAll(".chart-legend").forEach(legend -> legend.setStyle("-fx-text-fill: #BEB9B9;"));

                for (PieChart.Data d : pie.getData()) {
                    if (d.getNode() != null) {
                        String cleanId = "#" + d.getName().toLowerCase().replaceAll("[^a-z0-9]", "-") + "-slice";
                        d.getNode().setId(cleanId.substring(1)); 

                        d.getNode().lookupAll("Label").forEach(label ->
                                label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"));
                    }
                }

                pie.lookupAll(".chart-pie-label").forEach(label ->
                        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"));
            });
        }

        HBox root = new HBox(16, leftBox, rightBox);
        root.setPadding(new Insets(10));
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        leftBox.prefWidthProperty().bind(getDialogPane().widthProperty().multiply(0.6));
        barChart.prefWidthProperty().bind(leftBox.widthProperty());
        rightBox.prefWidthProperty().bind(getDialogPane().widthProperty().multiply(0.4));

        getDialogPane().setContent(root);

    }

    private List<PieChart.Data> buildExpenseByCategorySlices(int userId, int year) {
        List<Transaction> txList = SqlUtil.getAllTransactionsByUserId(userId, year, null);
        if (txList == null || txList.isEmpty()) return List.of();

        Map<String, BigDecimal> totals = new HashMap<>();
        for (Transaction t : txList) {
            if (!"expense".equalsIgnoreCase(t.getTransactionType())) continue;
            String category = (t.getTransactionCategory() != null &&
                    t.getTransactionCategory().getCategoryName() != null)
                    ? t.getTransactionCategory().getCategoryName()
                    : "Uncategorized";

            totals.merge(category, BigDecimal.valueOf(t.getTransactionAmount()), BigDecimal::add);
        }

        if (totals.isEmpty()) return List.of();

        List<Map.Entry<String, BigDecimal>> sorted = totals.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toList();

        List<PieChart.Data> result = new ArrayList<>();
        BigDecimal others = BigDecimal.ZERO;

        final int TOP = 6; 
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, BigDecimal> entry = sorted.get(i);
            if (i < TOP) {
                result.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
            } else {
                others = others.add(entry.getValue());
            }
        }

        if (others.compareTo(BigDecimal.ZERO) > 0) {
            result.add(new PieChart.Data("Others", others.doubleValue()));
        }

        return result;
    }
}