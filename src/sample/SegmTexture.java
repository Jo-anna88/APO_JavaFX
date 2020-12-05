package sample;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class SegmTexture {
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    private Image originalImage;
    private Image destinationImage;
    private Mat src;
    @FXML
    private Spinner spinner; //to get threshold
    private double thresh;
    @FXML
    private ChoiceBox choiceBox3; //block size
    String[] ksizeChoice = {"3","5","7","9"};
    private int ksize;
    private int borderType = Core.BORDER_DEFAULT;

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath);
        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
        //destinationImage=Functionality.segmTexture(src,ksize,0,thresh);
        //to get matrix size:
        choiceBox3.setItems(FXCollections.observableArrayList(ksizeChoice));
        choiceBox3.getSelectionModel().selectedIndexProperty().addListener(
                (observable, old_val, new_val) -> {
                    ksize = Integer.parseInt(ksizeChoice[new_val.intValue()]);
                    destinationImage = Functionality.segmTexture(src,ksize,borderType,thresh);
                    imageViewR.setImage(destinationImage);
                });
        //to get threshold:
        spinner.valueProperty().addListener((ChangeListener<Double>) (obs, oldValue, newValue) -> {
            thresh=newValue;
            destinationImage = Functionality.segmTexture(src,ksize,borderType,thresh);
            imageViewR.setImage(destinationImage);
        });
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
}
