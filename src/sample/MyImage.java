package sample;

import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class MyImage extends Image {
    private String name;
    private Histogram histo;
    private boolean isRGB;
    private BarChart<String, Number> histogramChannelRed;
    private BarChart<String, Number> histogramChannelGreen;
    private BarChart<String, Number> histogramChannelBlue;
    private BarChart<String, Number> histogramIntensity;

    public MyImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth){

        super(url,requestedWidth,requestedHeight,preserveRatio,smooth);
        try {
            this.name = setName();
        } catch (NullPointerException e) {
            this.name="";
        }
    }

//    public MyImage (WritableImage wi) {
//
//    }

    private String setName() {
        String fname = this.getUrl();
        int pos = fname.lastIndexOf("/");
        if (pos > 0) {
            fname = fname.substring((pos + 1));
        }
        return fname;
    }

    public String getName() {
        return this.name;
    }

    public String getNameWithoutExt() {
        String shortName = this.getName();
        int pos = shortName.lastIndexOf(".");
        if (pos>0) {
            shortName = shortName.substring(0,pos);
        }
        return shortName;
    }

    public void makeHistogram(){
        histo = new Histogram(this);
        isRGB = histo.getFlag();
        if (isRGB) {
            histogramChannelRed = histo.countHistogramChannel(histo.getRed());
            histogramChannelRed.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
            histogramChannelGreen = histo.countHistogramChannel(histo.getGreen());
            histogramChannelGreen.getStyleClass().add("histogramChannelGreen");
            histogramChannelBlue = histo.countHistogramChannel(histo.getBlue());
            histogramChannelBlue.getStyleClass().add("histogramChannelBlue");
            histogramIntensity = histo.countHistogramChannel(histo.getIntensity());
        }
        else {
            histogramIntensity = histo.countHistogramChannel(histo.getIntensity()); //tu równie dobrze może być .getRed()
        }

    }

    public boolean getIsRGB() {
        return isRGB;
    }


    public BarChart<String, Number> getHistogramChannelRed() {
        return histogramChannelRed;
    }

    public BarChart<String, Number> getHistogramChannelBlue() {
        return histogramChannelBlue;
    }

    public BarChart<String, Number> getHistogramChannelGreen() {
        return histogramChannelGreen;
    }

    public BarChart<String, Number> getHistogramIntensity() {
        return histogramIntensity;
    }

    public Image createImageAfterLinearHistogramStretching() {
        int width = (int) this.getWidth();
        int height = (int) this.getHeight();
        PixelReader pixelReader = this.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int y = 0; y < this.getHeight(); y++) {
            for (int x = 0; x < this.getWidth(); x++) {
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

        return newImage;
    }
}
