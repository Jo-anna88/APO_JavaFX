package sample;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HistogramRGB {
    @FXML
    private StackPane stackPane;

    public void showAllChannels(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().addAll(Controller.returnSelectedImage().getHistogramChannelRed(),Controller.returnSelectedImage().getHistogramChannelGreen(),Controller.returnSelectedImage().getHistogramChannelBlue());
        }

    public void showRedChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(Controller.returnSelectedImage().getHistogramChannelRed());
    }

    public void showGreenChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(Controller.returnSelectedImage().getHistogramChannelGreen());
    }

    public void showBlueChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(Controller.returnSelectedImage().getHistogramChannelBlue());
    }

    public void showIntensity(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(Controller.returnSelectedImage().getHistogramIntensity());
    }

    @FXML
    public void initialize() {
        stackPane.getChildren().addAll(Controller.returnSelectedImage().getHistogramChannelRed(),Controller.returnSelectedImage().getHistogramChannelGreen(),Controller.returnSelectedImage().getHistogramChannelBlue());;
    }
}
