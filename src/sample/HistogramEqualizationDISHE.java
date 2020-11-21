package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import static sample.Controller.DISEqualHisto;
import static sample.Controller.equalHisto;
import static sample.Controller.histo;

public class HistogramEqualizationDISHE {
    @FXML
    private StackPane stackPaneL;
    @FXML
    private StackPane stackPaneR;
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;

    Histogram histogram;
    Histogram DISEqualHistogram;
    BarChart<String, Number> histogramIntensity;
    BarChart<String, Number> histogramIntensity2;
    Image originalImage, destinationImage;

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(Functionality.rgbToGrayscale(originalImage));
        destinationImage = DISEqualHisto.getImage();
        imageViewR.setImage(destinationImage);
        stackPaneL.getChildren().add(histogramIntensity);
        stackPaneR.getChildren().add(histogramIntensity2);
    }

    public HistogramEqualizationDISHE() {
        histogram = histo;
        histogramIntensity = histogram.setHistogramChannelRGBtoGrayscale(histogram.getIntensity());
        DISEqualHistogram = DISEqualHisto;
        histogramIntensity2 = DISEqualHistogram.setHistogramChannel(DISEqualHistogram.getIntensity());
    }
    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }

}