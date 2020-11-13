package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.*;

public class HistogramStretchingWithRange {

    @FXML
    private ImageView imageViewL;

    @FXML
    private ImageView imageViewR;

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

    }

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        //lambda expression
        p1=Integer.parseInt(spinnerInputFrom.getValue().toString());
        p2=Integer.parseInt(spinnerInputTo.getValue().toString());
        q1=Integer.parseInt(spinnerOutputFrom.getValue().toString());
        q2=Integer.parseInt(spinnerOutputTo.getValue().toString());
        destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);

        spinnerInputFrom.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            p1=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
        });

        spinnerInputTo.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            p2=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
        });

        spinnerOutputFrom.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            q1=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
        });

        spinnerOutputTo.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) -> {
            q2=newValue;
            destinationImage=Functionality.createImageAfterLinearHistogramStretchingWithRange(originalImage,p1,p2,q1,q2);
            imageViewR.setImage(destinationImage);
        });

        imageViewL.setImage(Functionality.rgbToGrayscale(originalImage));
    }

}
//anonymous class:
//spinnerInputFrom.valueProperty().addListener(new ChangeListener<Integer>() {
//@Override
//public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
//
//        }
//        });
