package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.scene.Scene; 
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.components.TransactionComponent;
import org.example.dialogs.CreateGoalDialog;
import org.example.dialogs.CreateNewCategoryDialog;
import org.example.dialogs.CreateOrEditTransactionDialog;
import org.example.dialogs.CurrencyConverterDialog;
import org.example.dialogs.ExportDataDialog;
import org.example.dialogs.SetBudgetDialog;
import org.example.dialogs.ViewChartDialog;
import org.example.dialogs.ViewGoalsDialog;
import org.example.dialogs.ViewOrEditTransactionCategoryDialog;
import org.example.dialogs.ViewTransactionsDialog;
import org.example.models.Budget;
import org.example.models.MonthlyFinance;
import org.example.models.SavingsGoal;
import org.example.models.Transaction;
import org.example.models.User;
import org.example.utils.BudgetStore;
import org.example.utils.CsvExportUtil;
import org.example.utils.GoalStore;
import org.example.utils.SqlUtil;
import org.example.utils.ThemeManager;
import org.example.views.BudgetProgressView;
import org.example.views.DashboardView;
import org.example.views.LoginView;
import org.example.services.AIVoiceService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DashboardController {

    private final int recentTransactionSize = 10;
    private final DashboardView view;
    private User user;
    private int currentYear;
    private String currentMonth;
    private Budget currentBudget;

    public DashboardController(DashboardView view) {
        this.view = view;
        this.currentYear = view.getYearComboBox().getValue();
        this.currentMonth = "ALL";
        initListeners();
        fetchUserData();
    }

    public void fetchUserData() {
        view.getLoadingAnimationPane().setVisible(true);

        try {
            user = SqlUtil.getUserByEmail(view.getEmail());

            if (user != null) {
                String nm = user.getName() == null ? "" : user.getName();
                String em = user.getEmail() == null ? "" : user.getEmail();
                
                view.getUserNameLabel().setText(nm);
                view.getUserEmailLabel().setText("<" + em + ">");
            }

            loadYears();
            pickActiveBudgetFromStore();
            loadBalances();
            view.getTransactionTable().setItems(calcMonthly());
            loadRecents();
            refreshGoalWidget(); 
            
            // Run AI Proactive Monitor asynchronously
            new Thread(() -> {
                try {
                    List<Transaction> allTx = SqlUtil.getAllTransactionsByUserId(user.getId(), currentYear, null);
                    List<org.example.models.Budget> budgets = org.example.utils.BudgetStore.getBudgets(user.getId());
                    if (budgets != null) {
                        for (org.example.models.Budget b : budgets) {
                            b.setSpentAmount(calculateSpentFor(b));
                        }
                    }
                    List<String> alerts = org.example.services.AIProactiveMonitor.analyzeTransactions(allTx, budgets);
                    javafx.application.Platform.runLater(() -> {
                        view.aiAlertsButton.setText("🔔 AI Alerts (" + alerts.size() + ")");
                        if (!alerts.isEmpty()) {
                            view.aiAlertsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e13742; -fx-font-weight: bold; -fx-cursor: hand;");
                            view.aiAlertsButton.setOnAction(e -> {
                                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                                alert.setTitle("Proactive AI Alerts");
                                alert.setHeaderText("Finvora AI found " + alerts.size() + " anomalies/insights in your recent spending:");
                                alert.setContentText(String.join("\n\n", alerts));
                                alert.showAndWait();
                            });
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.getLoadingAnimationPane().setVisible(false);
        }
    }

    private void loadYears() {
        List<Integer> years = SqlUtil.getAllDistinctYears(user.getId());
        if (years != null) {
            for (Integer y : years)
                if (!view.getYearComboBox().getItems().contains(y))
                    view.getYearComboBox().getItems().add(y);
        }

        ObservableList<String> monthOptions = FXCollections.observableArrayList(
                "ALL", "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        );
        if (view.getMonthQuarterComboBox() != null) {
            view.getMonthQuarterComboBox().setItems(monthOptions);
            view.getMonthQuarterComboBox().setValue("ALL");
        }
    }

    private void loadBalances() {
        BigDecimal in  = BigDecimal.ZERO;
        BigDecimal out = BigDecimal.ZERO;

        List<Transaction> yearTx = SqlUtil.getAllTransactionsByUserId(user.getId(), currentYear, null);
        if (yearTx != null) {
            for (Transaction t : yearTx) {
                BigDecimal amt = BigDecimal.valueOf(t.getTransactionAmount());
                if ("income".equalsIgnoreCase(t.getTransactionType())) in  = in.add(amt);
                else out = out.add(amt);
            }
        }

        in  = in.setScale(2, RoundingMode.HALF_UP);
        out = out.setScale(2, RoundingMode.HALF_UP);
        BigDecimal bal = in.subtract(out).setScale(2, RoundingMode.HALF_UP);

        view.getTotalIncome().setText("₹" + in);
        view.getTotalExpense().setText("₹" + out);
        view.getCurrentBalance().setText("₹" + bal);

        view.getTotalIncome().getStyleClass().removeAll("text-light-red", "text-light-green");
        view.getTotalIncome().getStyleClass().add("text-light-green");

        view.getTotalExpense().getStyleClass().removeAll("text-light-red", "text-light-green");
        view.getTotalExpense().getStyleClass().add("text-light-red");
        
        view.getCurrentBalance().getStyleClass().removeAll("text-light-green", "text-light-red");
        if (bal.compareTo(BigDecimal.ZERO) < 0) {
            view.getCurrentBalance().getStyleClass().add("text-light-red");
        } else {
            view.getCurrentBalance().getStyleClass().add("text-light-green");
        }
        
        view.getBudgetRemaining().getStyleClass().removeAll("text-light-green", "text-light-red", "text-light-gray");

        if (currentBudget == null) {
            view.getBudgetStatusLabel().setText("No Budget Set Yet");
            view.getBudgetRemaining().setText("—");
            view.getBudgetRemaining().getStyleClass().add("text-light-gray"); 
        } else {
            currentBudget.setSpentAmount(calculateSpentFor(currentBudget));
            BigDecimal remaining = currentBudget.getRemaining().setScale(2, RoundingMode.HALF_UP);
            view.getBudgetRemaining().setText("₹" + remaining);
            
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                view.getBudgetRemaining().getStyleClass().add("text-light-red");
                view.getBudgetStatusLabel().setText("Budget Overspent:");
            } else {
                view.getBudgetRemaining().getStyleClass().add("text-light-green");
                view.getBudgetStatusLabel().setText("Budget Remaining:");
            }
        }
        
        List<org.example.models.SavingsGoal> goals = org.example.utils.GoalStore.getGoals(user.getId());
        if (goals != null && !goals.isEmpty()) {
            org.example.models.SavingsGoal g = goals.get(0);
            view.getTopGoalNameLabel().setText(g.getName() + " (₹" + g.getCurrentAmount() + " / ₹" + g.getTargetAmount() + ")");
            double pct = 0;
            if (g.getTargetAmount() != null && g.getTargetAmount().doubleValue() > 0) {
                pct = g.getCurrentAmount().divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue();
            }
            view.getTopGoalProgressBar().setProgress(Math.min(1.0, pct));
        } else {
            view.getTopGoalNameLabel().setText("No Active Goals");
            view.getTopGoalProgressBar().setProgress(0);
        }
    }

    private ObservableList<MonthlyFinance> calcMonthly() {
        double[] inc = new double[12], exp = new double[12];
        List<Transaction> list = SqlUtil.getAllTransactionsByUserId(user.getId(), currentYear, null);

        if (list != null) {
            for (Transaction t : list) {
                int m = t.getTransactionDate().getMonth().getValue() - 1;
                boolean matches = "ALL".equals(currentMonth)
                        || t.getTransactionDate().getMonth().name().equals(currentMonth);
                if (matches) {
                    if ("income".equalsIgnoreCase(t.getTransactionType())) inc[m] += t.getTransactionAmount();
                    else exp[m] += t.getTransactionAmount();
                }
            }
        }

        ObservableList<MonthlyFinance> data = FXCollections.observableArrayList();
        for (int i = 0; i < 12; i++)
            data.add(new MonthlyFinance(Month.of(i + 1).name(),
                    BigDecimal.valueOf(inc[i]), BigDecimal.valueOf(exp[i])));
        return data;
    }

    private void loadRecents() {
        view.getRecentTransactionBox().getChildren().clear();
        
        List<Transaction> recent = SqlUtil.getRecentTransactionByUserId(user.getId(), 0, 0, 500);
        
        if (recent != null) {
            recent.sort((t1, t2) -> {
                int cmp = t2.getTransactionDate().compareTo(t1.getTransactionDate());
                if (cmp == 0) cmp = Integer.compare(t2.getId(), t1.getId());
                return cmp;
            });
            for (Transaction t : recent)
                view.getRecentTransactionBox().getChildren().add(new TransactionComponent(this, t));
        }
    }

    private void initListeners() {
        view.getCreateCategoryMenuItem().setOnAction(e -> new CreateNewCategoryDialog(user).showAndWait());
        view.getViewCategoriesMenuItem().setOnAction(e -> new ViewOrEditTransactionCategoryDialog(user, this).showAndWait());

        view.getAddGoalMenuItem().setOnAction(e -> addGoal());
        view.getViewGoalsMenuItem().setOnAction(e -> seeGoals());
        
        view.getAboutUsMenuItem().setOnAction(e -> showAboutUs());

        view.getSetMonthlyBudgetsMenuItem().setOnAction(e -> {
            Optional<Budget> budget = new SetBudgetDialog(user).showAndWait();
            budget.ifPresent(b -> {
                b.setSpentAmount(calculateSpentFor(b));
                currentBudget = b;
                BudgetStore.add(user.getId(), b);
                new Alert(Alert.AlertType.INFORMATION,
                        "Budget set for " + b.getPeriodLabel() + " on " + b.getCategory()).showAndWait();
                List<Budget> all = BudgetStore.getBudgets(user.getId());
                all.forEach(x -> x.setSpentAmount(calculateSpentFor(x)));
                new BudgetProgressView(user, all).show();
                loadBalances();
            });
        });

        view.getViewBudgetProgressMenuItem().setOnAction(e -> {
            List<Budget> all = BudgetStore.getBudgets(user.getId());
            if (all.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION,
                        "No budget set yet. Use 'Set Monthly Budgets' first.").showAndWait();
            } else {
                all.forEach(b -> b.setSpentAmount(calculateSpentFor(b)));
                new BudgetProgressView(user, all).show();
            }
        });

        view.getConvertCurrencyMenuItem().setOnAction(e -> new CurrencyConverterDialog().showAndWait());

        view.getExportDataMenuItem().setOnAction(e -> {
            ExportDataDialog dlg = new ExportDataDialog(user);
            ExportDataDialog.ExportOptions opt = dlg.showAndWait().orElse(null);
            if (opt == null) return;

            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Choose export folder");
            File dir = dc.showDialog(view.getMenuBar().getScene().getWindow());
            if (dir == null) return;

            try {
                CsvExportUtil.exportAll(user, opt, dir.toPath());
                new Alert(Alert.AlertType.INFORMATION,
                        "Exported to: " + dir.getAbsolutePath()).showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).showAndWait();
            }
        });

        view.getGeneratePdfReportMenuItem().setOnAction(e -> {
            ExportDataDialog dlg = new ExportDataDialog(user);
            ExportDataDialog.ExportOptions opt = dlg.showAndWait().orElse(null);
            if (opt == null) return;

            FileChooser fc = new FileChooser();
            fc.setTitle("Save PDF report");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            String defaultName = "report-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
            fc.setInitialFileName(defaultName);
            File out = fc.showSaveDialog(view.getMenuBar().getScene().getWindow());
            if (out == null) return;

            try {
                generatePdfReport(opt.start, opt.end, out);
                new Alert(Alert.AlertType.INFORMATION, "PDF saved: " + out.getAbsolutePath()).showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "PDF generation failed: " + ex.getMessage()).showAndWait();
            }
        });

        view.getLogoutMenuItem().setOnAction(e -> {
            new LoginView().show();
        });

        view.getYearComboBox().setOnAction((ActionEvent e) -> {
            currentYear = view.getYearComboBox().getValue();
            fetchUserData();
        });

        if (view.getMonthQuarterComboBox() != null) {
            view.getMonthQuarterComboBox().setOnAction((ActionEvent e) -> {
                @SuppressWarnings("unchecked") ComboBox<String> src = (ComboBox<String>) e.getSource();
                currentMonth = src.getValue();
                view.getTransactionTable().setItems(calcMonthly());
            });
        }

        view.getAddTransactionButton().setOnMouseClicked(
                (MouseEvent e) -> new CreateOrEditTransactionDialog(this, false).showAndWait());
                
        view.scanReceiptButton.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Receipt Image or Document");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Receipts", "*.jpg", "*.pdf"));
            File img = fc.showOpenDialog(view.getAddTransactionButton().getScene().getWindow());
            if (img != null) {
                view.getAiStatusLabel().setText("AI Vision: Extracting receipt data...");
                new Thread(() -> {
                    try {
                        com.google.gson.JsonObject result = org.example.services.AIVisionService.processReceipt(img);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                com.google.gson.JsonObject tJson = new com.google.gson.JsonObject();
                                
                                String vendor = result.has("vendorName") && !result.get("vendorName").isJsonNull() ? result.get("vendorName").getAsString() : "Scanned Receipt";
                                
                                double amount = 0.0;
                                if (result.has("totalAmount") && !result.get("totalAmount").isJsonNull()) {
                                    String amtStr = result.get("totalAmount").getAsString().replaceAll("[^0-9.]", "");
                                    try { amount = Double.parseDouble(amtStr); } catch (Exception ignored) {}
                                }
                                
                                String date = java.time.LocalDate.now().toString();
                                if (result.has("date") && !result.get("date").isJsonNull()) {
                                    String dStr = result.get("date").getAsString();
                                    try {
                                        // Attempt to parse YYYY-MM-DD
                                        java.time.LocalDate.parse(dStr);
                                        date = dStr;
                                    } catch (Exception ignored) {
                                        try {
                                            // Attempt DD-MM-YYYY
                                            String[] parts = dStr.split("[-/]");
                                            if (parts.length == 3) {
                                                if (parts[0].length() == 4) date = parts[0] + "-" + parts[1] + "-" + parts[2];
                                                else date = parts[2] + "-" + parts[1] + "-" + parts[0];
                                            }
                                        } catch (Exception ignored2) {}
                                    }
                                }
                                
                                String categoryName = result.has("category") && !result.get("category").isJsonNull() ? result.get("category").getAsString() : "Uncategorized";
                                
                                tJson.addProperty("transactionName", vendor);
                                tJson.addProperty("transactionAmount", amount);
                                tJson.addProperty("transactionType", "expense");
                                tJson.addProperty("transactionDate", date);
                                
                                org.example.models.TransactionCategory targetCat = null;
                                java.util.List<org.example.models.TransactionCategory> cats = org.example.utils.SqlUtil.getAllTransactionCategoriesByUser(user);
                                if (cats != null) {
                                    for(org.example.models.TransactionCategory c : cats) {
                                        if(c.getCategoryName().equalsIgnoreCase(categoryName)) {
                                            targetCat = c;
                                            break;
                                        }
                                    }
                                }
                                if (targetCat == null && cats != null && !cats.isEmpty()) targetCat = cats.get(0);
                                if (targetCat != null) {
                                    com.google.gson.JsonObject catObj = new com.google.gson.JsonObject();
                                    catObj.addProperty("id", targetCat.getId());
                                    tJson.add("transactionCategory", catObj);
                                }
                                
                                com.google.gson.JsonObject userObj = new com.google.gson.JsonObject();
                                userObj.addProperty("id", user.getId());
                                tJson.add("user", userObj);
                                
                                org.example.utils.SqlUtil.postTransaction(tJson);
                                view.getAiStatusLabel().setText("AI Vision: Receipt parsed successfully!");
                                fetchUserData();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                view.getAiStatusLabel().setText("AI Vision: Missing or invalid fields in receipt.");
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> view.getAiStatusLabel().setText("AI Vision: Failed to read receipt."));
                    }
                }).start();
            }
        });

        view.getViewChartButton().setOnAction(
                e -> new ViewChartDialog(user, view.getTransactionTable().getItems(), currentYear).showAndWait());

        view.getTransactionTable().setRowFactory(table -> {
            TableRow<MonthlyFinance> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 2)
                    new ViewTransactionsDialog(this, row.getItem().getMonth()).showAndWait();
            });
            return row;
        });

        AIVoiceService voiceService = new AIVoiceService();

        view.getSendBtn().setOnAction(e -> {
            String text = view.getChatInput().getText();
            if(!text.isEmpty()){
                view.getAiStatusLabel().setText("Finvora AI is processing your request...");
                String context = buildDeepContext();
                
                voiceService.processTextDirectly(text, context, "Professional",
                    status -> javafx.application.Platform.runLater(() -> view.getAiStatusLabel().setText(status)),
                    advice -> javafx.application.Platform.runLater(() -> view.getAiAdviceArea().setText(advice)),
                    intentJson -> handleIntent(intentJson)
                );
                view.getChatInput().clear();
            } else {
                view.getAiStatusLabel().setText("Please type a message first.");
            }
        });

        view.getVoiceBtn().setOnAction(e -> {
            if (view.getVoiceBtn().isSelected()) {
                voiceService.startRecording();
                view.getAiStatusLabel().setText("Listening... (Click Voice again to send)");
            } else {
                view.getAiStatusLabel().setText("Processing voice...");
                String context = buildDeepContext();
                voiceService.stopRecordingAndProcess(context, "Professional",
                    status -> javafx.application.Platform.runLater(() -> view.getAiStatusLabel().setText(status)),
                    advice -> javafx.application.Platform.runLater(() -> view.getAiAdviceArea().setText(advice)),
                    intentJson -> handleIntent(intentJson)
                );
            }
        });
    }

    private String buildDeepContext() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Balance: %s, Income: %s, Expense: %s, Budget: %s. ", 
                view.getCurrentBalance().getText(), view.getTotalIncome().getText(), 
                view.getTotalExpense().getText(), view.getBudgetRemaining().getText()));
        
        java.util.List<org.example.models.Transaction> recents = org.example.utils.SqlUtil.getRecentTransactionByUserId(user.getId(), 0, 0, 15);
        if (recents != null && !recents.isEmpty()) {
            sb.append("Recent Transactions: ");
            for (org.example.models.Transaction t : recents) {
                sb.append(String.format("[%s, %s, %s, %s], ", t.getTransactionDate(), t.getTransactionType(), t.getTransactionAmount(), t.getTransactionCategory() != null ? t.getTransactionCategory().getCategoryName() : ""));
            }
        }
        
        java.util.List<org.example.models.Budget> budgets = org.example.utils.BudgetStore.getBudgets(user.getId());
        if (budgets != null && !budgets.isEmpty()) {
            sb.append("Budgets: ");
            for (org.example.models.Budget b : budgets) {
                sb.append(String.format("[%s limit %s spent %s], ", b.getCategory(), b.getLimitAmount(), b.getSpentAmount()));
            }
        }
        
        java.util.List<org.example.models.SavingsGoal> goals = org.example.utils.GoalStore.getGoals(user.getId());
        if (goals != null && !goals.isEmpty()) {
            sb.append("Savings Goals: ");
            for (org.example.models.SavingsGoal g : goals) {
                sb.append(String.format("[%s target %s saved %s deadline %s], ", g.getName(), g.getTargetAmount(), g.getCurrentAmount(), g.getDeadline()));
            }
        }
        
        return sb.toString();
    }
    
    private void handleIntent(String intentJsonStr) {
        javafx.application.Platform.runLater(() -> {
            try {
                // First, try to clean the intentJsonStr if it has markdown block ```json ... ```
                String cleanJson = intentJsonStr.trim();
                if (cleanJson.startsWith("```json")) {
                    cleanJson = cleanJson.substring(7);
                } else if (cleanJson.startsWith("```")) {
                    cleanJson = cleanJson.substring(3);
                }
                if (cleanJson.endsWith("```")) {
                    cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
                }
                cleanJson = cleanJson.trim();
                
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(cleanJson).getAsJsonObject();
                if (!root.has("intent")) return;
                
                String intent = root.get("intent").getAsString();
                com.google.gson.JsonObject data = root.has("data") ? root.get("data").getAsJsonObject() : new com.google.gson.JsonObject();
                
                if ("ADD_TRANSACTION".equalsIgnoreCase(intent)) {
                    double amount = data.has("amount") ? data.get("amount").getAsDouble() : 0;
                    String categoryName = data.has("category") && !data.get("category").getAsString().trim().isEmpty() ? data.get("category").getAsString() : "Uncategorized";
                    String type = data.has("type") ? data.get("type").getAsString() : "expense";
                    String name = data.has("name") && !data.get("name").getAsString().trim().isEmpty() ? data.get("name").getAsString() : "AI Transaction";
                    
                    if (amount > 0 && !type.isEmpty()) {
                        org.example.models.TransactionCategory targetCat = null;
                        java.util.List<org.example.models.TransactionCategory> cats = org.example.utils.SqlUtil.getAllTransactionCategoriesByUser(user);
                        if (cats != null) {
                            for(org.example.models.TransactionCategory c : cats) {
                                if(c.getCategoryName().equalsIgnoreCase(categoryName)) {
                                    targetCat = c;
                                    break;
                                }
                            }
                        }
                        
                        if (targetCat == null) {
                            com.google.gson.JsonObject catJson = new com.google.gson.JsonObject();
                            catJson.addProperty("categoryName", categoryName);
                            catJson.addProperty("categoryColor", "1E293B");
                            com.google.gson.JsonObject catUserObj = new com.google.gson.JsonObject();
                            catUserObj.addProperty("id", user.getId());
                            catJson.add("user", catUserObj);
                            org.example.utils.SqlUtil.postTransactionCategory(catJson);
                            
                            cats = org.example.utils.SqlUtil.getAllTransactionCategoriesByUser(user);
                            if (cats != null) {
                                for(org.example.models.TransactionCategory c : cats) {
                                    if(c.getCategoryName().equalsIgnoreCase(categoryName)) {
                                        targetCat = c;
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if (targetCat != null) {
                            com.google.gson.JsonObject tJson = new com.google.gson.JsonObject();
                            tJson.addProperty("transactionName", name);
                            tJson.addProperty("transactionAmount", amount);
                            tJson.addProperty("transactionType", type.toLowerCase());
                            String date = data.has("date") && !data.get("date").getAsString().isEmpty() ? data.get("date").getAsString() : java.time.LocalDate.now().toString();
                            tJson.addProperty("transactionDate", date);
                            
                            com.google.gson.JsonObject tCatObj = new com.google.gson.JsonObject();
                            tCatObj.addProperty("id", targetCat.getId());
                            tJson.add("transactionCategory", tCatObj);
                            
                            com.google.gson.JsonObject tUserObj = new com.google.gson.JsonObject();
                            tUserObj.addProperty("id", user.getId());
                            tJson.add("user", tUserObj);
                            
                            org.example.utils.SqlUtil.postTransaction(tJson);
                        }
                        
                        fetchUserData(); // Refresh dashboard
                        view.getAiStatusLabel().setText("Transaction added automatically!");
                    }
                } else if ("NAVIGATION".equalsIgnoreCase(intent)) {
                    String target = data.has("target") ? data.get("target").getAsString() : "";
                    if ("BUDGETS".equalsIgnoreCase(target)) {
                        view.getViewBudgetProgressMenuItem().fire();
                    } else if ("CATEGORIES".equalsIgnoreCase(target)) {
                        view.getViewCategoriesMenuItem().fire();
                    } else if ("GOALS".equalsIgnoreCase(target)) {
                        view.getViewGoalsMenuItem().fire();
                    } else if ("REPORT".equalsIgnoreCase(target)) {
                        view.getExportDataMenuItem().fire();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Failed to parse intent: " + intentJsonStr);
            }
        });
    }

    private void showAboutUs() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        
        alert.setTitle("Project Information");
        alert.setHeaderText("Finvora - Finance Tracker"); 

        String content = 
            "PROJECT IDENTITY:\n" +
            "Finvora - Finance Tracker\n\n" +

            "PROJECT SUMMARY:\n" +
            "A modern desktop application built on JavaFX for local expense tracking, budgeting, and goal setting. Features a responsive, dual-theme user interface.\n\n" +
            
            "SOURCE CODE LINK:\n" +
            "Repository: https://github.com/yuvanvishnupandi/finance_tracker_java\n" +
            "Profile: https://github.com/yuvanvishnupandi/\n\n" +
            
            "IMPLEMENTATION:\n" +
            "Sole Developer & Lead Implementer: Yuvan Vishnu Pandi (Lead, Backend, Frontend)\n" +
            "All application logic, features (Theme Toggle, Budgeting, Currency), and UI implementation were completed solely by the developer.\n\n" +
            
            "FORMAL GROUP ACKNOWLEDGMENT (For Submission):\n" +
            "1. Naveen Karthick (Backend Tester & Consultation)\n" +
            "2. Sundar Dinesh (Frontend Tester)\n" +
            "3. Yashawini (Frontend Tester)\n";

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        alert.getDialogPane().setContent(textArea);
        
        alert.showAndWait();
    }

    private void generatePdfReport(LocalDate start, LocalDate end, File outFile) throws IOException {
        List<Integer> years = SqlUtil.getAllDistinctYears(user.getId());
        List<Transaction> all = new ArrayList<>();
        for (Integer y : years) {
            List<Transaction> ylist = SqlUtil.getAllTransactionsByUserId(user.getId(), y, null);
            if (ylist != null) all.addAll(ylist);
        }

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : all) {
            LocalDate d = t.getTransactionDate();
            if (start != null && d.isBefore(start)) continue;
            if (end != null && d.isAfter(end)) continue;
            filtered.add(t);
        }

        BigDecimal inc = BigDecimal.ZERO, exp = BigDecimal.ZERO;
        for (Transaction t : filtered) {
            BigDecimal amt = BigDecimal.valueOf(t.getTransactionAmount());
            if ("income".equalsIgnoreCase(t.getTransactionType())) inc = inc.add(amt);
            else exp = exp.add(amt);
        }
        BigDecimal bal = inc.subtract(exp);

        List<Budget> budgets = BudgetStore.getBudgets(user.getId());
        budgets.forEach(b -> b.setSpentAmount(calculateSpentFor(b)));

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;
            float x = margin;

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            y = drawText(cs, x, y, PDType1Font.HELVETICA_BOLD, 16,
                    "Finvora - Finance Tracker Report – " + (user != null ? user.getEmail() : ""));
            y = drawText(cs, x, y - 6, PDType1Font.HELVETICA, 10,
                    "Date range: " + (start == null ? "All" : start.toString())
                            + " to " + (end == null ? "All" : end.toString()));

            y = drawText(cs, x, y - 16, PDType1Font.HELVETICA_BOLD, 12, "Summary");
            y = drawText(cs, x, y - 12, PDType1Font.HELVETICA, 11, "Total Income: ₹" + inc.setScale(2, RoundingMode.HALF_UP));
            y = drawText(cs, x, y - 12, PDType1Font.HELVETICA, 11, "Total Expense: ₹" + exp.setScale(2, RoundingMode.HALF_UP));
            y = drawText(cs, x, y - 12, PDType1Font.HELVETICA, 11, "Balance: ₹" + bal.setScale(2, RoundingMode.HALF_UP));

            y = drawText(cs, x, y - 16, PDType1Font.HELVETICA_BOLD, 12, "Budgets");
            for (Budget b : budgets) {
                if (y < margin + 60) {
                    cs.close();
                    page = new PDPage(PDRectangle.LETTER);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - margin;
                }
                String line = String.format("%s | %s | Limit: ₹%s | Spent: ₹%s | Remaining: ₹%s | %s",
                        b.getCategory(),
                        b.getPeriodLabel(),
                        money(b.getLimitAmount()),
                        money(b.getSpentAmount()),
                        money(b.getRemaining()),
                        b.getSpentAmount().compareTo(b.getLimitAmount() == null ? BigDecimal.ZERO : b.getLimitAmount()) > 0
                                ? "Limit exceeded" : "OK");
                y = drawText(cs, x, y - 12, PDType1Font.HELVETICA, 10, line);
            }

            y = drawText(cs, x, y - 16, PDType1Font.HELVETICA_BOLD, 12, "Transactions");
            y = drawText(cs, x, y - 12, PDType1Font.HELVETICA_BOLD, 10,
                    "Date           Category          Type     Amount        Name");

            int printed = 0;
            for (Transaction t : filtered) {
                if (y < margin + 60) {
                    cs.close();
                    page = new PDPage(PDRectangle.LETTER);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - margin;
                    y = drawText(cs, x, y, PDType1Font.HELVETICA_BOLD, 10,
                            "Date           Category          Type     Amount        Name");
                }
                String category = t.getTransactionCategory() == null ? "" : t.getTransactionCategory().getCategoryName();
                String line = String.format("%-14s %-18s %-8s ₹%-12s %s",
                        t.getTransactionDate(),
                        cut(category, 18),
                        cut(t.getTransactionType(), 8),
                        BigDecimal.valueOf(t.getTransactionAmount()).setScale(2, RoundingMode.HALF_UP),
                        cut(t.getTransactionName(), 40));
                y = drawText(cs, x, y - 12, PDType1Font.HELVETICA, 9, line);
                printed++;
                if (printed >= 500) break;
            }

            cs.close();
            doc.save(outFile);
        }
    }

    private float drawText(PDPageContentStream cs, float x, float y, PDType1Font font, int size, String text)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y;
    }

    private String money(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String cut(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1);
    }

    private void pickActiveBudgetFromStore() {
        List<Budget> list = BudgetStore.getBudgets(user.getId());
        if (list.isEmpty()) { currentBudget = null; return; }
        LocalDate now = LocalDate.now();
        for (int i = list.size() - 1; i >= 0; i--) {
            Budget b = list.get(i);
            if (b.getPeriodType() == Budget.PeriodType.MONTHLY
                    && b.getYear() == now.getYear()
                    && b.getMonth() != null && b.getMonth() == now.getMonthValue()) { currentBudget = b; return; }
            if (b.getPeriodType() == Budget.PeriodType.QUARTERLY
                    && b.getYear() == now.getYear()
                    && b.getQuarter() != null && quarterOf(now.getMonthValue()) == b.getQuarter()) { currentBudget = b; return; }
            if (b.getPeriodType() == Budget.PeriodType.YEARLY && b.getYear() == now.getYear()) { currentBudget = b; return; }
        }
        currentBudget = list.get(list.size() - 1);
    }

    private int quarterOf(int m) { return (m - 1) / 3 + 1; }

    private BigDecimal calculateSpentFor(Budget b) {
        if (b == null) return BigDecimal.ZERO;
        BigDecimal spent = BigDecimal.ZERO;
        switch (b.getPeriodType()) {
            case MONTHLY -> spent = spent.add(sumCategoryExpenses(
                    SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), b.getMonth()),
                    b.getCategory()));
            case QUARTERLY -> {
                int start = ((b.getQuarter() == null ? 1 : b.getQuarter()) - 1) * 3 + 1;
                for (int m = start; m <= start + 2; m++)
                    spent = spent.add(sumCategoryExpenses(
                            SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), m),
                            b.getCategory()));
            }
            case YEARLY -> spent = spent.add(sumCategoryExpenses(
                    SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), null),
                    b.getCategory()));
        }
        return spent.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumCategoryExpenses(List<Transaction> tx, String categoryName) {
        BigDecimal sum = BigDecimal.ZERO;
        if (tx == null || categoryName == null) return sum;
        String wanted = categoryName.trim();
        for (Transaction t : tx) {
            if (!"expense".equalsIgnoreCase(t.getTransactionType())) continue;
            if (t.getTransactionCategory() == null) continue;
            String cat = t.getTransactionCategory().getCategoryName();
            if (cat != null && cat.trim().equalsIgnoreCase(wanted)) {
                sum = sum.add(BigDecimal.valueOf(t.getTransactionAmount()));
            }
        }
        return sum;
    }

    private void addGoal() {
        Optional<SavingsGoal> res = new CreateGoalDialog().showAndWait();
        res.ifPresent(goal -> {
            GoalStore.add(user.getId(), goal);
            new Alert(Alert.AlertType.INFORMATION, "Goal saved successfully!").showAndWait();
            refreshGoalWidget();
        });
    }

    private void seeGoals() {
        List<SavingsGoal> goals = GoalStore.getGoals(user.getId());
        new ViewGoalsDialog(goals, user.getId()).showAndWait();
        refreshGoalWidget();
    }

    private void refreshGoalWidget() {
        List<SavingsGoal> goals = GoalStore.getGoals(user.getId());
        
        if (goals == null || goals.isEmpty()) {
            view.getTopGoalNameLabel().setText("Check Menu to Set Goal");
            view.getTopGoalProgressBar().setProgress(0); 
            
            view.getTopGoalNameLabel().getStyleClass().add("empty-goal-state"); 
            return;
        }
        
        SavingsGoal g = goals.get(0);
        
        view.getTopGoalNameLabel().getStyleClass().removeAll("empty-goal-state");
        
        double p = 0;
        try {
            if (g.getTargetAmount() != null && g.getTargetAmount().doubleValue() > 0)
                p = g.getCurrentAmount().divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception ignored) {}
        
        view.getTopGoalNameLabel().setText("🏆 " + g.getName()); 
        view.getTopGoalProgressBar().setProgress(Math.min(1.0, p));
    }

    public User getUser() { return user; }
    public int getCurrentYear() { return currentYear; }
}
