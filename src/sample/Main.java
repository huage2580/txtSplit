package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("TXT文件章节分割 - by 花生");
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
        Controller controller = loader.getController();
        controller.setStage(primaryStage); // or what you want to do
    }


    public static void main(String[] args) {
        launch(args);
    }
}
