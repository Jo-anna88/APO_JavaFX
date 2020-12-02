package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class Sharpening {
    private Mat src; //originalImage
    //private Mat dst; //destinationImage
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    private Image originalImage;
    private Image destinationImage;
    private ImageView imageMatrix1;
    private ImageView imageMatrix2;
    private ImageView imageMatrix3;

    @FXML
    private ToggleGroup ToggleGroup;
    @FXML
    private ToggleButton toggleBtn1;
    @FXML
    private ToggleButton toggleBtn2;
    @FXML
    private ToggleButton toggleBtn3;
    private int matrixNr=1; //to get matrix number
    private String url1=new File("./resources/laplasjan1.PNG").toURI().toURL().toString();
    private String url2=new File("./resources/laplasjan2.PNG").toURI().toURL().toString();
    private String url3=new File("./resources/laplasjan3.PNG").toURI().toURL().toString();

    @FXML
    private ChoiceBox choiceBox; //to get border type
    private int choice;

    @FXML
    private Spinner valSpinner; //to get constant value
    @FXML
    private Label valLabel;
    private int value;

    public Sharpening() throws MalformedURLException {
    }

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath);
            cvtColor(src,src,COLOR_BGR2GRAY);
        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
        destinationImage = Functionality.laplasjan(src,matrixNr,choice,value);
        imageViewR.setImage(destinationImage);
        /////////ustawienie ikon do toggle buttons/////////
        imageMatrix1 = new ImageView(new Image(url1));
        imageMatrix2 = new ImageView(new Image(url2));
        imageMatrix3 = new ImageView(new Image(url3));
        imageMatrix1.setPreserveRatio(true);
        imageMatrix2.setPreserveRatio(true);
        imageMatrix3.setPreserveRatio(true);
        toggleBtn1.setGraphic(imageMatrix1);
        toggleBtn1.setUserData(1);
        toggleBtn2.setGraphic(imageMatrix2);
        toggleBtn2.setUserData(2);
        toggleBtn3.setGraphic(imageMatrix3);
        toggleBtn3.setUserData(3);
        ////////////////////////////////////////////////////
        ToggleGroup.selectedToggleProperty().addListener(
                (observable, old_val, new_val) -> {
                    matrixNr = (int) new_val.getUserData(); //uwaga! nr-y to 1,2,3 (numeruje od 1!)
                    destinationImage = Functionality.laplasjan(src,matrixNr,choice,value);
                    imageViewR.setImage(destinationImage);
                });

        choiceBox.setItems(FXCollections.observableArrayList("default", "reflect", "replicate", "constant", "leave original values"));
        choiceBox.setValue("default"); //domyślne dla blur, filter2D i gaussianBlur (BORDER_REFLECT_101)
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldVal, newVal) -> { //uwaga! numeracja od 0!
                    if (newVal.intValue()==3) {//constant
                        valLabel.setVisible(true);
                        valSpinner.setVisible(true);
                    }
                    else {
                        valLabel.setVisible(false);
                        valSpinner.setVisible(false);
                    }
                    choice=newVal.intValue();
                    destinationImage = Functionality.laplasjan(src,matrixNr,choice,value);
                    imageViewR.setImage(destinationImage);
                });
        //to get constant value:
        valSpinner.valueProperty().addListener((ChangeListener<Integer>) (obs, oldValue, newValue) -> {
            value=newValue;
            destinationImage = Functionality.laplasjan(src,matrixNr,choice,value);
            imageViewR.setImage(destinationImage);
        });

    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }

    /*
    @FXML
    void acceptSettings(ActionEvent event) {
        choice = (int) ToggleGroup.getSelectedToggle().getUserData();
        choiceBox.getSelectionModel().getSelectedItem();
        destinationImage = Functionality.laplasjan(src,size,choice);
        //imageViewR.setImage(destinationImage);
        event.consume();
    }
    */
}
