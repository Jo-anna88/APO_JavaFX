package sample;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

import static sample.Controller.histo;
import static sample.Controller.stretchHisto;

public class HistogramStretching {
    @FXML
    private StackPane stackPaneL;

    @FXML
    private StackPane stackPaneR;
    Histogram histogram;
    Histogram stretchHistogram;
    BarChart<String, Number> histogramChannelRed;
    BarChart<String, Number> histogramChannelGreen;
    BarChart<String, Number> histogramChannelBlue;
    BarChart<String, Number> histogramIntensity;
    BarChart<String, Number> histogramChannelRed2;
    BarChart<String, Number> histogramChannelGreen2;
    BarChart<String, Number> histogramChannelBlue2;
    BarChart<String, Number> histogramIntensity2;

    @FXML
    public void initialize() {
        if(histogram.getFlag()) {
            stackPaneL.getChildren().addAll(histogramChannelRed, histogramChannelGreen, histogramChannelBlue);
            stackPaneR.getChildren().addAll(histogramChannelRed2, histogramChannelGreen2, histogramChannelBlue2);
        }
        stackPaneL.getChildren().addAll(histogramIntensity);
        stackPaneR.getChildren().addAll(histogramIntensity2);
    }
    public HistogramStretching() {
        histogram = histo;
        histogramChannelRed = histogram.countHistogramChannel(histogram.getRed()); //histo = Controller.histo
        histogramChannelRed.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
        histogramChannelGreen = histogram.countHistogramChannel(histogram.getGreen());
        histogramChannelGreen.getStyleClass().add("histogramChannelGreen");
        histogramChannelBlue = histogram.countHistogramChannel(histogram.getBlue());
        histogramChannelBlue.getStyleClass().add("histogramChannelBlue");
        histogramIntensity = histogram.countHistogramChannel(histogram.getIntensity());
        stretchHistogram = stretchHisto;
        histogramChannelRed2 = stretchHistogram.countHistogramChannel(stretchHistogram.getRed()); //histo = Controller.histo
        histogramChannelRed2.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
        histogramChannelGreen2 = stretchHistogram.countHistogramChannel(stretchHistogram.getGreen());
        histogramChannelGreen2.getStyleClass().add("histogramChannelGreen");
        histogramChannelBlue2 = stretchHistogram.countHistogramChannel(stretchHistogram.getBlue());
        histogramChannelBlue2.getStyleClass().add("histogramChannelBlue");
        histogramIntensity2 = stretchHistogram.countHistogramChannel(stretchHistogram.getIntensity());
    }


}
