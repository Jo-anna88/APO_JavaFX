package sample;

import javafx.scene.chart.BarChart;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

public class MyStackPane extends StackPane {
    private BarChart<String, Number> histogramChannelRed;
    private BarChart<String, Number> histogramChannelGreen;
    private BarChart<String, Number> histogramChannelBlue;
    private BarChart<String, Number> histogramIntensity;
    private Histogram histogram;
    private boolean isRGB;

    MyStackPane(Image img){
        histogram = new Histogram(img);
        isRGB = histogram.getFlag();
        if (isRGB) {
            histogramChannelRed = histogram.countHistogramChannel(histogram.getRed());
            histogramChannelRed.getStyleClass().add("histogramChannelRed"); //nadajemy nazwę stylu w pliku .css
            histogramChannelGreen = histogram.countHistogramChannel(histogram.getGreen());
            histogramChannelGreen.getStyleClass().add("histogramChannelGreen");
            histogramChannelBlue = histogram.countHistogramChannel(histogram.getBlue());
            histogramChannelBlue.getStyleClass().add("histogramChannelBlue");
            histogramIntensity = histogram.countHistogramChannel(histogram.getIntensity());
        }
        else {
            histogramIntensity = histogram.countHistogramChannel(histogram.getIntensity()); //tu równie dobrze może być .getRed()
        }
    }
}
