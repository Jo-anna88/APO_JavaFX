package sample;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.AnchorPane;

import static sample.Controller.histo;

public class HistogramRGB {
    @FXML
    private StackPane stackPane;
    private Histogram histogram;
    BarChart<String, Number> histogramChannelRed;
    BarChart<String, Number> histogramChannelGreen;
    BarChart<String, Number> histogramChannelBlue;
    BarChart<String, Number> histogramIntensity;
    @FXML
    private AnchorPane anchorPane;
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

    public HistogramRGB(){
    }

    public void showAllChannels(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().addAll(histogramChannelRed,histogramChannelGreen,histogramChannelBlue);
        if(anchorPane.isVisible()) anchorPane.setVisible(false);
        }

    public void showRedChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramChannelRed);
        if(!anchorPane.isVisible()) anchorPane.setVisible(true);
        min.setText(String.valueOf(histogram.findMinimumIntensity(histogram.getRed())));
        max.setText(String.valueOf(histogram.findMaximumIntensity(histogram.getRed())));

    }

    public void showGreenChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramChannelGreen);
        if(!anchorPane.isVisible()) anchorPane.setVisible(true);
        min.setText(String.valueOf(histogram.findMinimumIntensity(histogram.getGreen())));
        max.setText(String.valueOf(histogram.findMaximumIntensity(histogram.getGreen())));
    }

    public void showBlueChannel(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramChannelBlue);
        if(!anchorPane.isVisible()) anchorPane.setVisible(true);
        min.setText(String.valueOf(histogram.findMinimumIntensity(histogram.getBlue())));
        max.setText(String.valueOf(histogram.findMaximumIntensity(histogram.getBlue())));
    }

    public void showIntensity(MouseEvent mouseEvent) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(histogramIntensity);
        if(!anchorPane.isVisible()) anchorPane.setVisible(true);
        min.setText(String.valueOf(histogram.findMinimumIntensity(histogram.getIntensity())));
        max.setText(String.valueOf(histogram.findMaximumIntensity(histogram.getIntensity())));
    }

    @FXML
    public void initialize() {
        histogram = histo; //histo = Controller.histo
        histogramChannelRed = histogram.setHistogramChannel(histogram.getRed());
        histogramChannelRed.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
        histogramChannelGreen = histogram.setHistogramChannel(histogram.getGreen());
        histogramChannelGreen.getStyleClass().add("histogramChannelGreen");
        histogramChannelBlue = histogram.setHistogramChannel(histogram.getBlue());
        histogramChannelBlue.getStyleClass().add("histogramChannelBlue");
        histogramIntensity = histogram.setHistogramChannel(histogram.getIntensity());
        stackPane.getChildren().addAll(histogramChannelRed,histogramChannelGreen,histogramChannelBlue);
        pixels.setText(String.valueOf(Functionality.pixelsAmount(histogram.getImage())));
    }
}
