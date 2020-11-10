package sample;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.StackPane;

import static sample.Controller.histo;
import static sample.Controller.equalHisto;

public class HistogramEqualization {
    @FXML
    private StackPane stackPaneL;
    @FXML
    private StackPane stackPaneR;

    Histogram histogram;
    Histogram equalHistogram;
    BarChart<String, Number> histogramIntensity;
    BarChart<String, Number> histogramIntensity2;

    @FXML
    public void initialize() {
        stackPaneL.getChildren().add(histogramIntensity);
        stackPaneR.getChildren().add(histogramIntensity2);
    }

    public HistogramEqualization() {
        histogram = histo;
        histogramIntensity = histogram.countHistogramChannel(histogram.getIntensity());
        equalHistogram = equalHisto;
        histogramIntensity2 = equalHistogram.countHistogramChannel(equalHistogram.getIntensity());
    }

}