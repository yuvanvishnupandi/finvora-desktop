package org.example.views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.animations.LoadingAnimationPane;
import org.example.controllers.DashboardController;
import org.example.models.MonthlyFinance;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;
import org.example.utils.ThemeManager; 
import org.example.services.AIVoiceService;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;
import javafx.collections.FXCollections;

public class DashboardView {

    private Label topGoalNameLabel;
    private ProgressBar topGoalProgressBar;
    private String email;
    private LoadingAnimationPane loadingAnimationPane;
    private Label currentBalanceLabel, currentBalance;
    private Label totalIncomeLabel, totalIncome;
    private Label totalExpenseLabel, totalExpense;

    private Label budgetStatusLabel, budgetRemaining;

    private Label userNameLabel, userEmailLabel;
    private ImageView userIcon;
    private ToggleButton themeToggle; 
    private javafx.scene.layout.HBox mainContent; 

    private ComboBox<Integer> yearComboBox;
    private ComboBox<String> monthQuarterComboBox;

    public Button addTransactionButton, viewChartButton, scanReceiptButton;
    private VBox recentTransactionBox;
    private MenuBar menuBar;

    private MenuItem createCategoryMenuItem, viewCategoriesMenuItem, logoutMenuItem;
    private MenuItem exportDataMenuItem;
    private MenuItem generatePdfReportMenuItem;
    private MenuItem setMonthlyBudgetsMenuItem;
    private MenuItem viewBudgetProgressMenuItem;
    private MenuItem addGoalMenuItem;
    private MenuItem viewGoalsMenuItem;
    private MenuItem aboutUsMenuItem; 

    private MenuItem convertCurrencyMenuItem;

    private TableView<MonthlyFinance> transactionTable;
    public Button aiAlertsButton;
    private TableColumn<MonthlyFinance, String> monthColumn;
    private TableColumn<MonthlyFinance, BigDecimal> incomeColumn;
    private TableColumn<MonthlyFinance, BigDecimal> expenseColumn;

    private ToggleButton listenBtn;
    private Label aiStatusLabel;
    private TextArea aiAdviceArea;
    private TextField chatInput;
    private Button sendBtn;

    public DashboardView(String email) {
        this.email = email;
        loadingAnimationPane = new LoadingAnimationPane(Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);

        currentBalanceLabel = new Label("Current Balance:");
        totalIncomeLabel = new Label("Total Income:");
        totalExpenseLabel = new Label("Total Expense:");

        budgetStatusLabel = new Label("Budget Remaining:");
        budgetRemaining = new Label("₹0.00");

        currentBalance = new Label("₹0.00");
        totalIncome = new Label("₹0.00");
        totalExpense = new Label("₹0.00");
        addTransactionButton = new Button("+");
        scanReceiptButton = new Button("📷 Scan");

        monthQuarterComboBox = new ComboBox<>();

        userNameLabel = new Label("");
        userEmailLabel = new Label("");
        userNameLabel.getStyleClass().addAll("user-name-label");
        userEmailLabel.getStyleClass().addAll("user-email-label");

        userIcon = createUserIcon();
        
        themeToggle = new ToggleButton(); 
        themeToggle.getStyleClass().add("theme-toggle");
        
        createCategoryMenuItem = new MenuItem("Add Category");
        viewCategoriesMenuItem = new MenuItem("View Categories");
        exportDataMenuItem = new MenuItem("Export Data (CSV)");
        aiAlertsButton = new Button("🔔 AI Alerts (0)");
        aiAlertsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #111; -fx-font-weight: bold; -fx-cursor: hand;");
        generatePdfReportMenuItem = new MenuItem("Generate PDF Report");
        logoutMenuItem = new MenuItem("Logout");
        setMonthlyBudgetsMenuItem = new MenuItem("Set Monthly Budgets");
        viewBudgetProgressMenuItem = new MenuItem("View Budget Progress");
        addGoalMenuItem = new MenuItem("Add Goal");
        viewGoalsMenuItem = new MenuItem("View Goals");
        aboutUsMenuItem = new MenuItem("About Us"); 
        
        convertCurrencyMenuItem = new MenuItem("Convert Currency...");

        yearComboBox = new ComboBox<>();
        transactionTable = new TableView<>();
        recentTransactionBox = new VBox();
        topGoalNameLabel = new Label();
        topGoalProgressBar = new ProgressBar();
        viewChartButton = new Button("View Chart");
    }

