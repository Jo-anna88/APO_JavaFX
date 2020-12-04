package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class OtsuThreshold {
    private Image originalImage;
    private Image destinationImage;
    private Mat src;
    @FXML
    private ImageView imageViewL;
    @FXML
    private ImageView imageViewR;


    @FXML
    public void initialize() {
        originalImage = Controller.returnSelectedImage();
        imageViewL.setImage(originalImage);
        try {
            File f = new File(new URL(originalImage.getUrl()).toURI());
            String filepath = f.getAbsolutePath();
            src = Imgcodecs.imread(filepath, 0);
            //https://www.desmos.com/matrix?lang=pl

        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("musisz pracować na zapisanym pliku");
        }
        //Otsu's thresholding after Gaussian filtering
        Mat blur= new Mat(src.rows(), src.cols(), src.type());
        Imgproc.GaussianBlur(src,blur,new Size(5,5),0);
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.threshold(blur,dst,0,255,Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
        java.awt.Image destImg = HighGui.toBufferedImage(dst);
        destinationImage = SwingFXUtils.toFXImage((BufferedImage) destImg, null); //writable image
        imageViewR.setImage(destinationImage);
    }


    @FXML
    void saveDestinationImage(ActionEvent event) {
        Functionality.save(originalImage,destinationImage);
    }
}
