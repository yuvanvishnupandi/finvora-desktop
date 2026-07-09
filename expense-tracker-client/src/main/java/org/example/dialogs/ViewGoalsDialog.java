package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import org.example.models.SavingsGoal;
import org.example.utils.GoalStore;
import org.example.utils.ThemeManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ViewGoalsDialog extends Dialog<Void> {

    private final int userId;
    private final List<SavingsGoal> goals;

    public ViewGoalsDialog(List<SavingsGoal> goals, int userId) {
        this.userId = userId;
        this.goals = goals;

        setTitle("Your Savings Goals");
        DialogPane pane = getDialogPane();
        pane.setPrefSize(520, 500);
        pane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        pane.getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        if (goals == null || goals.isEmpty()) {
            content.getChildren().add(new Label("No goals found."));
        } else {
            for (SavingsGoal goal : goals) {
                content.getChildren().add(createGoalNode(goal));
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        pane.setContent(scrollPane);

    }

    private VBox createGoalNode(SavingsGoal goal) {
        VBox box = new VBox(6);
        box.getStyleClass().add("goal-item");

        Label title = new Label("🎯 " + goal.getName());
        title.getStyleClass().add("goal-title");

        Label deadline = new Label("⏳ " + goal.getDeadline());
        deadline.getStyleClass().add("goal-subtext");

        Label money = new Label("💰 ₹" + goal.getCurrentAmount() + " / ₹" + goal.getTargetAmount());
        money.getStyleClass().add("goal-subtext");

        ProgressBar bar = new ProgressBar();
        double pct = 0;
        try {
            if (goal.getTargetAmount() != null && goal.getTargetAmount().doubleValue() > 0) {
                pct = goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
        } catch (Exception ignored) {}
        bar.setProgress(Math.min(1.0, pct));
        bar.setPrefWidth(380);

        Button add = new Button("➕ Add");
        add.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog("0");
            d.setHeaderText("Add amount to: " + goal.getName());
            Optional<String> r = d.showAndWait();
            r.ifPresent(val -> {
                try {
                    BigDecimal inc = new BigDecimal(val);
                    BigDecimal next = goal.getCurrentAmount().add(inc);
                    if (goal.getTargetAmount() != null && next.compareTo(goal.getTargetAmount()) > 0) {
                        next = goal.getTargetAmount();
                    }
                    goal.setCurrentAmount(next);
                    GoalStore.update(userId, goal);
                    refresh();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid amount!").showAndWait();
                }
            });
        });

        Button edit = new Button("✏ Edit");
        edit.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog(goal.getName());
            d.setHeaderText("Edit Goal Name:");
            d.showAndWait().ifPresent(name -> {
                goal.setName(name);
                GoalStore.update(userId, goal);
                refresh();
            });
        });

        Button del = new Button("🗑 Delete");
        del.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + goal.getName() + "?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    GoalStore.delete(userId, goal.getId());
                    refresh();
                }
            });
        });

        ToolBar tools = new ToolBar(add, edit, del);
        box.getChildren().addAll(title, deadline, money, bar, tools);
        return box;
    }

    private void refresh() {
        this.close();
        List<SavingsGoal> updated = GoalStore.getGoals(userId);
        new ViewGoalsDialog(updated, userId).showAndWait();
    }
}