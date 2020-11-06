package sample;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.util.Arrays;
import java.util.LongSummaryStatistics;

public class Histogram2 {
    private MyImage img;

    private long[] red = new long[256];
    private long[] green = new long[256];
    private long[] blue = new long[256];
    private long[] intensity = new long[256];

//    XYChart.Series seriesRed;
//    XYChart.Series seriesGreen;
//    XYChart.Series seriesBlue;

    long max[] = new long[3]; //RGB
    long maximum;

    private boolean flag = true; //do sprawdzania czy obraz jest monochromatyczny czy rgb

    Histogram2(MyImage src) {
        img = src;

        for (int i = 0; i < 256; i++) {
            red[i] = green[i] = blue[i] = 0;
        }

        PixelReader pixelReader = img.getPixelReader();
        if (pixelReader == null) {
            return;
        }

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int i = (int)(.299*r + .587*g + 0.114*b); //intensywność w wersji grayscale / mono

                red[r]++; //histogram w postaci tablicy LUT dla kanału czerwonego
                green[g]++; //histogram w postaci tablicy LUT dla kanału zielonego
                blue[b]++; //histogram w postaci tablicy LUT dla kanału niebieskiego
                intensity[i]++; //histogram w postaci tablicy LUT po przekształceniu do grayscale
            }
        }

        if ((Arrays.equals(red,blue)) && (Arrays.equals(red,green)))
            flag = false; //obraz jest monochromatyczny

        countMaximum(); //największa liczebność pikseli (a nie: największa wartość janości!)
    }

    private void countMaximum() {
        LongSummaryStatistics statRed = Arrays.stream(red).summaryStatistics();
        max[0] = statRed.getMax();
        LongSummaryStatistics statGreen = Arrays.stream(green).summaryStatistics();
        max[1] = statGreen.getMax();
        LongSummaryStatistics statBlue = Arrays.stream(blue).summaryStatistics();
        max[2] = statBlue.getMax();
        LongSummaryStatistics stat = Arrays.stream(max).summaryStatistics();
        maximum = stat.getMax();
    }

    public int findMinimumIntensity (long[] LUT) {
        int min;
        int i = 0;
        for (; i<LUT.length; i++) {
            if (LUT[i]>0) break;
        }
        min = i;
        return min;
    }

    public int findMaximumIntensity (long[] LUT) {
        int max;
        int i = 255;
        for (; i>=0; i--) {
            if (LUT[i]>0) break;
        }
        max = i;
        return max;
    }


    public long[] getBlue() {
        return blue; //zwraca tablicę LUT dla kanału niebieskiego
    }

    public long[] getGreen() {
        return green; //zwraca tablicę LUT dla kanału zielonego
    }

    public long[] getRed() {
        return red; //zwraca tablicę LUT dla kanału czerwonego
    }

    public long[] getIntensity() {
        return intensity; //zwraca tablicę LUT dla intensywności RGB
    }

    public boolean getFlag() {
        return this.flag;
    }

    public BarChart<String, Number> countHistogramChannel(long LUTtable[]){
        final CategoryAxis xAxis = new CategoryAxis();
        xAxis.setTickLabelsVisible(false); //nie pokazuje opisu dla osi x
        xAxis.setTickLength(0); //nie pokazuje kresek na osi x
        final NumberAxis yAxis = new NumberAxis(0, maximum,1000);
        final BarChart <String, Number> barChart = new BarChart<>(xAxis,yAxis);
        XYChart.Series series = new XYChart.Series(); //np. seriesRed
        for (int i = 0; i < 256; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i), LUTtable[i])); //np. red[i]
        }
        barChart.getData().add(series);
        configureBarChart(barChart);
        return barChart;
    }
    private void configureBarChart (BarChart<?,?> barChart) {
        barChart.setLegendVisible(false);
        barChart.setAnimated(false); //?
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.getXAxis().setVisible(false);
        barChart.getYAxis().setVisible(false);
        barChart.setBarGap(0);
        barChart.setCategoryGap(0);
        barChart.getStylesheets().addAll(getClass().getResource("myAppStyle.css").toExternalForm()); //styl standardowy (czarne słupki, transparentność)
    }

}

/**
 * RGB to Grayscale:
 * x=0.299r+0.587g+0.114b
 * x=0.2126r+0.7152g+0.0722b
 */

