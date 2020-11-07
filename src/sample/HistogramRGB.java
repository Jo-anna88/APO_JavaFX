package sample;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static sample.Controller.histo;

public class HistogramRGB {
    @FXML
    private StackPane stackPane;
    private Histogram histogram;
    BarChart<String, Number> histogramChannelRed;
    BarChart<String, Number> histogramChannelGreen;
    BarChart<String, Number> histogramChannelBlue;
    BarChart<String, Number> histogramIntensity;

    public HistogramRGB(){
        histogram = histo;
        histogramChannelRed = histogram.countHistogramChannel(histogram.getRed()); //histo = Controller.histo
        histogramChannelRed.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
        histogramChannelGreen = histogram.countHistogramChannel(histogram.getGreen());
        histogramChannelGreen.getStyleClass().add("histogramChannelGreen");
        histogramChannelBlue = histogram.countHistogramChannel(histogram.getBlue());
        histogramChannelBlue.getStyleClass().add("histogramChannelBlue");
        histogramIntensity = histogram.countHistogramChannel(histogram.getIntensity());
    }

    public void showAllChannels(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().addAll(histogramChannelRed,histogramChannelGreen,histogramChannelBlue);
        }

    public void showRedChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramChannelRed);
    }

    public void showGreenChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramChannelGreen);
    }

    public void showBlueChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramChannelBlue);
    }

    public void showIntensity(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramIntensity);
    }

    @FXML
    public void initialize() {
        stackPane.getChildren().addAll(histogramChannelRed,histogramChannelGreen,histogramChannelBlue);
    }
}
