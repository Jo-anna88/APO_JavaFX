package sample;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class AdaptiveThreshold {
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    private Image originalImage;
    private Image destinationImage;
    private Mat src; //originalImage

    @FXML
    private ChoiceBox choiceBox1; //adaptive method
    private int adaptiveMethod;
    @FXML
    private ChoiceBox choiceBox2; //threshold type
    private int thresholdType;
    @FXML
    private ChoiceBox choiceBox3; //block size
    String[] ksizeChoice = {"3","5","7", "9","11","13","15","17","19","21","23","25"};
    private int blockSize;
    @FXML
    private Spinner spinner; //constant C - tolerancja progu, np. 2
    private double C;

//    public AdaptiveThreshold() {
//        for (int i=0, k=3; i<100; i++,k+=2) {
//            ksizeChoice[i]=Integer.toString(k);
//        }
//    }

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath);
            cvtColor(src,src,COLOR_BGR2GRAY); //8-bit single-channel image
        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
        //to get addaptive method:
        choiceBox1.setItems(FXCollections.observableArrayList("Mean", "Gaussian"));
        choiceBox1.getSelectionModel().selectedIndexProperty().addListener(
                (observable, old_val, new_val) -> {
                    adaptiveMethod = new_val.intValue();
                    destinationImage = Functionality.adaptiveThresh(src,adaptiveMethod,thresholdType,blockSize,C);
                    imageViewR.setImage(destinationImage);
                });
        //to get threshold type:
        choiceBox2.setItems(FXCollections.observableArrayList("Binary", "Binary Inv."));
        choiceBox2.getSelectionModel().selectedIndexProperty().addListener(
                (observable, old_val, new_val) -> {
                    thresholdType = new_val.intValue();
                    destinationImage = Functionality.adaptiveThresh(src,adaptiveMethod,thresholdType,blockSize,C);
                    imageViewR.setImage(destinationImage);
                });
        //to get block size:
        choiceBox3.setItems(FXCollections.observableArrayList(ksizeChoice));
        choiceBox3.getSelectionModel().selectedIndexProperty().addListener(
                (observable, old_val, new_val) -> {
                    blockSize = Integer.parseInt(ksizeChoice[new_val.intValue()]);
                    destinationImage = Functionality.adaptiveThresh(src,adaptiveMethod,thresholdType,blockSize,C);
                    imageViewR.setImage(destinationImage);
                });
        //to get C value:
        spinner.valueProperty().addListener((ChangeListener<Integer>) (obs, oldValue, newValue) -> {
            C=newValue;
            destinationImage = Functionality.adaptiveThresh(src,adaptiveMethod,thresholdType,blockSize,C);
            imageViewR.setImage(destinationImage);
        });

    }


    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
}
