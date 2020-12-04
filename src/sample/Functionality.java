package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.FileChooser;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;

import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.BORDER_REPLICATE;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

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
    //////////////////////////////////////////////////LAB3/////////////////////////////////////////////////////////

    public static Image median(Mat src, Mat dst, int ksize) {
        Imgproc.medianBlur(src, dst, ksize); //borderType -> median filter uses BORDER_REPLICATE internally (aaaaaa|abcdefgh|hhhhhhh)
        //Converting matrix to JavaFX writable image
        java.awt.Image img = HighGui.toBufferedImage(dst); //a jak to zrobić dla javafx.Image ??
        WritableImage writableImage= SwingFXUtils.toFXImage((BufferedImage) img, null);
        return writableImage;
    }

    public static Image median(Mat src, Mat dst, int ksize, int methodForBorderPixels, int value) {
        int borderType;
        switch (methodForBorderPixels) {
            case 1:
                borderType = Core.BORDER_REFLECT; //reflect     fedcba|abcdefgh|hgfedcb
                break;
            case 2:
                borderType = Core.BORDER_WRAP; //wrap           cdefgh|abcdefgh|abcdefg
                break;
            case 3:
                borderType = Core.BORDER_CONSTANT; //constant   iiiiii|abcdefgh|iiiiii
                // !! (tu wg wykładów ma być bez dodawania kolumn i wierszy pomocniczych, a jedynie nadanie skrajnym kolumnom i wierszom stałej wartości)
                break;
            case 4:
                borderType=4; //leave original value (bez dodawania kolumn i wierszy pomocniczych)
                Imgproc.medianBlur(src,dst,ksize); //robimy filtrację metodą domyślną (potem zastąpimy wartości skrajnych pikseli wartościami z obrazu źródłowego)
                break;
            default:
                //borderType = Core.BORDER_REPLICATE;
                Imgproc.medianBlur(src,dst,ksize);
                java.awt.Image img = HighGui.toBufferedImage(dst); //a jak to zrobić dla javafx.Image ??
                WritableImage writableImage= SwingFXUtils.toFXImage((BufferedImage) img, null);
                return writableImage;
        }
        //jeśli nie stosujemy metody domyślnej musimy uzupełnić piksele brzegowe "własnoręcznie"

        //1. tworzymy obraz uzupełniony o brzeg (bufImage) dla danego obrazu (src): powiększony z każdej strony o x pikseli (border),
        //   wykorzystując daną (wbudowaną) metodę uzupełniania wartości dla tych pikseli (uwaga! - nie dotyczy opcji nr 4!)
        //int top, bottom, left, right; ==ksize-2
        int border = ksize/2;

        if (borderType!=4) {
            Mat bufImage = new Mat(src.rows()+border*2, src.cols()+border*2, src.type() ); //matryca dla obrazu źródłowego powiększonego o brzeg
            Mat dstPlus = new Mat(bufImage.rows(),bufImage.cols(),bufImage.type()); //matryca dla obrazu wynikowego po filtracji medianowej na bufImage

            if (borderType == BORDER_CONSTANT)
                Core.copyMakeBorder(src, bufImage, border, border, border, border, BORDER_CONSTANT, new Scalar(value, value, value)); //Scalar (B, G, R (,alpha))
            else
                Core.copyMakeBorder(src, bufImage, border, border, border, border, borderType);

            //2. wykonanie filtracji medianowej na tym obrazie (powiększonym) - nie interesuje nas, że dla dodatkowego brzegu użyje met.domyślnej
            Imgproc.medianBlur(bufImage, dstPlus, ksize);
            //3. usunięcie zbędnego brzegu z obrazu (wskazanie ROI?) - create subimage / crop (rtcg)
            //https://stackoverflow.com/questions/15589517/how-to-crop-an-image-in-opencv-using-python <-- using Python
            Rect roi = new Rect(border,border,src.cols(),src.rows()); //x_start, y_start, width, height
            Mat dst2 = new Mat(dstPlus,roi);
            //4. przesłanie obrazu po filtracji
            java.awt.Image img = HighGui.toBufferedImage(dst2); //a jak to zrobić dla javafx.Image ??
            WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage) img, null);
            return writableImage;
        }

