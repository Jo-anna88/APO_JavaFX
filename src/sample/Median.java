package sample;

import com.jfoenix.controls.JFXButton;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Median {
    private Mat src; //originalImage
    private Mat dst; //destinationImage
    private int ksize; //rozmiar otoczenia mediany
    String[] ksizeChoice = {"3", "5", "7", "9"};
    //    private int[] ksizeChoice = {3,5,7,9};
//    private enum UzupBrzegow {CONSTANT, WRAP, REFLECT}
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    @FXML
    private ChoiceBox choiceBox1; //wielkość otoczenia
    @FXML
    private ChoiceBox choiceBox2; //metoda uzupełniania pikseli brzegowych
//    @FXML
//    private ChoiceBox<Integer> choiceBox1; //wielkość otoczenia
//    @FXML
//    private ChoiceBox<Enum> choiceBox2; //metoda uzupełniania pikseli brzegowych


    @FXML
    void saveDestinationImage(ActionEvent event) {
        //Functionality.save(originalImage,destinationImage);
    }

    @FXML
    public void initialize() {
        //originalImage = Controller.imageToMat(Controller.returnSelectedImage());

//        String file ="C:/EXAMPLES/OpenCV/sample.jpg";
//        Mat matrix = Imgcodecs.imread(file);

        Image originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath, Imgcodecs.IMREAD_COLOR);
            dst = new Mat(src.rows(), src.cols(), src.type());

//        choiceBox1 = new ChoiceBox<>();
//        ArrayList<Integer> arrayList = Arrays.stream(ksizeChoice)
//                .boxed()
//                .collect(Collectors.toCollection(ArrayList::new));

            choiceBox1.setItems(FXCollections.observableArrayList(ksizeChoice));
            choiceBox1.getSelectionModel().selectedIndexProperty().addListener(
                    (observable, old_val, new_val) -> {
                        ksize = Integer.parseInt(ksizeChoice[new_val.intValue()]);
                        Image destinationImage = Functionality.median(src, dst, ksize);
                        imageViewR.setImage(destinationImage);
                    });

            choiceBox2.setItems(FXCollections.observableArrayList("replicate", "reflect", "wrap", "constant", "set original"));
            choiceBox2.setValue("replicate"); //domyślne dla cv.medianBlur (BORDER_REPLICATE)
            choiceBox2.getSelectionModel().selectedIndexProperty().addListener(
                    (observable, old_val, new_val) -> {
                        Image destinationImage = Functionality.median(src,dst,ksize, new_val.intValue());
                        imageViewR.setImage(destinationImage);
                    });

//        choiceBox2 = new ChoiceBox<>();
//        choiceBox2.getItems().setAll(UzupBrzegow.values());


        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }


    }
}
