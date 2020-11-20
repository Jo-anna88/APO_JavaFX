package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import static sample.Controller.histo;
import static sample.Controller.equalHisto;

public class HistogramEqualization {
    @FXML
    private StackPane stackPaneL;
    @FXML
    private StackPane stackPaneR;
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;

    Histogram histogram;
    Histogram equalHistogram;
    BarChart<String, Number> histogramIntensity;
    BarChart<String, Number> histogramIntensity2;
    Image originalImage, originalImageGrayscale, destinationImage;

    @FXML
    public void initialize() {
        stackPaneL.getChildren().add(histogramIntensity);
        stackPaneR.getChildren().add(histogramIntensity2);
        originalImage = histo.getImage();
        originalImageGrayscale = Functionality.rgbToGrayscale(histo.getImage());
        imageViewL.setImage(originalImageGrayscale);
        destinationImage = equalHisto.getImage();
        imageViewR.setImage(destinationImage);
    }

    public HistogramEqualization() {
        histogram = histo;
        histogramIntensity = histogram.setHistogramChannelRGBtoGrayscale(histogram.getIntensity());
        equalHistogram = equalHisto;
        histogramIntensity2 = equalHistogram.setHistogramChannel(equalHistogram.getIntensity());
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }

}