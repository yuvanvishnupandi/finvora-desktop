package org.example.dialogs;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.example.components.CategoryComponent;
import org.example.controllers.DashboardController;
import org.example.models.TransactionCategory;
import org.example.models.User;
import org.example.utils.SqlUtil;

import java.util.List;

public class ViewOrEditTransactionCategoryDialog extends CustomDialog{
    private DashboardController dashboardController;

    public ViewOrEditTransactionCategoryDialog(User user, DashboardController dashboardController){
        super(user);
        this.dashboardController = dashboardController;

        setTitle("View Categories");
        setWidth(1000);
        setHeight(700);

        ScrollPane mainContainer = createMainContainerContent();
        getDialogPane().setContent(mainContainer);
    }

    private ScrollPane createMainContainerContent(){
        VBox dialogVBox = new VBox(20);

        ScrollPane scrollPane = new ScrollPane(dialogVBox);
        scrollPane.setMinHeight(getHeight() - 40);
        scrollPane.setFitToWidth(true);

        List<TransactionCategory> transactionCategories = SqlUtil.getAllTransactionCategoriesByUser(user);
        for(TransactionCategory transactionCategory : transactionCategories){
            CategoryComponent categoryComponent = new CategoryComponent(dashboardController, transactionCategory);
            dialogVBox.getChildren().add(categoryComponent);
        }

        return scrollPane;
    }
}
