package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Smoothing {
    private Mat src; //originalImage
    //private Mat dst; //destinationImage
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    private Image originalImage;
    private Image destinationImage;
    private ImageView imageMatrix;
    @FXML
    private ChoiceBox choiceBox;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField getK;
    @FXML
    private Label filtr;
    private String url;
    private int choice;
    private int size=3;
    private int K;

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath, Imgcodecs.IMREAD_COLOR);
            //dst = new Mat(src.rows(), src.cols(), src.type());
            //https://www.desmos.com/matrix?lang=pl

        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
        choiceBox.setItems(FXCollections.observableArrayList("uśrednianie", "uśrednianie K-pudełkowe", "gaussowskie"));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (observable, old_val, new_val) -> {
                    switch(new_val.intValue()){
                        case 0:
                            if(anchorPane.isVisible()) anchorPane.setVisible(false);
                            try {
                                choice=0;
                                url =  new File("./resources/usrednianie.PNG").toURI().toURL().toString();
                            } catch (MalformedURLException e) {
                            }
                            break;
                        case 1:
                            anchorPane.setVisible(true);
                            try {
                                choice=1;
                                url=new File("./resources/usrednianieK.png").toURI().toURL().toString();
                            } catch (MalformedURLException e) {
                            }
                            break;
                        case 2:
                            if(anchorPane.isVisible()) anchorPane.setVisible(false);
                            try {
                                choice=2;
                                url=new File("./resources/gauss.PNG").toURI().toURL().toString();
                            } catch (MalformedURLException e) {
                            }
                    }
                    imageMatrix = new ImageView(new Image(url));
                    imageMatrix.setPreserveRatio(true);
                    filtr.setGraphic(imageMatrix);
                });
        getK.textProperty().addListener(
                (observable,oldValue,newValue) -> {
                    K=Integer.parseInt(newValue);
        });
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
    @FXML
    void acceptSettings(ActionEvent event) {
        destinationImage = Functionality.smooth(src,size,choice,K);
        imageViewR.setImage(destinationImage);
        event.consume();
    }

}
