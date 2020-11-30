package sample;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.opencv.core.Mat;
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
    private ChoiceBox choiceBox1; //pobiera metodę wygładzania (uśrednianie, uśrednianie K-pud, gauss)
    private int choiceOfSmoothingType; //indeks dot. wyboru z choicebox1 (typu wygładzania)
    @FXML
    private ChoiceBox choiceBox2; //pobiera sposób rozwiązania problemu wartości skrajnych pikseli (marginesów)
    private int choiceOfBorderType; //indeks dot. wuboru z choicebox2
    @FXML
    private ChoiceBox choiceBox3; //pobiera rozmiar filtra
    String[] ksizeChoice = {"3", "5", "7", "9"};
    private int size=3; //rozmiar macierzy filtra

    @FXML
    private Label valLabel;
    @FXML
    private Spinner valSpinner; //do pobrania wartości "constant" (przy typ_border=CONSTANT)
    private int value;

    @FXML
    private Label kLabel;

//  https://en.wikipedia.org/wiki/Box_blur - uśrednianie K-pudełkowe
    @FXML
    private Spinner kSpinner; //do pobrania wartości K (wagi dla środkowego piksela filtra)
    private int K;

    @FXML
    private Label filtr; //pokazuje macierz dla wybranego filtra
    private String url; //przechowuje url do obrazka macierzy wstawianego do ww. Label filtr

    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath, Imgcodecs.IMREAD_COLOR);
            //https://www.desmos.com/matrix?lang=pl

        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
        /////////////ustawienie startowego obrazu wynikowego - przy ustawieniach domyślnych///////////////
        destinationImage = Functionality.smooth(src,size,choiceOfSmoothingType,choiceOfBorderType,value,K);
        imageViewR.setImage(destinationImage);
        try {
            url =  new File("./resources/usrednianie.PNG").toURI().toURL().toString();
        } catch (MalformedURLException e) {
        }

        ////////ustawienia dla choicebox i spinner'ów - obrazy wynikowe dla ustawień wprowadzonych przez użytkownika/////
        choiceBox1.setItems(FXCollections.observableArrayList("Averaging", "Box Blurring", "Gaussian Blurring"));
        choiceBox1.setValue("Averaging");
        choiceBox1.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldVal, newVal) -> {
                    choiceOfSmoothingType=newVal.intValue();
                    switch(newVal.intValue()){
                        case 0:
                            if(kLabel.isVisible()) {
                                kLabel.setVisible(false);
                                kSpinner.setVisible(false);
                            }
                            try {
                                url =  new File("./resources/usrednianie.PNG").toURI().toURL().toString();
                            } catch (MalformedURLException e) {
                            }
                            break;
                        case 1:
                            if(!kLabel.isVisible()) {
                                kLabel.setVisible(true);
                                kSpinner.setVisible(true);
                            }
                            try {
                                url=new File("./resources/usrednianieK.png").toURI().toURL().toString();
                            } catch (MalformedURLException e) {
                            }
                            break;
                        case 2:
                            if(kLabel.isVisible()) {
                                kLabel.setVisible(false);
                                kSpinner.setVisible(false);
                            }
                            try {
                                url=new File("./resources/gauss.PNG").toURI().toURL().toString();
                            } catch (MalformedURLException e) {
                            }
                    }
                    imageMatrix = new ImageView(new Image(url));
                    imageMatrix.setPreserveRatio(true);
                    filtr.setGraphic(imageMatrix);
                    destinationImage = Functionality.smooth(src,size, choiceOfSmoothingType,choiceOfBorderType,value,K);
                    imageViewR.setImage(destinationImage);
                });

        choiceBox2.setItems(FXCollections.observableArrayList("default", "reflect", "replicate", "constant", "leave original values"));
        choiceBox2.setValue("default"); //domyślne dla blur, filter2D i gaussianBlur (BORDER_REFLECT_101)
        choiceBox2.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldVal, newVal) -> {
                    if (newVal.intValue()==3) {//constant
                        valLabel.setVisible(true);
                        valSpinner.setVisible(true);
                    }
                    else {
                        valLabel.setVisible(false);
                        valSpinner.setVisible(false);
                    }
                    choiceOfBorderType=newVal.intValue();
                    destinationImage = Functionality.smooth(src,size,choiceOfSmoothingType,choiceOfBorderType,value,K);
                    imageViewR.setImage(destinationImage);
                });

        //to get size:
        choiceBox3.setItems(FXCollections.observableArrayList(ksizeChoice));
        choiceBox3.setValue("3");
        choiceBox3.getSelectionModel().selectedIndexProperty().addListener(
                (observable, old_val, new_val) -> {
                    size = Integer.parseInt(ksizeChoice[new_val.intValue()]);
                    destinationImage = Functionality.smooth(src,size,choiceOfSmoothingType,choiceOfBorderType,value,K);
                    imageViewR.setImage(destinationImage);
                });

        //to get constant value:
        valSpinner.valueProperty().addListener((ChangeListener<Integer>) (obs, oldValue, newValue) -> {
            value=newValue;
            destinationImage = Functionality.smooth(src,size,choiceOfSmoothingType,choiceOfBorderType,value,K);
            imageViewR.setImage(destinationImage);
        });

        //to get K value:
        kSpinner.setPromptText("1");
        kSpinner.valueProperty().addListener((ChangeListener<Integer>) (obs, oldValue, newValue) -> {
            K=newValue;
            destinationImage = Functionality.smooth(src,size,choiceOfSmoothingType,choiceOfBorderType,value,K);
            imageViewR.setImage(destinationImage);
        });
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
//    @FXML
//    void acceptSettings(ActionEvent event) {
//        destinationImage = Functionality.smooth(src,size,choiceOfSmoothingType,choiceOfBorderType,value);
//        imageViewR.setImage(destinationImage);
//        event.consume();
//    }

}
