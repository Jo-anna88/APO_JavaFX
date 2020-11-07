package sample;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static sample.Controller.histo;

public class HistogramMono {
    @FXML
    private StackPane stackPane;
    private Histogram histogram;
    BarChart<String, Number> histogramIntensity;

    public HistogramMono() {
        histogram = histo;
        histogramIntensity = histogram.countHistogramChannel(histogram.getIntensity());
    }
    public void showIntensity(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramIntensity);
    }
    @FXML
    public void initialize() {
        stackPane.getChildren().addAll(histogramIntensity);
    }
}