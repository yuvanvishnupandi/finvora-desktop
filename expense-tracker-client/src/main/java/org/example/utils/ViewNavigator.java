package org.example.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewNavigator {
    private static Stage mainStage;

    public static void setMainStage(Stage stage){
        mainStage = stage;
    }

    public static void switchViews(Scene scene){
        if(mainStage != null){
            mainStage.setScene(scene);
            mainStage.show();
        }
    }
}
