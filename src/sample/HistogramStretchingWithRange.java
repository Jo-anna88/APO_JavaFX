package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import javax.swing.*;

import static sample.Controller.histo;

public class HistogramStretchingWithRange {

    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    @FXML
    private StackPane stackPaneL;
    @FXML
    private StackPane stackPaneR;
    Histogram histogram;
    Histogram stretchHistogram2;
    BarChart<String, Number> histogramIntensity;
    BarChart<String, Number> histogramIntensity2;

    @FXML
    private Spinner spinnerInputFrom;
    @FXML
    private Spinner spinnerInputTo;
    @FXML
    private Spinner spinnerOutputFrom;
    @FXML
    private Spinner spinnerOutputTo;

    private Image originalImage;
    private Image destinationImage;

    int p1, p2, q1, q2;

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(Functionality.rgbToGrayscale(originalImage));
        histogram = histo;
        histogramIntensity = histogram.setHistogramChannelRGBtoGrayscale(histogram.getIntensity());
        stackPaneL.getChildren().add(histogramIntensity);
        histogramIntensity.setHorizontalZeroLineVisible(true);
        //histogramIntensity.setCursor(Cursor.DEFAULT);

        //lambda expression
        p1=Integer.parseInt(spinnerInputFrom.getValue().toString());
        p2=Integer.parseInt(spinnerInputTo.getValue().toString());
        q1=Integer.parseInt(spinnerOutputFrom.getValue().toString());
        q2=Integer.parseInt(spinnerOutputTo.getValue().toString());
        destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
        imageViewR.setImage(destinationImage);

        spinnerInputFrom.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            p1=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
            stretchHistogram2 = new Histogram(destinationImage);
            histogramIntensity2 = stretchHistogram2.setHistogramChannel(stretchHistogram2.getIntensity());
            stackPaneR.getChildren().clear();
            stackPaneR.getChildren().add(histogramIntensity2);
        });

        spinnerInputTo.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            p2=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
            stretchHistogram2 = new Histogram(destinationImage);
            histogramIntensity2 = stretchHistogram2.setHistogramChannel(stretchHistogram2.getIntensity());
            stackPaneR.getChildren().clear();
            stackPaneR.getChildren().add(histogramIntensity2);
        });

        spinnerOutputFrom.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            q1=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
            stretchHistogram2 = new Histogram(destinationImage);
            histogramIntensity2 = stretchHistogram2.setHistogramChannel(stretchHistogram2.getIntensity());
            stackPaneR.getChildren().clear();
            stackPaneR.getChildren().add(histogramIntensity2);
        });

        spinnerOutputTo.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            q2=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
            stretchHistogram2 = new Histogram(destinationImage);
            histogramIntensity2 = stretchHistogram2.setHistogramChannel(stretchHistogram2.getIntensity());
            stackPaneR.getChildren().clear();
            stackPaneR.getChildren().add(histogramIntensity2);
        });



    }

}
//anonymous class:
//spinnerInputFrom.valueProperty().addListener(new ChangeListener<Integer>() {
//@Override
//public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
//
//        }
//        });
