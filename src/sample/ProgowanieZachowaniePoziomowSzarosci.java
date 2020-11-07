package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ProgowanieZachowaniePoziomowSzarosci {

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
            destinationImage = Functionality.progowanieZachowaniePoziomowSzarosci(originalImage,newValue.intValue());
            imageViewR.setImage(destinationImage);
        });
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
    }

}
