package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.FileChooser;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;

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
                int i = (int) Math.round(.299 * r + .587 * g + .114 * b);
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
        int maxB = histo.findMaximumIntensity(histo.getBlue());
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

                int nr = (r - minR) * 255 / (maxR - minR);
                int ng = (g - minG) * 255 / (maxG - minG);
                int nb = (b - minB) * 255 / (maxB - minB);
                int nargb = (a << 24) | (nr << 16) | (ng << 8) | nb;
                if (null != pixelWriter) {
                    pixelWriter.setArgb(x, y, nargb);
                }
            }
        }
        return writableImage;
    }

    public static Image createImageAfterLinearHistogramStretchingWithRange(Image img, int p1, int p2, int q1, int q2) {
        //rozciąganie histogramu z zakresu {p1,p2} do {q3,q4}
        //założenie: q=(p-fmin)/(fmax-fmin)*Lmax+offset
        // {0,(p1-1)} --> {0, q1-1}     gdy p<p1
        // {p1, p2}   --> {q1, q2}      gdy p1<=p<=p2
        // {p2+1, 255}--> {q2+1, 255}   gdy p>p2
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
                int intensity = (int) Math.round(.299 * r + .587 * g + .114 * b);

                int fmin, fmax, Lmax, offset, newIntensity;
                if (intensity < p1) {
//                    if (q1==0 || q1==1) {
//                        newIntensity=0;
//                    }
//                    else if (p1==q1) {
//                        newIntensity=intensity;
//                    }
//                    else {
                    fmin = 0;
                    fmax = p1 - 1;
                    Lmax = q1 - 1;
                    offset = 0;
//                      newIntensity = (int) ((intensity - fmin)/(double)(fmax-fmin) *Lmax + offset);
//                    }
                } else if (intensity > p2) {
//                    if (q2==255 || q2==255-1) {
//                        newIntensity=255;
//                    }
//                    else if (p2==q2) {
//                        newIntensity=intensity;
//                    }
//                    else {
                    fmin = p2 + 1;
                    fmax = 255;
                    Lmax = 255 - (q2 + 1);
                    offset = q2 + 1;
//                    }
                } else {
                    fmin = p1;
                    fmax = p2;
                    Lmax = q2 - q1;
                    offset = q1;
//                  newIntensity = (int) ((intensity - fmin)/(double)(fmax-fmin) *Lmax + offset);
                }
                newIntensity = (int) ((intensity - fmin) / (double) (fmax - fmin) * Lmax + offset);
                int nargb = (a << 24) | (newIntensity << 16) | (newIntensity << 8) | newIntensity;
                if (null != pixelWriter) {
                    pixelWriter.setArgb(x, y, nargb);
                }
            }
        }
        return writableImage;
    }

    public static Image createImageAfterHistogramEqualizationAPO(Image img, Histogram histogram) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        int pixelsSum = width * height;
        //utworzenie dystrybuanty
        double[] dystrybuanta = new double[256];
        long cummulativeValue = histogram.getIntensity()[0];
        for (int i = 0; i < dystrybuanta.length; i++) {
            dystrybuanta[i] = cummulativeValue / (double) pixelsSum;
            cummulativeValue += histogram.getIntensity()[i];
        }
        //znalezienie pierwszej niezerowej wartości dystrybuanty
        double min;
        int i = 0;
        for (; i < dystrybuanta.length; i++) {
            if (dystrybuanta[i] > 0) break;
        }
        min = dystrybuanta[i];
        //utworzenie tablicy przekodowań
        int[] LUT = new int[256];
        for (i = 0; i < LUT.length; i++) {
            LUT[i] = (int) ((((dystrybuanta[i]) - min) / (1 - min)) * 255);
        }
        //odczyt danych z obrazu i przyporządkowanie im nowych wartości zgodnie z tablicą LUT
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
                int intensity = (int) Math.round(.299 * r + .587 * g + 0.114 * b);
                intensity = LUT[intensity];
                int nargb = (a << 24) | (intensity << 16) | (intensity << 8) | intensity;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }


    //http://spatial-analyst.net/ILWIS/htm/ilwisapp/stretch_algorithm.htm
    public static Image createImageAfterHistogramEqualization(Image img, Histogram histogram) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        int pixelsSum = width * height; //sumujemy liczbę pikseli, żeby wiedzieć jak je równomiernie poprzydzielać nowym wartościom
        double threshold = pixelsSum / 256.0; //próg (liczba pikseli, która ma się znaleźć w każdej z grup (o ile się uda)
        //utworzenie histogramu skumulowanego
        long[] cummulativeHistogram = new long[256];
        cummulativeHistogram[0] = histogram.getIntensity()[0];
        for (int i = 1; i < (cummulativeHistogram.length); i++) {
            cummulativeHistogram[i] = histogram.getIntensity()[i] + cummulativeHistogram[i - 1];
        }
        //utworzenie tablicy przekodowań (np. pikselom o wartości j będziemy przyporządkowywać wartość i)
        int[] LUT = new int[256];
        for (int i = 0, j = 0; i < 256; i++) {
            while ((j < 256) && (cummulativeHistogram[j] <= ((i + 1) * threshold))) { //sprawdza pierwszy warunek (j<256) i gdy j==256 i tu wyjdzie false, to nie sprawdzi cummulativeHistogram[256] (bo tu operator AND) - nie wyjdzie poza granicę tablicy
                LUT[j] = i;
                j++;
            }
        }
        //odczyt danych z obrazu i przyporządkowanie im nowych wartości zgodnie z tablicą LUT
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
                int intensity = (int) Math.round (.299 * r + .587 * g + .114 * b);
                intensity = LUT[intensity];
                int nargb = (a << 24) | (intensity << 16) | (intensity << 8) | intensity;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }

    public static Image createImageAfterSelectedHistogramEqualization(Image img, Histogram histogram) {
//• BBHE - Bi-Histogram Equalization
//• DSIHE - Dualistic Sub-Image Histogram Equalization
// polegają na dekompozycji obrazu wejściowego na dwa podobrazy (wg pewnego kryterium) i wykonania operacji
//  HE dla tych podobrazów
// W metodzie BBHE za kryterium podziału przyjmuje się średnią jasność w obrazie,
// a w DSIHE obraz dzieli się na dwa podobrazy o takiej samej ilości pikseli (jaśniejszych i ciemniejszych).
        //tu przyjęto met.DSIHE
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        //utworzenie histogramu skumulowanego
        long[] cummulativeHistogram = new long[256];
        cummulativeHistogram[0] = histogram.getIntensity()[0];
        for (int i = 1; i < (cummulativeHistogram.length); i++) {
            cummulativeHistogram[i] = histogram.getIntensity()[i] + cummulativeHistogram[i - 1];
        }
        //wyliczenie wartości progu
        int halfPixelsSum = width * height / 2;
        int threshold = 0; //0 oznacza tu poziom jasności, którym inicjujemy zmienną 'threshold'
        long min = Math.abs(cummulativeHistogram[0] - halfPixelsSum); //poszukujemy min.różnicy pomiędzy wartością hist.skum. a poł.pikseli obrazu
        for (int i = 1; i < cummulativeHistogram.length; i++) {
            if ((Math.abs(cummulativeHistogram[i] - halfPixelsSum)) < min) {
                threshold = i;
                min = Math.abs(cummulativeHistogram[i] - halfPixelsSum);
            }
        }
        //podział histogramu (intensywność poziomu jasności) na 2 histogramy ('ciemniejszy'-left i 'jasniejszy'-right)
        long[] histoLeftSide = new long[threshold + 1];
        long[] histoRightSide = new long[255 - threshold];
        int i = 0;
        while (i <= threshold) {
            histoLeftSide[i] = histogram.getIntensity()[i];
            i++;
        }
        i = 0;
        while (i < histoRightSide.length) {
            histoRightSide[i] = histogram.getIntensity()[threshold + 1 + i];
            i++;
        }
        //////////////////////////////wykonanie equalizacji (HE) dla tych dwóch histogramów/////////////////////////////
        //1. zliczenie liczby pikseli w każdym z histogramów
        //   wynika ona z histogramu skumulowanego -> piksele do thresholdu (włącznie) tworzą pierwszy 'podobraz'
        long sumLeft = cummulativeHistogram[threshold];
        long sumRight = width * height - sumLeft;
        //2. uwtorzenie dystrybuant
        double[] dystrybuantaLeft = new double[threshold + 1]; //==new double [histoLeftSide.length()]
        double[] dystrybuantaRight = new double[255 - threshold]; //==new double [histoRightSide.length()]
        for (i = 0; i < dystrybuantaLeft.length; i++) {
            dystrybuantaLeft[i] = cummulativeHistogram[i] / (double) sumLeft;
        }
        for (i = 0; i < dystrybuantaRight.length; i++) {
            dystrybuantaRight[i] = (cummulativeHistogram[threshold + 1 + i] - sumLeft) / (double) sumRight;
        }
        //3. znalezienie pierwszych niezerowych wartości dystrybuanty
        double minLeft;
        double minRight;
        i = 0;
        for (; i < dystrybuantaLeft.length; i++) {
            if (dystrybuantaLeft[i] > 0) break;
        }
        minLeft = dystrybuantaLeft[i];
        i = 0;
        for (; i < dystrybuantaRight.length; i++) {
            if (dystrybuantaRight[i] > 0) break;
        }
        minRight = dystrybuantaRight[i];
        //4. utworzenie tablicy przekodowań
        int[] LUTLeft = new int[threshold + 1];
        int[] LUTRight = new int[255 - threshold];
        for (i = 0; i < LUTLeft.length; i++) {
            LUTLeft[i] = (int) ((((dystrybuantaLeft[i]) - minLeft) / (1 - minLeft)) * threshold); //{0,threshold}
        }
        for (i = 0; i < LUTRight.length; i++) {
            LUTRight[i] = ((int) ((((dystrybuantaRight[i]) - minRight) / (1 - minRight)) * (255 - threshold - 1)) + threshold + 1); //{threshold+1, 255}
        }
        int[] LUT = new int[256];
        i = 0;
        while (i < LUTLeft.length) {
            LUT[i] = LUTLeft[i]; //ost.while to LUT[threshold]
            i++; //ost.'i' to threshold+1
        }
        while (i < LUT.length) {
            LUT[i] = LUTRight[i - threshold - 1];
            i++;
        }
        //5. odczyt danych z obrazu i przypisanie nowych wartości na podstawie tablicy LUT
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
                int intensity = (int) Math.round(.299 * r + .587 * g + .114 * b);
                intensity = LUT[intensity];
                int nargb = (a << 24) | (intensity << 16) | (intensity << 8) | intensity;
                pixelWriter.setArgb(x, y, nargb);
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

    public static Image progowanieBinarne(Image img, int prog) { //argumentem musi być grayscale image!
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
                int i = (int) Math.round(.299 * r + .587 * g + .114 * b);

                if (i <= prog) i = 0;
                else i = 255;

                nargb = (a << 24) | (i << 16) | (i << 8) | i;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }

    public static Image progowanieZachowaniePoziomowSzarosci(Image img, int prog) {
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
                int i = (int) Math.round(.299 * r + .587 * g + .114 * b);

                if (i <= prog) i = 0;

                nargb = (a << 24) | (i << 16) | (i << 8) | i;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }

    public static Image redukcjaPoziomowSzarosci(Image img, int levels) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        pixelReader = img.getPixelReader();
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();

        //krok do wyznaczania nowych wartości:
        double krok1 = (double) 255 / (levels - 1); //np. krok dla 2 poziomów to 255 -> wartości {0,255}, krok dla 3 poziomów to 127.5 -> wartości {0,(int)127.5,255}
        //krok do wyznaczania progów:
        double krok2 = (double) 255 / levels; //np. krok dla 2 poziomów to 127.5 -> progi {127.5,255}, krok dla 3 poziomów to 85 -> progi {85,170,255}
        //tablica przechowująca nowe wartości (tzw. poziomy rekonstrukcji / wartości reprezentujące dany przedział):
        int[] newValues = new int[levels];
        //tablica przechowująca progi (granice decyzyjne):
        int[] progi = new int[levels];
        //uzupełnienie tablicy przechowującej nowe wartości:
        for (int i = 0; i < levels; i++)
            newValues[i] = (int) (i * krok1);
        //uzupełnienie tablicy przechowującej progi:
        for (int i = 1; i < (levels + 1); i++)
            progi[i - 1] = (int) (i * krok2);

        //pozyskanie danych wejściowych:
        int nargb;
        int j = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int i = (int) Math.round(.299 * r + .587 * g + .114 * b);
                //przypisywanie nowych wartości w zależności od progu
                while (true) {
                    if (i <= progi[j]) {
                        i = newValues[j];
                        break;
                    }
                    j++;
                }
                j = 0;

                nargb = (a << 24) | (i << 16) | (i << 8) | i;
                pixelWriter.setArgb(x, y, nargb);
            }
        }
        return writableImage;
    }
    ////////////////STATYSTYKI HISTOGRAMU/////////////////
    public static int pixelsAmount (Image img) {
        return (int)(img.getHeight()*img.getWidth());
    }
