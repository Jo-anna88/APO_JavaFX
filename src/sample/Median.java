package sample;

import com.jfoenix.controls.JFXButton;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
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

public class Median {
    private Image originalImage;
    private Image destinationImage;
    private Mat src; //originalImage
    private Mat dst; //destinationImage
    private int ksize; //rozmiar otoczenia mediany (aperture linear size; it must be odd and greater than 1, for example: 3, 5, 7 ...)
    String[] ksizeChoice = {"3", "5", "7", "9"};
    private int value; //gdy borderType==BORDER_CONSTANT
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    @FXML
    private ChoiceBox choiceBox1; //wielkość otoczenia (rozmiar matrycy filtru)
    @FXML
    private ChoiceBox choiceBox2; //metoda uzupełniania pikseli brzegowych
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Spinner spinner; //pobiera wartość CONSTANT Value (gdy BorderType=BORDER_CONSTANT)


    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }

    @FXML
    public void initialize() {
        //originalImage = Controller.imageToMat(Controller.returnSelectedImage());

//        String file ="C:/EXAMPLES/OpenCV/sample.jpg";
//        Mat matrix = Imgcodecs.imread(file);

        originalImage = Controller.returnSelectedImage();
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
                        if(choiceBox2.getValue().toString()=="replicate") {
                            destinationImage = Functionality.median(src, dst, ksize);
                            imageViewR.setImage(destinationImage);
                        }
                        else {
                            destinationImage = Functionality.median(src,dst,ksize,choiceBox2.getSelectionModel().selectedIndexProperty().intValue(),value);
                            imageViewR.setImage(destinationImage);
                        }
                    });

            choiceBox2.setItems(FXCollections.observableArrayList("replicate", "reflect", "wrap", "constant", "leave original values"));
            choiceBox2.setValue("replicate"); //domyślne dla cv.medianBlur (BORDER_REPLICATE)
            choiceBox2.getSelectionModel().selectedIndexProperty().addListener(
                    (observable, old_val, new_val) -> {
                        if (new_val.intValue()==3) //constant
                            {anchorPane.setVisible(true);}
                        else { anchorPane.setVisible(false);}

                        destinationImage = Functionality.median(src,dst,ksize,new_val.intValue(),value);
                        imageViewR.setImage(destinationImage);
                    });

            spinner.valueProperty().addListener((ChangeListener<Integer>) (obs, oldValue, newValue) -> {
                value=newValue;
                destinationImage = Functionality.median(src,dst,ksize,choiceBox2.getSelectionModel().selectedIndexProperty().intValue(),value);
                imageViewR.setImage(destinationImage);
            });

//        choiceBox2 = new ChoiceBox<>();
//        choiceBox2.getItems().setAll(UzupBrzegow.values());


        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }


    }
}
