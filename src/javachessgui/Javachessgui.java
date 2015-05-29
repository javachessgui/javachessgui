package javachessgui;

import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.*;

public class Javachessgui extends Application {

    @Override
    public void start(Stage primaryStage) {

        init_app();

        primaryStage.setTitle("Chess GUI");

        Group root = new Group();

        Board b = new Board();

        root.getChildren().add(b.vertical_box);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void init_app() {
        Board.init_class();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
