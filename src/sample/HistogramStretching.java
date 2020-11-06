package sample;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class HistogramStretching {
    @FXML
    private StackPane stackPaneL;

    @FXML
    private StackPane stackPaneR;

    @FXML
    public void initialize() {
        stackPaneL.getChildren().addAll(Controller.returnSelectedImage().getHistogramChannelRed(),Controller.returnSelectedImage().getHistogramChannelGreen(),Controller.returnSelectedImage().getHistogramChannelBlue());
        //stackPaneR.getChildren().addAll(Controller.getNimg().getHistogramChannelRed(),Controller.getNimg().getHistogramChannelGreen(),Controller.getNimg().getHistogramChannelBlue());
    }

}
