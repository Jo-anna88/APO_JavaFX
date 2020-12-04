package sample;

import com.jfoenix.controls.JFXRadioButton;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class Sobel_Canny {
    private Image originalImage;
    private Image destinationImage;
    private Mat src; //originalImage

    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;

    @FXML
    private ChoiceBox choiceBox; //metoda uzupełniania pikseli brzegowych
    private int choice;

    @FXML
    private ToggleGroup group; //wybór operatora (Sobel, Canny)
    private int operator;
    @FXML
    private JFXRadioButton sobelBtn;
    @FXML
    private JFXRadioButton cannyBtn;

    @FXML
    private ToggleGroup sobelGroup;
    @FXML
    private JFXRadioButton btnDefault;
    @FXML
    private JFXRadioButton btn10;
    @FXML
    private JFXRadioButton btn01;

    private int derivatives;

    @FXML
    private Spinner spinnerThreshold;
    @FXML
    private Label labelThreshold;
    private int threshold;
    private int ratio;

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }

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
        ratio=3;

        sobelBtn.setUserData(0);
        cannyBtn.setUserData(1);
        group.selectedToggleProperty().addListener(
                (observable, old_val, new_val) -> {
                    operator = (int) new_val.getUserData();
                    if (operator==0) {//Sobel
                        setAnchor(operator);
                        destinationImage = Functionality.sobel(src, derivatives, choice);
                    }
                    else {//Canny
                        setAnchor(operator);
                        destinationImage = Functionality.canny(src, threshold, ratio, choice);
                    }
                    imageViewR.setImage(destinationImage);
                });
        //to get dx and dy:
        btnDefault.setUserData(0);
        btn10.setUserData(1);
        btn01.setUserData(2);
        sobelGroup.selectedToggleProperty().addListener(
                (observable, oldVal, newVal) -> {
                    derivatives=(int) newVal.getUserData();
                    destinationImage = Functionality.sobel(src, derivatives, choice);
                    imageViewR.setImage(destinationImage);
                });

        //to get threshold:
        spinnerThreshold.valueProperty().addListener((ChangeListener<Integer>) (obs,oldValue,newValue) -> {
            threshold=newValue;
            destinationImage = Functionality.canny(src, threshold, ratio, choice);
            imageViewR.setImage(destinationImage);
        });

        //to get border type:
        choiceBox.setItems(FXCollections.observableArrayList("default", "reflect", "replicate", "leave original values"));
        choiceBox.setValue("default"); //domyślne dla blur, filter2D i gaussianBlur (BORDER_REFLECT_101)
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldVal, newVal) -> { //uwaga! numeracja od 0!
                    choice=newVal.intValue();
                    if (operator==0) //Sobel
                        destinationImage = Functionality.sobel(src, derivatives, choice);
                    else //Canny
                        destinationImage = Functionality.canny(src, threshold, ratio, choice);
                    imageViewR.setImage(destinationImage);
                });

    }

    void setAnchor(int v) {
        if (v==0) { //Sobel
            btn01.setVisible(true);
            btn10.setVisible(true);
            btnDefault.setVisible(true);
            btnDefault.setSelected(true);
            labelThreshold.setVisible(false);
            spinnerThreshold.setVisible(false);
        }
        else { //Canny
            btn01.setVisible(false);
            btn10.setVisible(false);
            btnDefault.setVisible(false);
            labelThreshold.setVisible(true);
            spinnerThreshold.setVisible(true);
        }
    }
}
