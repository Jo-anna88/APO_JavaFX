package sample;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import static sample.Controller.histo;

public class HistogramMono {
    @FXML
    private StackPane stackPane;
    private Histogram histogram;
    BarChart<String, Number> histogramIntensity;
    @FXML
    private Label min;
    @FXML
    private Label max;
    @FXML
    private Label mean;
    @FXML
    private Label median;
    @FXML
    private Label stdDev;
    @FXML
    private Label pixels;

    public HistogramMono() {
    }
    public void showIntensity(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramIntensity);
    }
    @FXML
    public void initialize() {
        histogram = histo;
        histogramIntensity = histogram.setHistogramChannel(histogram.getIntensity());
        stackPane.getChildren().addAll(histogramIntensity);
        pixels.setText(String.valueOf(Functionality.pixelsAmount(histogram.getImage())));
        min.setText(String.valueOf(histogram.findMinimumIntensity(histogram.getIntensity())));
        max.setText(String.valueOf(histogram.findMaximumIntensity(histogram.getIntensity())));
    }
}