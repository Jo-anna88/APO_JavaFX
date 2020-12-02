package sample;

import javafx.beans.value.ChangeListener;
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

public class Prewitt {
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
    private ImageView imageMatrix4;
    private ImageView imageMatrix5;
    private ImageView imageMatrix6;
    private ImageView imageMatrix7;
    private ImageView imageMatrix8;

    @FXML
    private ToggleGroup group;
    @FXML
    private ToggleButton togBtnN;
    @FXML
    private ToggleButton togBtnNE;
    @FXML
    private ToggleButton togBtnNW;
    @FXML
    private ToggleButton togBtnS;
    @FXML
    private ToggleButton togBtnSE;
    @FXML
    private ToggleButton togBtnSW;
    @FXML
    private ToggleButton togBtnE;
    @FXML
    private ToggleButton togBtnW;
    private int matrixNr=1; //to get matrix number
    private String prewN = new File("./resources/PrewittN.PNG").toURI().toURL().toString();
    private String prewNE = new File("./resources/PrewittNE.PNG").toURI().toURL().toString();
    private String prewNW = new File("./resources/PrewittNW.PNG").toURI().toURL().toString();
    private String prewS = new File("./resources/PrewittS.PNG").toURI().toURL().toString();
    private String prewSE = new File("./resources/PrewittSE.PNG").toURI().toURL().toString();
    private String prewSW = new File("./resources/PrewittSW.PNG").toURI().toURL().toString();
    private String prewE = new File("./resources/PrewittE.PNG").toURI().toURL().toString();
    private String prewW = new File("./resources/PrewittW.PNG").toURI().toURL().toString();

    @FXML
    private ChoiceBox choiceBox; //to get border type
    private int choice;

    @FXML
    private Spinner valSpinner; //to get constant value
    @FXML
    private Label valLabel;
    private int value;

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
        //destinationImage = Functionality.laplasjan(src, matrixNr, choice, value);
        //imageViewR.setImage(destinationImage);
        /////////ustawienie ikon do toggle buttons/////////
        imageMatrix1 = new ImageView(new Image(prewN));
        imageMatrix2 = new ImageView(new Image(prewNE));
        imageMatrix3 = new ImageView(new Image(prewE));
        imageMatrix4 = new ImageView(new Image(prewSE));
        imageMatrix5 = new ImageView(new Image(prewS));
        imageMatrix6 = new ImageView(new Image(prewSW));
        imageMatrix7 = new ImageView(new Image(prewW));
        imageMatrix8 = new ImageView(new Image(prewNW));

        togBtnN.setGraphic(imageMatrix1);
        togBtnN.setUserData(1);
        togBtnNE.setGraphic(imageMatrix2);
        togBtnNE.setUserData(2);
        togBtnE.setGraphic(imageMatrix3);
        togBtnE.setUserData(3);
        togBtnSE.setGraphic(imageMatrix4);
        togBtnSE.setUserData(4);
        togBtnS.setGraphic(imageMatrix5);
        togBtnS.setUserData(5);
        togBtnSW.setGraphic(imageMatrix6);
        togBtnSW.setUserData(6);
        togBtnW.setGraphic(imageMatrix7);
        togBtnW.setUserData(7);
        togBtnNW.setGraphic(imageMatrix8);
        togBtnNW.setUserData(8);
        ////////////////////////////////////////////////////
        group.selectedToggleProperty().addListener(
                (observable, old_val, new_val) -> {
                    matrixNr = (int) new_val.getUserData(); //uwaga! nr-y to 1,2,3,... (numeruje od 1!)
                    destinationImage = Functionality.prewitt(src,matrixNr,choice,value);
                    imageViewR.setImage(destinationImage);
                });
        //to get border type:
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
                    destinationImage = Functionality.prewitt(src,matrixNr,choice,value);
                    imageViewR.setImage(destinationImage);
                });
        //to get constant value:
        valSpinner.valueProperty().addListener((ChangeListener<Integer>) (obs, oldValue, newValue) -> {
            value=newValue;
            destinationImage = Functionality.prewitt(src,matrixNr,choice,value);
            imageViewR.setImage(destinationImage);
        });
    }

    public Prewitt() throws MalformedURLException {
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
}
