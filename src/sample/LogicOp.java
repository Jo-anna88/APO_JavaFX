package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;


public class LogicOp {
    private Mat src1;
    private Mat src2;
    private Image dst; //destinationImage
    @FXML
    private AnchorPane node;
    @FXML
    private ImageView imgView1;
    @FXML
    private ImageView imgView2;
    @FXML
    private ImageView imgViewResult;

    @FXML
    private ToggleGroup group;
    @FXML
    private JFXRadioButton btnAND;
    @FXML
    private JFXRadioButton btnOR;
    @FXML
    private JFXRadioButton btnXOR;
    private int selection;

    @FXML
    private JFXButton loadImg1;
    @FXML
    private JFXButton loadImg2;

    @FXML
    public void initialize() {
        loadImg1.setOnAction((actionEvent) -> {
            src1=loadImage();
            java.awt.Image img=HighGui.toBufferedImage(src1);
            WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
            imgView1.setImage(writableImage);
        });

        loadImg2.setOnAction((actionEvent) -> {
            src2=loadImage();
            java.awt.Image img=HighGui.toBufferedImage(src2);
            WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
            imgView2.setImage(writableImage);
        });

        btnAND.setUserData(0);
        btnOR.setUserData(1);
        btnXOR.setUserData(2);
        group.selectedToggleProperty().addListener(
                (observable, old_val, new_val) -> {
                    selection=(int)new_val.getUserData();
                    dst = Functionality.logicOp(src1,src2,selection);
                    imgViewResult.setImage(dst);
                });


    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(null,dst);
    }
    Mat loadImage() {
        FileChooser fileChooser = Controller.sFileChooser;
        File file = fileChooser.showOpenDialog(node.getScene().getWindow());
        String filepath = file.getAbsolutePath();
        Mat src = Imgcodecs.imread(filepath,Imgcodecs.IMREAD_COLOR);
        cvtColor(src,src,COLOR_BGR2GRAY);
        return src;
    }

}