//      2. zmiana wartości skrajnych pikseli na te z obrazu źródłowego
        putOriginalValue(src,dst,ksize);

        java.awt.Image img = HighGui.toBufferedImage(dst);
        WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img,null);
        return writableImage;
    }

    public static Mat blurConstant (Mat src, int ksize, int value) { //uśrednianie dla BORDER_CONSTANT
        int border = ksize/2;
        Mat bufImage = new Mat(src.rows() + border * 2, src.cols() + border * 2, src.type()); //matryca dla obrazu źródłowego powiększonego o brzeg
        Mat dstPlus = new Mat(bufImage.rows(), bufImage.cols(), bufImage.type()); //matryca dla obrazu wynikowego po filtracji medianowej na bufImage
        Core.copyMakeBorder(src, bufImage, border, border, border, border, BORDER_CONSTANT, new Scalar(value, value, value)); //Scalar (B, G, R (,alpha))
        Imgproc.blur(bufImage, dstPlus, new Size(ksize,ksize));
        Rect roi = new Rect(border,border,src.cols(),src.rows()); //x_start, y_start, width, height
        Mat dst2 = new Mat(dstPlus,roi);
        return dst2;
    }

    public static Mat filter2dConstant (Mat src, int ksize, int value, Mat kernel) {
        int border = ksize/2;
        Mat bufImage = new Mat(src.rows() + border * 2, src.cols() + border * 2, src.type()); //matryca dla obrazu źródłowego powiększonego o brzeg
        Mat dstPlus = new Mat(bufImage.rows(), bufImage.cols(), bufImage.type()); //matryca dla obrazu wynikowego po filtracji medianowej na bufImage
        Core.copyMakeBorder(src, bufImage, border, border, border, border, BORDER_CONSTANT, new Scalar(value, value, value)); //Scalar (B, G, R (,alpha))
        Imgproc.filter2D(bufImage, dstPlus,-1,kernel);//?
        Rect roi = new Rect(border,border,src.cols(),src.rows()); //x_start, y_start, width, height
        Mat dst2 = new Mat(dstPlus,roi);
        return dst2;
    }

    public static Mat gaussBlurConstant (Mat src, int ksize, int value) {
        int border = ksize/2;
        Mat bufImage = new Mat(src.rows() + border * 2, src.cols() + border * 2, src.type()); //matryca dla obrazu źródłowego powiększonego o brzeg
        Mat dstPlus = new Mat(bufImage.rows(), bufImage.cols(), bufImage.type()); //matryca dla obrazu wynikowego po filtracji medianowej na bufImage
        Core.copyMakeBorder(src, bufImage, border, border, border, border, BORDER_CONSTANT, new Scalar(value, value, value)); //Scalar (B, G, R (,alpha))
        Imgproc.GaussianBlur(bufImage, dstPlus, new Size(ksize,ksize),0);
        Rect roi = new Rect(border,border,src.cols(),src.rows()); //x_start, y_start, width, height
        Mat dst2 = new Mat(dstPlus,roi);
        return dst2;
    }

    public static void putOriginalValue (Mat src, Mat dst, int ksize) {
        int border = ksize/2;
        int height = src.rows();
        int weight = src.cols();
        for (int i=0; i<height; i++) {
            if (i<border || i>=(height-border)) { //góra lub dół obrazu
                for (int j = 0; j < weight; j++) {
                    double[] data = src.get(i, j); //get(int row, int col) - pobranie wartości z obrazu źródłowego
                    dst.put(i, j, data); //wstawienie wartości dla obrazu wyjściowego
                }
            }
            else {
                for (int j=0; j<border; j++) { //lewa strona obrazu
                    double[] data = src.get(i, j);
                    dst.put(i, j, data);
                }
                for (int j=weight-border; j<weight; j++) { //prawa strona obrazu
                    double[] data = src.get(i, j);
                    dst.put(i, j, data);
                }
            }
        }
    }

    public static Image smooth(Mat src, int ksize, int choiceOfSmoothingType, int choiceOfBorderType, int value, int K) {
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        Point anchor = new Point(-1,-1);
        Size kSize = new Size(ksize,ksize);
        int borderType = returnBorderType(choiceOfBorderType);

        switch(choiceOfSmoothingType) {
            case 0: //uśrednianie
                if(borderType==10) {//leave original value //choice:4
                    Imgproc.blur(src, dst, kSize);
                    putOriginalValue(src,dst,ksize);
                }
                else if (borderType==BORDER_CONSTANT) //choice:3
                    dst=blurConstant(src,ksize,value);
                else
                    Imgproc.blur(src,dst,kSize,anchor,borderType);
                break;

            case 1: //uśrednianie K-pudełkowe
                int sum = ksize*ksize + (K-1);
                //zainicjowanie macierzy jedynkami (można użyć Mat m = Mat::ones(2, 2, CV_8UC3), ale to == Mat(2, 2, CV_8UC3, 1); // OpenCV replaces `1` with `Scalar(1,0,0)`)
                Mat kernel_init=Mat.ones(kSize,CvType.CV_32F);
                //wstawienie wartości K
                kernel_init.put(ksize/2,ksize/2, K);

                //normalizacja https://docs.opencv.org/3.4/d4/dbd/tutorial_filter_2d.html
                Mat kernel = new Mat();
                Core.multiply(kernel_init, new Scalar(1/(double)sum), kernel); //alternatywnie: https://www.tutorialspoint.com/opencv/opencv_filter2d.htm

                //Imgproc.filter2D(Mat src, Mat dst, int ddepth, Mat kernel, Point anchor, double delta, int borderType)
                if(borderType==10) { //leave original value
                    Imgproc.filter2D(src,dst,-1,kernel); //filtrowanie z domyślnym type_border
                    putOriginalValue(src,dst,ksize);
                }
                else if (borderType==BORDER_CONSTANT) //choice:3
                    dst=filter2dConstant(src,ksize,value,kernel);//?
                else
                    Imgproc.filter2D(src,dst,-1,kernel,anchor,0, borderType);

                break;

            case 2: //filtr gaussowski
                if (borderType==10) { //leave original value
                    Imgproc.GaussianBlur(src,dst,kSize,0); //filtrowanie z domyślnym type_border
                    putOriginalValue(src,dst,ksize);
                }
                else if (borderType==BORDER_CONSTANT) //BORDER_CONSTANT
                    dst=gaussBlurConstant(src,ksize,value);
                else
                    Imgproc.GaussianBlur(src,dst,kSize,0,0, borderType); //sigmaX-Gaussian kernel standard deviation in X direction
                break;
        }

        java.awt.Image img = HighGui.toBufferedImage(dst);
        WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
        return writableImage;
    }

    public static int returnBorderType (int choice) { //uwaga! nie dla mediany!!
        int borderType;
        switch (choice) {
            case 1:
                borderType = Core.BORDER_REFLECT; //reflect         fedcba|abcdefgh|hgfedcb
                break;
            case 2:
                borderType = Core.BORDER_REPLICATE; //replicate     aaaaaa|abcdefgh|hhhhhhh
                break;
            case 3:
                borderType = Core.BORDER_CONSTANT; //constant       iiiiii|abcdefgh|iiiiii
                break;
            case 4:
                borderType=10; //leave original value (bez dodawania kolumn i wierszy pomocniczych)
                break;
            default:
                borderType = Core.BORDER_DEFAULT;//BORDER_REFLECT_101 gfedcb|abcdefgh|gfedcba
        }
        return borderType;
    }

    public static Image sobel(Mat src, int derivatives, int choice) {
        //paramtery
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;
        //convert colors to grayscale
        Mat gray = new Mat(src.rows(), src.cols(), src.type());
        cvtColor(src,gray,COLOR_BGR2GRAY);
        //remove noise by gaussianblur
        if (choice==3) choice=4; //bo usunięto constant
        int borderType = returnBorderType(choice);
        //wygładzanie z rozróźnieniem borderType:
        //if (borderType==10 || borderType==BORDER_CONSTANT) borderType=BORDER_DEFAULT; //dla wygładzania nie rozróżniam opcji spoza zdef.w OpenCV listy
        //Imgproc.GaussianBlur(gray,gray,new Size(3,3),0,0,borderType);
        //wygładzanie dla domyślnych wartości:
        Mat grayGB = new Mat (src.rows(), src.cols(),gray.type());
        Imgproc.GaussianBlur(gray,grayGB,new Size(3,3),0);
        //gradienty
        Mat grad_x = new Mat(), grad_y = new Mat(), grad_xy = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat(), abs_grad_xy = new Mat();
        if (borderType==10) {//leave original value
            Imgproc.Sobel(grayGB, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
            Imgproc.Sobel(grayGB, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);
            // converting back to CV_8U
            Core.convertScaleAbs( grad_x, abs_grad_x ); //liczymy wartość przybliżoną
            Core.convertScaleAbs( grad_y, abs_grad_y ); //liczymy wartość przybliżoną
        }
         else {
            Imgproc.Sobel(grayGB, grad_x, ddepth, 1, 0, 3, scale, delta, borderType);
            Imgproc.Sobel(grayGB, grad_y, ddepth, 0, 1, 3, scale, delta, borderType);
            // converting back to CV_8U
            Core.convertScaleAbs( grad_x, abs_grad_x ); //liczymy wartość przybliżoną
            Core.convertScaleAbs( grad_y, abs_grad_y ); //liczymy wartość przybliżoną
        }

        Mat dst = new Mat();
        java.awt.Image img;
        //ustawienie obrazu wynikowego
        if (derivatives==0) {//tryb domyślny
            Core.addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, dst );
            if (borderType==10) putOriginalValue(gray, dst,3);
            img = HighGui.toBufferedImage(dst);
        } else if (derivatives==1) {//dx:1, dy:0
            if (borderType==10) putOriginalValue(gray, abs_grad_x,3);
            img = HighGui.toBufferedImage(abs_grad_x);
        } else { //dx:0, dy:1
            if (borderType==10) putOriginalValue(gray, abs_grad_y,3);
            img = HighGui.toBufferedImage(abs_grad_y);
        }
        //konwersja z Mat do Image
        WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
        return writableImage;
    }

    public static Image canny(Mat src, int threshold, int ratio, int choice) {
        Mat gray = new Mat(src.rows(), src.cols(), src.type());
        //Mat grayBlur = new Mat(src.rows(), src.cols(), src.type());
        Mat edges = new Mat(src.rows(), src.cols(), src.type());
        Mat dst = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
        //konwersja na obraz szaroodcieniowy
        cvtColor(src,gray,COLOR_BGR2GRAY);

        //detecting the edges
        if (choice==3) choice=4; //bo usunięto constant
        int borderType = returnBorderType(choice);
        if (borderType==10) {//leave original value
            //blur
            Imgproc.blur(gray, edges, new Size(3, 3));
            //detecting the edges
            Imgproc.Canny(edges, edges, threshold,threshold*ratio,3,false);
            src.copyTo(dst,edges); //wstawiamy krawędzie do obrazu wyjściowego
            putOriginalValue(src,dst,3); //dla skrajnych pikseli - ustawiamy wartość z obrazu źródł.szaroodc.
        } else {
            //blur
            Imgproc.blur(gray, edges, new Size(3, 3), new Point(-1,-1),borderType);
            //detecting the edges
            Imgproc.Canny(edges,edges,threshold,threshold*ratio,3,false);
            src.copyTo(dst,edges);
        }

        java.awt.Image img = HighGui.toBufferedImage(dst);
        WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
        return writableImage;
    }

    public static Image prewitt(Mat src, int matrixNr, int choice) {
        if (choice==3) choice=4; //bo usunięto constant
        int borderType = returnBorderType(choice);
        Point anchor = new Point(-1, -1);

        Mat kernel=null;
        switch (matrixNr) {
            case 1: //N
                int[] data1 = {1,1,1,1,-2,1,-1,-1,-1};
                kernel = createKernel(data1);
                break;
            case 2: //NE
                int[] data2 = {1,1,1,-1,-2,1,-1,-1,1};
                kernel = createKernel(data2);
                break;
            case 3: //E
                int[] data3 = {-1,1,1,-1,-2,1,-1,1,1};
                kernel = createKernel(data3);
                break;
            case 4: //SE
                int[] data4 = {-1,-1,1,-1,-2,1,1,1,1};
                kernel = createKernel(data4);
                break;
            case 5: //S
                int[] data5 = {-1,-1,-1,1,-2,1,1,1,1};
                kernel = createKernel(data5);
                break;
            case 6: //SW
                int[] data6 = {1,-1,-1,1,-2,-1,1,1,1};
                kernel = createKernel(data6);
                break;
            case 7: //W
                int[] data7 = {1,1,-1,1,-2,-1,1,1,-1};
                kernel = createKernel(data7);
                break;
            case 8: //NW
                int[] data8 = {1,1,1,1,-2,-1,1,-1,-1};
                kernel = createKernel(data8);
                break;
        }
        //0.przechowanie typu obrazu źródłowego
        int srcType = src.type();
        //1.konwersja obrazu źródłowego do formatu uwzględniającego liczby z zakresu {-4080, 4080} <- możliwe min i max
        src.convertTo(src,CvType.CV_16S); //nadal pozostajemy przy 3-4 kanałach (takiej ilości, jaka była)
        //2.utworzenie macierzy wyjściowej dla obrazu po filtracji
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        if (borderType==10) {//leave original values
            Imgproc.filter2D(src,dst,-1,kernel); //filtrowanie z domyślnym type_border
        }
        else {Imgproc.filter2D(src,dst,-1,kernel,anchor,0,borderType);}
        //3.normalizacja: min, max --> 0-255
        Core.normalize(dst, dst, 0, 255, Core.NORM_MINMAX,-1);
        //4.konwersja "powrotna" i dokończenie opcji "leave orig.val."
        src.convertTo(src,srcType);
        dst.convertTo(dst,srcType);
        if (borderType==10) {putOriginalValue(src,dst,3);}
        //5.przygotowanie obrazu wyjściowego
        java.awt.Image img = HighGui.toBufferedImage(dst);
        WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
        return writableImage;
    }

    public static Image laplasjan(Mat src, int matrixNr, int choice, int value) {
        int borderType = returnBorderType(choice);
        Point anchor = new Point(-1,-1);
        Mat kernel;
        if (matrixNr==1) {
            int[]data={0,-1,0,-1,4,-1,0,-1,0};
            kernel = createKernel(data);
        } else if (matrixNr==2) {
            int[]data={-1,-1,-1,-1,8,-1,-1,-1,-1};
            kernel = createKernel(data);
        } else {
            int[]data={1,-2,1,-2,4,-2,1,-2,1};
            kernel = createKernel(data);
        }

        //0.przechowanie typu obrazu źródłowego
        int srcType = src.type();
        //1.konwersja obrazu źródłowego do formatu uwzględniającego liczby z zakresu {-4080, 4080} <- możliwe min i max
        src.convertTo(src,CvType.CV_16S); //nadal pozostajemy przy 3-4 kanałach (takiej ilości, jaka była)
        //2.utworzenie macierzy wyjściowej dla obrazu po filtracji
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        if (borderType==10) {//leave original values
            Imgproc.filter2D(src,dst,-1,kernel); //filtrowanie z domyślnym type_border
            //putOriginalValue(src,dst,3);
        }
        else if (borderType==BORDER_CONSTANT) {
            dst=filter2dConstant(src,3,value,kernel);
        }
        else {Imgproc.filter2D(src,dst,-1,kernel,anchor,0,borderType);}
        //3.normalizacja: min, max --> 0-255
        Core.normalize(dst, dst, 0, 255, Core.NORM_MINMAX,-1);
        //4.konwersja "powrotna"
        src.convertTo(src,srcType);
        dst.convertTo(dst,srcType);
        if (borderType==10) {
            putOriginalValue(src, dst, 3);
            printMat(src);
            printMat(dst);
        }
        //5.przygotowanie obrazu wyjściowego
        java.awt.Image img = HighGui.toBufferedImage(dst);
        WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
        return writableImage;
    }

    public static Mat createKernel (int[] data) {
        Mat kernel = new Mat(3,3,CvType.CV_32F);
        int k=0;
        for(int i=0; i<kernel.rows(); i++) {
            for (int j=0; j<kernel.cols(); j++) {
                kernel.put(i,j,data[k]);
                k++;
            }
        }
        return kernel;
    }

    public static void printMat (Mat mat) {
        for (int i=0; i<mat.rows();i++) {
            for (int j=0; j<mat.cols(); j++) {
                double[] m = mat.get(i,j);
                for (double x: m) {
                    System.out.print(x+", ");
                }
                System.out.print("\t");
            }
            System.out.println();
        }
    }
    //////////////////////////////////Logic Op.//////////////////////////////////////////////////////////////////
    public static Image logicOp (Mat src1, Mat src2, int selection) {
        System.out.println("src1.type: "+src1.type()+" src2.type: "+src2.type());
        Mat dst= new Mat();
        switch (selection) {
            case 0: //AND
                Core.bitwise_and(src1,src2,dst);
                break;
            case 1: //OR
                Core.bitwise_or(src1,src2,dst);
                break;
            case 2: //XOR
                Core.bitwise_xor(src1,src2,dst);
                break;
            default:
        }
        java.awt.Image img = HighGui.toBufferedImage(dst);
        WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage)img, null);
        return writableImage;
    }

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