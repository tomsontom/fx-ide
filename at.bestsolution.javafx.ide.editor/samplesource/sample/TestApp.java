package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class TestApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(new Button("HELLO"), 800, 600));
		primaryStage.setTitle("FX-IDE");
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
