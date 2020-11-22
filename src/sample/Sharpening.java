package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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

    private String url1=new File("./resources/laplasjan1.PNG").toURI().toURL().toString();
    private String url2=new File("./resources/laplasjan2.PNG").toURI().toURL().toString();
    private String url3=new File("./resources/laplasjan3.PNG").toURI().toURL().toString();

    private int choice;
    private int size=1;

    public Sharpening() throws MalformedURLException {
    }

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath, Imgcodecs.IMREAD_COLOR);
        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
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
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
    @FXML
    void acceptSettings(ActionEvent event) {
        choice = (int) ToggleGroup.getSelectedToggle().getUserData();
        destinationImage = Functionality.laplasjan(src,size,choice);
        //imageViewR.setImage(destinationImage);
        event.consume();
    }

}
