package sample;

import com.jfoenix.controls.JFXRadioButton;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import javafx.scene.control.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class SegmWatershed {
    private Image originalImage;
    private Image destinationImage;
    private Mat src;
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    /////////////////////////////////
    @FXML
    private ToggleGroup background;
    @FXML
    private JFXRadioButton dark;
    @FXML
    private JFXRadioButton light;
    private int backgroundChoice;
    /////////////////////////////////
    @FXML
    private ChoiceBox choiceBox;
    private int choice;
    /////////////////////////////////
    @FXML
    private Spinner spinnerThreshold;
    private double threshold;
    @FXML
    private Label labelThreshold;
    ////////////////////////////////
    @FXML
    private Spinner spinnerCannyThreshold;
    private int thresholdCanny;
    @FXML
    private Label labelCannyThreshold;
    @FXML private AnchorPane panel;


    //https://docs.opencv.org/master/d3/db4/tutorial_py_watershed.html - zaimplementowana metoda
    //https://docs.opencv.org/master/d4/d40/samples_2cpp_2watershed_8cpp-example.html#a36 - C++
    //https://www.pyimagesearch.com/2015/11/02/watershed-opencv/ - być może ten sposób jest lepszy (Python)
    //https://docs.opencv.org/3.4/d2/dbd/tutorial_distance_transform.html - Java
    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath);
            //https://www.desmos.com/matrix?lang=pl

        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
        //to get info about background color:
        dark.setUserData(0);
        light.setUserData(1);
        background.selectedToggleProperty().addListener(
                (observable, old_val, new_val) -> {
                    backgroundChoice = (int) new_val.getUserData();
//                    switch(choice) {
//                        case 0:
//                            destinationImage = Functionality.segmWatershed3(src,threshold,backgroundChoice);
//                            break;
//                        case 1:
//                            destinationImage = Functionality.segmWatershed(src,thresholdCanny,backgroundChoice);
//                            break;
//                        case 2:
//                            destinationImage = Functionality.segmWatershed(src,backgroundChoice);
//                            break;
//                        case 3:
//                            destinationImage = Functionality.segmWatershed2(src);
//                            break;
//                    }
//                    imageViewR.setImage(destinationImage);
                });
        //
        choiceBox.setItems(FXCollections.observableArrayList("mean-shift filter, Otsu, EDT", "Canny, find/draw contours", "Otsu, morph.op., EDT", "laplasjan, Otsu, EDT, ..."));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (observable, old_val, new_val) -> {
                    choice = new_val.intValue();
                    if (new_val.intValue()==0) {//mean-shift, Otsu, EDT, ...
                        labelCannyThreshold.setVisible(false);
                        spinnerCannyThreshold.setVisible(false);
                        labelThreshold.setVisible(true);
                        spinnerThreshold.setVisible(true);
                    }
                    else if (new_val.intValue()==1) {//Canny, find/draw contours
                        labelThreshold.setVisible(false);
                        spinnerThreshold.setVisible(false);
                        labelCannyThreshold.setVisible(true);
                        spinnerCannyThreshold.setVisible(true);
                    }
                    else {
                        labelThreshold.setVisible(false);
                        spinnerThreshold.setVisible(false);
                        labelCannyThreshold.setVisible(false);
                        spinnerCannyThreshold.setVisible(false);
                        if (new_val.intValue()==2) //Otsu, morphological op, EDT
                            destinationImage = Functionality.segmWatershed(src,backgroundChoice);
                        else //new_val...=3 // laplasjan, Otsu, EDT, ...
                            destinationImage = Functionality.segmWatershed2(src);
                        imageViewR.setImage(destinationImage);
                    }
                });
        //to get threshold:
        spinnerThreshold.valueProperty().addListener((ChangeListener<Double>) (obs, oldValue, newValue) -> {
            threshold=newValue;
            panel.getScene().setCursor(Cursor.WAIT);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Image destinationImage = Functionality.segmWatershed3(src, threshold, backgroundChoice);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageViewR.setImage(destinationImage);
                            panel.getScene().setCursor(Cursor.DEFAULT);
                        }
                    });
                }
            }).start();


        });
        //to get Canny threshold:
        spinnerCannyThreshold.valueProperty().addListener((ChangeListener<Integer>) (obs, oldValue, newValue) -> {
            thresholdCanny=newValue;
            destinationImage = Functionality.segmWatershed(src,thresholdCanny,backgroundChoice);
            imageViewR.setImage(destinationImage);
        });

    }


    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
}
