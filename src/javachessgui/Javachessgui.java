package javachessgui;

import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.*;

public class Javachessgui extends Application {
    
    public Gui gui;

    @Override
    public void start(Stage primaryStage) {

        init_app();

        primaryStage.setTitle("Chess GUI");

        Group root = new Group();

        gui = new Gui(primaryStage);

        root.getChildren().add(gui.horizontal_box);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        
        System.out.println("application started");
    }
       
    @Override
    public void stop() {

        gui.shutdown();
        
        System.out.println("application stopped");
    }

    private void init_app() {
        Board.init_class();
        
        System.out.println("application initialized");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
