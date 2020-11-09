package sample;

import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LongSummaryStatistics;


public class Functionality {
//////////////////////////////Functionality connecting with images//////////////////////////////////////////////////////
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
        int minR = histo.findMinimumIntensity(histo.getRed());
        int maxR = histo.findMaximumIntensity(histo.getRed());
        int minG = histo.findMinimumIntensity(histo.getGreen());
        int maxG = histo.findMaximumIntensity(histo.getGreen());
        int minB = histo.findMinimumIntensity(histo.getBlue());
        int maxB = histo.findMaximumIntensity(histo.getGreen());
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

                int nr = (r-minR)*255/(maxR-minR);
                int ng = (g-minG)*255/(maxG-minG);
                int nb = (b-minB)*255/(maxB-minB);
                int nargb = (a << 24) | (nr << 16) | (ng << 8) | nb;
                if (null != pixelWriter) {
                    pixelWriter.setArgb(x, y, nargb);
                }
            }
        }
        return writableImage;
    }

    public static Image invert(Image img) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        pixelReader = img.getPixelReader();
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color.invert());
            }
        }
        return writableImage;
    }

    public static Image progowanieBinarne(Image img, int prog) { //argumentrm musi być greyscale image!
        Histogram histo = new Histogram(img);
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        pixelReader = img.getPixelReader();
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        int nargb;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int i = (int) (.299 * r + .587 * g + 0.114 * b);

                if (i <= prog) i = 0;
                else i = 255;

                nargb = (a << 24) | (i << 16) | (i << 8) | i;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }

    public static Image progowanieZachowaniePoziomowSzarosci (Image img, int prog) {
        Histogram histo = new Histogram(img);
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        pixelReader = img.getPixelReader();
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        int nargb;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int i = (int) (.299 * r + .587 * g + 0.114 * b);

                if (i <= prog) i = 0;

                nargb = (a << 24) | (i << 16) | (i << 8) | i;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }

    public static Image redukcjaPoziomowSzarosci (Image img, int levels) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        pixelReader = img.getPixelReader();
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();

        //krok do wyznaczania nowych wartości:
        double krok1 = (double)255/(levels-1); //np. krok dla 2 poziomów to 255 -> wartości {0,255}, krok dla 3 poziomów to 127.5 -> wartości {0,(int)127.5,255}
        //krok do wyznaczania progów:
        double krok2 = (double)255/levels; //np. krok dla 2 poziomów to 127.5 -> progi {127.5,255}, krok dla 3 poziomów to 85 -> progi {85,170,255}
        //tablica przechowująca nowe wartości (tzw. poziomy rekonstrukcji / wartości reprezentujące dany przedział):
        int[] newValues = new int[levels];
        //tablica przechowująca progi (granice decyzyjne):
        int[] progi = new int[levels];
        //uzupełnienie tablicy przechowującej nowe wartości:
        for (int i=0; i<levels; i++)
            newValues[i] = (int)(i*krok1);
        //uzupełnienie tablicy przechowującej progi:
        for (int i=1; i<(levels+1);i++)
            progi[i-1] = (int)(i*krok2);

        //pozyskanie danych wejściowych:
        int nargb;
        int j=0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int i = (int) (.299 * r + .587 * g + 0.114 * b);
        //przypisywanie nowych wartości w zależności od progu
                while (true) {
                    if (i <= progi[j]) {
                        i = newValues[j];
                        break;
                    }
                    j++;
                }
                j=0;

                nargb = (a << 24) | (i << 16) | (i << 8) | i;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }
//////////////////////////////Functionality connecting with files//////////////////////////////////////////////////////
    public static String getImageName (Image img) {
        String fname = img.getUrl(); //np.C:/.../zdjecie.jpg
        int pos = fname.lastIndexOf("/");
        if (pos > 0) {
            fname = fname.substring((pos + 1)); //zdjecie.jpg
        }
        return fname;
    }
    public static String getNameWithoutExt(Image img) {
        String shortName = getImageName(img);
        int pos = shortName.lastIndexOf(".");
        if (pos>0) {
            shortName = shortName.substring(0,pos);
        }
        return shortName; //zdjecie
    }
    public static String getNameWithoutExt(String tabName) {
        String fName = tabName; //zdjecie_copy.jpg
        int pos = fName.lastIndexOf(".");
        if (pos>0) {
            fName = fName.substring(0,pos);
        }
        return fName; //zdjecie_copy
    }
    public static String getExtension2(Image img) throws MalformedURLException, URISyntaxException {
        URI uri = (new URL(img.getUrl())).toURI();
        File f = new File(uri);
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    public static String getExtension2 (String fName) {
        String ext = null;
        int i = fName.lastIndexOf('.');
        if (i > 0 &&  i < fName.length() - 1) {
            ext = fName.substring(i+1).toLowerCase();
        }
        return ext;
    }
    public static File getParentFile (Image img) throws MalformedURLException, URISyntaxException {
        URI uri = (new URL(img.getUrl())).toURI();
        File f = new File(uri);
        File currentDir = f.getParentFile(); //pobiera info o pliku-rodzicu (o folderze, w którym przechowywany jest obraz)
        return currentDir;
    }
    public static String setFileNameFromTabName (String tabName) {
        String fname = tabName;
        int pos = tabName.lastIndexOf(".");
        if (pos>0) {
            fname = fname.substring(0,pos);
        }
        return fname+"_copy";
    }
}