//    public static Image mediana(Image img) {
//
//    }


    //////////////////////////////Functionality connecting with files//////////////////////////////////////////////////////
    public static String getImageName(Image img) {
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
        if (pos > 0) {
            shortName = shortName.substring(0, pos);
        }
        return shortName; //zdjecie
    }

    public static String getNameWithoutExt(String tabName) {
        String fName = tabName; //zdjecie_copy.jpg
        int pos = fName.lastIndexOf(".");
        if (pos > 0) {
            fName = fName.substring(0, pos);
        }
        return fName; //zdjecie_copy
    }

    public static String getExtension2(Image img) throws MalformedURLException, URISyntaxException {
        URI uri = (new URL(img.getUrl())).toURI();
        File f = new File(uri);
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static String getExtension2(String fName) {
        String ext = null;
        int i = fName.lastIndexOf('.');
        if (i > 0 && i < fName.length() - 1) {
            ext = fName.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static File getParentFile(Image img) throws MalformedURLException, URISyntaxException {
        URI uri = (new URL(img.getUrl())).toURI();
        File f = new File(uri);
        File currentDir = f.getParentFile(); //pobiera info o pliku-rodzicu (o folderze, w którym przechowywany jest obraz)
        return currentDir;
    }

    public static String setFileNameFromTabName(String tabName) {
        String fname = tabName;
        int pos = tabName.lastIndexOf(".");
        if (pos > 0) {
            fname = fname.substring(0, pos);
        }
        return fname + "_copy";
    }

    public static void save (Image originalImage, Image destinationImage) {
        String ext2="png";

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                // new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );
        try {
            File currentDir = Functionality.getParentFile(originalImage); //dla WritableImage tu byłby błąd
            //ext2 = Functionality.getExtension2(originalImage); //np.jpg
            fileChooser.setInitialFileName(Functionality.getNameWithoutExt(originalImage)); //ustawia nazwę dla pliku
            if ((currentDir != null) && (currentDir.exists())) {
                fileChooser.setInitialDirectory(currentDir); //ustawia initial directory jako ten, w którym zapisany jest f
            }
        } catch (MalformedURLException | URISyntaxException e) { //dla WritableImage
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            ext2 = "png";
        }

        if( fileChooser == null ) {
            return;
        }
        fileChooser.setTitle("Save Image As...");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(destinationImage,
                        null), ext2, file); //domyślnie ustawia .jpg (dlaczego?)
            } catch (IOException ex) {
                Logger.getLogger(
                        FileChooser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        ///https://www.baeldung.com/java-file-extension///
//    public Optional<String> getExtension(File file) {
//        String filename = file.getName();
//        return Optional.ofNullable(filename)
//                .filter(f -> f.contains("."))
//                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
//    }
    }

}