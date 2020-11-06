package sample;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;


public class Functionality {
    static PixelReader pixelReader;
    static PixelWriter pixelWriter;
    static WritableImage writableImage;

    public static Image rgbToGrayscale(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        pixelReader = image.getPixelReader();
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int i = (int) (.299 * r + .587 * g + 0.114 * b);
                int nargb = (a << 24) | (i << 16) | (i << 8) | i;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }

    public static Image createImageAfterLinearHistogramStretching(Image img) {
        Histogram histo = new Histogram(img);
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        pixelReader = img.getPixelReader();
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int nr = (r - histo.findMinimumIntensity(histo.getRed())) / (histo.findMaximumIntensity(histo.getRed()) - histo.findMinimumIntensity(histo.getRed())) * 255;
                int ng = (g - histo.findMinimumIntensity(histo.getGreen())) / (histo.findMaximumIntensity(histo.getGreen()) - histo.findMinimumIntensity(histo.getGreen())) * 255;
                int nb = (b - histo.findMinimumIntensity(histo.getBlue())) / (histo.findMaximumIntensity(histo.getBlue()) - histo.findMinimumIntensity(histo.getBlue())) * 255;
                int nargb = (a<<24) | (nr<<16) | (ng<<8) | nb;
                if (null != pixelWriter) {
                    pixelWriter.setArgb(x,y,nargb);
                }
            }
        }
        return writableImage;
    }
}
