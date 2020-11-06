package sample;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HistogramMono {
    @FXML
    private StackPane stackPane;
    public void showIntensity(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(Controller.returnSelectedImage().getHistogramIntensity());
    }
}