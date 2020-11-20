package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import static sample.Controller.histo;
//import static sample.Controller.stretchHisto;

public class HistogramStretching {
    @FXML
    private StackPane stackPaneL;
    @FXML
    private StackPane stackPaneR;
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;

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

    private Image originalImage;
    private Image destinationImage;

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        destinationImage = Functionality.createImageAfterLinearHistogramStretching(originalImage);
        imageViewR.setImage(destinationImage);

        histogram = histo;
        stretchHistogram = new Histogram(destinationImage);

        histogramChannelRed = histogram.setHistogramChannel(histogram.getRed()); //histo = Controller.histo
        histogramChannelRed.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
        histogramChannelGreen = histogram.setHistogramChannel(histogram.getGreen());
        histogramChannelGreen.getStyleClass().add("histogramChannelGreen");
        histogramChannelBlue = histogram.setHistogramChannel(histogram.getBlue());
        histogramChannelBlue.getStyleClass().add("histogramChannelBlue");
        histogramIntensity = histogram.setHistogramChannel(histogram.getIntensity());

        histogramChannelRed2 = stretchHistogram.setHistogramChannel(stretchHistogram.getRed()); //histo = Controller.histo
        histogramChannelRed2.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
        histogramChannelGreen2 = stretchHistogram.setHistogramChannel(stretchHistogram.getGreen());
        histogramChannelGreen2.getStyleClass().add("histogramChannelGreen");
        histogramChannelBlue2 = stretchHistogram.setHistogramChannel(stretchHistogram.getBlue());
        histogramChannelBlue2.getStyleClass().add("histogramChannelBlue");
        histogramIntensity2 = stretchHistogram.setHistogramChannel(stretchHistogram.getIntensity());

        if(histogram.getFlag()) {
            stackPaneL.getChildren().addAll(histogramChannelRed, histogramChannelGreen, histogramChannelBlue);
            stackPaneR.getChildren().addAll(histogramChannelRed2, histogramChannelGreen2, histogramChannelBlue2);
        }
        else {
            stackPaneL.getChildren().addAll(histogramIntensity);
            stackPaneR.getChildren().addAll(histogramIntensity2);
        }
    }

    public HistogramStretching() {
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }


}
