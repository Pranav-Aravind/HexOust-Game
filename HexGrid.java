import javafx.application.Application;
import javafx.stage.Stage;

public class HexGrid extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        GameMngr.startGame(stage);
    }

}