    private ImageView createUserIcon() {
        String iconPath = "/images/userlogo.png";
        try {
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)));
            ImageView iv = new ImageView(iconImage);
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            return iv;
        } catch (Exception e) {
            System.err.println("Could not load user icon at: " + iconPath + ". Using empty ImageView.");
            return new ImageView();
        }
    }

    private ToggleButton voiceBtn;

    public void show() {
        Scene scene = createScene();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        
        VBox rootVBox = (VBox) ((StackPane) scene.getRoot()).getChildren().get(0);
        
        rootVBox.getStyleClass().removeAll("main-background", "main-background-dark");
        rootVBox.getStyleClass().add("main-background-light");
        themeToggle.setVisible(false);
        themeToggle.setManaged(false);
        
        ThemeManager.apply(scene);       
        new DashboardController(this);
        scene.widthProperty().addListener((observable, oldVal, newVal) -> {
            loadingAnimationPane.resizeWidth(newVal.doubleValue());
            resizeTableWidthColumns();
        });
        scene.heightProperty().addListener((observable, oldVal, newVal) ->
                loadingAnimationPane.resizeHeight(newVal.doubleValue()));
        ViewNavigator.switchViews(scene);
    }

    private Scene createScene() {
        StackPane rootStack = new StackPane();
        
        VBox rootVBox = new VBox();
        rootVBox.getStyleClass().add("main-background");
        
        javafx.scene.layout.HBox topMenuBar = createTopMenuBar();

        mainContent = new javafx.scene.layout.HBox();
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        mainContent.setStyle("-fx-background-color: transparent;");

        VBox mainContainerWrapper = new VBox();
        mainContainerWrapper.getStyleClass().add("dashboard-padding");
        VBox.setVgrow(mainContainerWrapper, Priority.ALWAYS);
        HBox.setHgrow(mainContainerWrapper, Priority.ALWAYS);
        
        HBox balanceSummaryBox = createBalanceSummaryBox();
        GridPane contentGridPane = createContentGridPane();
        VBox.setVgrow(contentGridPane, Priority.ALWAYS);
        mainContainerWrapper.getChildren().addAll(balanceSummaryBox, contentGridPane);

        ScrollPane scrollPane = new ScrollPane(mainContainerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setStyle("-fx-background-color: transparent;");

        mainContent.getChildren().add(scrollPane);
        rootVBox.getChildren().addAll(topMenuBar, mainContent);
        
        rootStack.getChildren().addAll(rootVBox, loadingAnimationPane);

        return new Scene(rootStack, Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);
    }

    private javafx.scene.layout.HBox createTopMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: transparent; -fx-padding: 5px;");

        Menu categoryMenu = new Menu("Categories");
        categoryMenu.getItems().addAll(createCategoryMenuItem, viewCategoriesMenuItem);

        Menu savingsMenu = new Menu("Savings Goals");
        savingsMenu.getItems().addAll(addGoalMenuItem, viewGoalsMenuItem);

        Menu budgetMenu = new Menu("Budgets");
        budgetMenu.getItems().addAll(setMonthlyBudgetsMenuItem, viewBudgetProgressMenuItem);

        Menu exportMenu = new Menu("Export / Reports");
        exportMenu.getItems().addAll(exportDataMenuItem, generatePdfReportMenuItem);

        Menu currencyMenu = new Menu("Currency Convert");
        currencyMenu.getItems().add(convertCurrencyMenuItem);
        
        Menu systemMenu = new Menu("System");
        systemMenu.getItems().addAll(aboutUsMenuItem, logoutMenuItem);

        menuBar.getMenus().addAll(categoryMenu, savingsMenu, budgetMenu, exportMenu, currencyMenu, systemMenu);
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, Priority.ALWAYS);
        
        themeToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #D4D4D4; -fx-cursor: hand; -fx-font-weight: bold;");
        
        javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox(menuBar, spacer, aiAlertsButton, themeToggle);
        topBar.getStyleClass().add("top-bar-background");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new javafx.geometry.Insets(0, 15, 0, 0));
        
        return topBar;
    }

    private HBox createBalanceSummaryBox() {
        HBox statBox = new HBox(40);
        statBox.setAlignment(Pos.CENTER);
        statBox.setStyle("-fx-padding: 20 0 40 0;");

        VBox balanceCard = new VBox(10);
        balanceCard.getStyleClass().addAll("stat-card", "card-balance");
        currentBalanceLabel.getStyleClass().setAll("stat-label");
        currentBalance.getStyleClass().setAll("stat-amount");
        balanceCard.getChildren().addAll(currentBalanceLabel, currentBalance);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);

        VBox incomeCard = new VBox(10);
        incomeCard.getStyleClass().addAll("stat-card", "card-income");
        totalIncomeLabel.getStyleClass().setAll("stat-label");
        totalIncome.getStyleClass().setAll("stat-amount");
        incomeCard.getChildren().addAll(totalIncomeLabel, totalIncome);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);

        VBox expenseCard = new VBox(10);
        expenseCard.getStyleClass().addAll("stat-card", "card-expense");
        totalExpenseLabel.getStyleClass().setAll("stat-label");
        totalExpense.getStyleClass().setAll("stat-amount");
        expenseCard.getChildren().addAll(totalExpenseLabel, totalExpense);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);

        VBox budgetCard = new VBox(10);
        budgetCard.getStyleClass().addAll("stat-card", "card-budget");
        budgetStatusLabel.getStyleClass().setAll("stat-label");
        budgetRemaining.getStyleClass().setAll("stat-amount");
        budgetCard.getChildren().addAll(budgetStatusLabel, budgetRemaining);
        HBox.setHgrow(budgetCard, Priority.ALWAYS);

        VBox topGoalCard = new VBox(8);
        topGoalCard.getStyleClass().addAll("stat-card", "card-savings");
        Label topGoalLabel = new Label("Savings Progress:");
        topGoalLabel.getStyleClass().add("stat-label");
        topGoalNameLabel = new Label("Check Savings Menu"); 
        topGoalNameLabel.getStyleClass().add("stat-amount");
        topGoalProgressBar = new ProgressBar(0.0);
        topGoalProgressBar.setPrefWidth(200);
        topGoalProgressBar.getStyleClass().add("progress-bar");
        topGoalCard.getChildren().addAll(topGoalLabel, topGoalNameLabel, topGoalProgressBar);
        HBox.setHgrow(topGoalCard, Priority.ALWAYS);

        statBox.getChildren().addAll(balanceCard, incomeCard, expenseCard, budgetCard, topGoalCard);
        return statBox;
    }

    private GridPane createContentGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setMinWidth(0); 

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setPercentWidth(55);
        leftCol.setMinWidth(0);
        
        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setPercentWidth(45);
        rightCol.setMinWidth(0);
        
        gridPane.getColumnConstraints().addAll(leftCol, rightCol);
        VBox transactionsTableSummaryBox = new VBox(20);
        transactionsTableSummaryBox.setMinWidth(0);

        HBox filterAndChartButtonBox = createFilterAndChartButtonBox();

        VBox transactionTableContentBox = createTransactionsTableContentBox();
        VBox.setVgrow(transactionTableContentBox, Priority.ALWAYS);
        transactionTableContentBox.setMinWidth(0);

        transactionsTableSummaryBox.getChildren().addAll(filterAndChartButtonBox, transactionTableContentBox);

        VBox recentTransactionsVBox = createRecentTransactionsVBox();
        recentTransactionsVBox.getStyleClass().addAll("field-background", "rounded-border", "padding-10px");
        GridPane.setVgrow(recentTransactionsVBox, Priority.ALWAYS);
        recentTransactionsVBox.setMinWidth(0);
        
        VBox rightColumn = new VBox(10, createAIVoicePlannerPanel(), recentTransactionsVBox);
        GridPane.setVgrow(rightColumn, Priority.ALWAYS);
        rightColumn.setMinWidth(0);

        gridPane.add(transactionsTableSummaryBox, 0, 0);
        gridPane.add(rightColumn, 1, 0);
        return gridPane;
    }

    private HBox createFilterAndChartButtonBox() {
        HBox hbox = new HBox(15);

        yearComboBox = new ComboBox<>();
        yearComboBox.getStyleClass().add("text-size-md");
        yearComboBox.setValue(Year.now().getValue());

        monthQuarterComboBox = new ComboBox<>();
        monthQuarterComboBox.getStyleClass().add("text-size-md");
        monthQuarterComboBox.setPromptText("Month/Quarter");

        viewChartButton = new Button("View Chart");
        viewChartButton.getStyleClass().addAll("field-background", "text-light-gray", "text-size-md");

        hbox.getChildren().addAll(yearComboBox, monthQuarterComboBox, viewChartButton);
        return hbox;
    }

    private VBox createTransactionsTableContentBox() {
        VBox vbox = new VBox();
        transactionTable = new TableView<>();
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        monthColumn = new TableColumn<>("Month");
        monthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
        monthColumn.getStyleClass().addAll("main-background", "text-size-md", "text-light-gray");
        
        incomeColumn = new TableColumn<>("Income");
        incomeColumn.setCellValueFactory(new PropertyValueFactory<>("income"));
        incomeColumn.getStyleClass().addAll("main-background", "text-size-md", "text-light-gray");
        
        expenseColumn = new TableColumn<>("Expense");
        expenseColumn.setCellValueFactory(new PropertyValueFactory<>("expense"));
        expenseColumn.getStyleClass().addAll("main-background", "text-size-md", "text-light-gray");
        
        transactionTable.getColumns().addAll(monthColumn, incomeColumn, expenseColumn);
        vbox.getChildren().add(transactionTable);
        // We can remove resizeTableWidthColumns() calls now that CONSTRAINED_RESIZE_POLICY is used, but keeping it is fine too.
        resizeTableWidthColumns();
        return vbox;
    }

    private ComboBox<String> recentFilterBox;

    private VBox createRecentTransactionsVBox() {
        VBox recentTransactionsVBox = new VBox();
        HBox labelAndButtonBox = new HBox(15);
        labelAndButtonBox.setAlignment(Pos.CENTER_LEFT);
        Label recentTransactionsLabel = new Label("Recent Transactions");
        recentTransactionsLabel.getStyleClass().addAll("text-size-lg", "text-light-gray");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        scanReceiptButton.getStyleClass().addAll("field-background", "text-size-md", "text-light-gray", "rounded-border");
        addTransactionButton.getStyleClass().addAll("field-background", "text-size-md", "text-light-gray", "rounded-border");
        labelAndButtonBox.getChildren().addAll(recentTransactionsLabel, spacer, scanReceiptButton, addTransactionButton);
        recentTransactionBox = new VBox(10);
        ScrollPane recentTransactionsScrollPane = new ScrollPane(recentTransactionBox);
        recentTransactionsScrollPane.setFitToWidth(true);
        recentTransactionsScrollPane.setFitToHeight(true);
        recentTransactionsVBox.getChildren().addAll(labelAndButtonBox, recentTransactionsScrollPane);
        return recentTransactionsVBox;
    }

    private VBox createAIVoicePlannerPanel() {
        VBox aiBox = new VBox(15);
        aiBox.getStyleClass().addAll("ui-card-squircle");
        
        Label titleLabel = new Label("Finvora AI Assistant");
        titleLabel.getStyleClass().addAll("text-size-lg", "text-light-gray");
        
        HBox headerBox = new HBox(15, titleLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        chatInput = new TextField();
        chatInput.setPromptText("Type your financial question or goal...");
        chatInput.getStyleClass().addAll("input-field");
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        sendBtn = new Button("Send");
        sendBtn.getStyleClass().addAll("primary-button");
        
        voiceBtn = new ToggleButton("🎤 Voice");
        voiceBtn.getStyleClass().addAll("toggle-button");
        
        HBox textInputBox = new HBox(10, chatInput, sendBtn, voiceBtn);
        textInputBox.setAlignment(Pos.CENTER_LEFT);

        aiStatusLabel = new Label("Ready.");
        aiStatusLabel.getStyleClass().addAll("stat-label");

        aiAdviceArea = new TextArea();
        aiAdviceArea.setPromptText("Your AI advisor's strategy will appear here...");
        aiAdviceArea.setWrapText(true);
        aiAdviceArea.setEditable(false);
        aiAdviceArea.setPrefRowCount(3);
        aiAdviceArea.getStyleClass().addAll("input-field");

        aiBox.getChildren().addAll(headerBox, textInputBox, aiStatusLabel, aiAdviceArea);

        return aiBox;
    }

    private void resizeTableWidthColumns() {
        Platform.runLater(() -> {
            double width = transactionTable.getWidth() * 0.335;
            monthColumn.setPrefWidth(width);
            incomeColumn.setPrefWidth(width);
            expenseColumn.setPrefWidth(width);
        });
    }

    public Label getBudgetStatusLabel() { return budgetStatusLabel; }
    public Label getBudgetRemaining() { return budgetRemaining; }
    public MenuBar getMenuBar() { return this.menuBar; }
    public MenuItem getCreateCategoryMenuItem() { return createCategoryMenuItem; }
    public MenuItem getViewCategoriesMenuItem() { return viewCategoriesMenuItem; }
    public MenuItem getExportDataMenuItem() { return exportDataMenuItem; }
    public MenuItem getGeneratePdfReportMenuItem() { return generatePdfReportMenuItem; }
    public MenuItem getLogoutMenuItem() { return logoutMenuItem; }
    public MenuItem getSetMonthlyBudgetsMenuItem() { return setMonthlyBudgetsMenuItem; }
    public MenuItem getViewBudgetProgressMenuItem() { return viewBudgetProgressMenuItem; }
    public MenuItem getAddGoalMenuItem() { return addGoalMenuItem; }
    public MenuItem getViewGoalsMenuItem() { return viewGoalsMenuItem; }
    public MenuItem getAboutUsMenuItem() { return aboutUsMenuItem; }
    public String getEmail() { return email; }
    public Button getAddTransactionButton() { return addTransactionButton; }
    public VBox getRecentTransactionBox() { return recentTransactionBox; }
    public LoadingAnimationPane getLoadingAnimationPane() { return loadingAnimationPane; }
    public TableView<MonthlyFinance> getTransactionTable() { return transactionTable; }
    public TableColumn<MonthlyFinance, String> getMonthColumn() { return monthColumn; }
    public TableColumn<MonthlyFinance, BigDecimal> getIncomeColumn() { return incomeColumn; }
    public TableColumn<MonthlyFinance, BigDecimal> getExpenseColumn() { return expenseColumn; }
    public ComboBox<Integer> getYearComboBox() { return yearComboBox; }
    public Label getCurrentBalance() { return currentBalance; }
    public ToggleButton getVoiceBtn() { return voiceBtn; }
    public Label getTotalIncome() { return totalIncome; }
    public Label getTotalExpense() { return totalExpense; }
    public Button getViewChartButton() { return viewChartButton; }
    public ComboBox<String> getMonthQuarterComboBox() { return monthQuarterComboBox; }
    
    public Label getUserNameLabel() { return userNameLabel; }
    public Label getUserEmailLabel() { return userEmailLabel; }
    
    public MenuItem getConvertCurrencyMenuItem() { return convertCurrencyMenuItem; }

    public Label getTopGoalNameLabel() { return topGoalNameLabel; }
    public ProgressBar getTopGoalProgressBar() { return topGoalProgressBar; }
    
    public ToggleButton getThemeToggle() { return themeToggle; }

    public Label getAiStatusLabel() { return aiStatusLabel; }
    public TextArea getAiAdviceArea() { return aiAdviceArea; }
    public TextField getChatInput() { return chatInput; }
    public Button getSendBtn() { return sendBtn; }
}
