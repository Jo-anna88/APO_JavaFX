package sample;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class RedukcjaPoziomowSzarosci {

    @FXML
    private ImageView imageViewL;

    @FXML
    private ImageView imageViewR;

    @FXML
    private JFXComboBox combo_box;

    private Image originalImage, destinationImage;

    @FXML
    public void initialize() {
        // levels
        String[] levels = {"2", "3", "4", "8", "16", "32", "64", "128", "256"};
        combo_box.setItems(FXCollections.observableArrayList(levels));
        combo_box.valueProperty().addListener((observable, oldValue, newValue) -> {
            destinationImage = Functionality.redukcjaPoziomowSzarosci(originalImage, Integer.parseInt(newValue.toString()));
            imageViewR.setImage(destinationImage);
        });
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(Functionality.rgbToGrayscale(originalImage));
    }

    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
}
