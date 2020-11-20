package sample;

import com.jfoenix.controls.JFXButton;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ProgowanieBinarne {

    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;
    @FXML
    private Slider slider;
    @FXML
    private Label prog;

    private Image originalImage;
    private Image destinationImage;

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }

    @FXML
    public void initialize() {
        slider.setValue(40);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(10);
        prog.setText("40");
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            prog.setText(Integer.toString(newValue.intValue()));
            destinationImage = Functionality.progowanieBinarne(originalImage,newValue.intValue());
            imageViewR.setImage(destinationImage);
        });
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
    }
}
