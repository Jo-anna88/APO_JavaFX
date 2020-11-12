package sample;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.StackPane;

import static sample.Controller.DISEqualHisto;
import static sample.Controller.histo;

public class HistogramEqualizationDISHE {
    @FXML
    private StackPane stackPaneL;
    @FXML
    private StackPane stackPaneR;

    Histogram histogram;
    Histogram DISEqualHistogram;
    BarChart<String, Number> histogramIntensity;
    BarChart<String, Number> histogramIntensity2;

    @FXML
    public void initialize() {
        stackPaneL.getChildren().add(histogramIntensity);
        stackPaneR.getChildren().add(histogramIntensity2);
    }

    public HistogramEqualizationDISHE() {
        histogram = histo;
        histogramIntensity = histogram.countHistogramChannel(histogram.getIntensity());
        DISEqualHistogram = DISEqualHisto;
        histogramIntensity2 = DISEqualHistogram.countHistogramChannel(DISEqualHistogram.getIntensity());
    }

}