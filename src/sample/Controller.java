package sample;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.image.*;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class Controller {
    final FileChooser fileChooser = new FileChooser();
    static Histogram histo;
    //static Histogram stretchHisto;
    static Histogram equalHisto;
    static Histogram DISEqualHisto;

    @FXML
    private BorderPane rootNode;
    @FXML
    private TabPane tabPane;

    static TabPane sTabPane;
    static FileChooser sFileChooser;

    public Controller() {
        setExtFilters(fileChooser);
        configureFileChooser(fileChooser);
        sFileChooser=fileChooser;
    }

    @FXML
    void chooseFile(ActionEvent event) throws ImageProcessingException, IOException {
        File file = fileChooser.showOpenDialog(rootNode.getScene().getWindow()); //argument showOpenDialog to Stage (tu: primaryStage)
        if (file != null) {
            openFile(file);
        }
    }

    void setExtFilters(FileChooser chooser) {
        chooser.getExtensionFilters().addAll(
                // new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );
    }

    private static void configureFileChooser(final FileChooser chooser) {
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }

    private void openFile(File file) throws IOException, ImageProcessingException {
        int rotate = checkOrientation(file); //metoda sprawdza orientację wybraną podczas akwizycji (czy zdjęcie wykonywane było pionowo/poziomo/...)
        Image img = null;
        try {
            img = new Image(file.toURI().toURL().toString(), 0, 0, true, false);
            //jeśli orientacja obrazu podczas akwizycji jest niestandardowa a jego wymiary większe niż wys. i szer. tabPane, to:
            if (((img.getHeight() > tabPane.getHeight() - 10 || img.getWidth() > tabPane.getWidth() - 10) && (rotate == 0 || rotate == 180)) || ((img.getHeight() > tabPane.getWidth() - 10 || img.getWidth() > tabPane.getHeight() - 10) && (rotate == 90 || rotate == 270))) {
                img = fitImageToTabPane(file, img, rotate);
            }
            //Image image = new Image(new FileInputStream("url for the image));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //ImageInfo info = new ImageInfo(img);
        //arraylist.add(info);
        ImageView imgView = new ImageView(img);
        imgView.setRotate(rotate);
        Tab tab0 = new Tab(Functionality.getImageName(img), imgView);
        //tab0.setUserData(file);

        tabPane.getTabs().add(tab0); //wstawia nowe zdjęcie na końcu tabPane (na pocz.: add(0,tab0)
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1); //ustawia focus na nowo otwartym oknie

        File currentDir = file.getParentFile(); //pobiera info o pliku-rodzicu (o folderze, w którym przechowywany jest obraz)
        if ((currentDir != null) && (currentDir.exists())) {
            fileChooser.setInitialDirectory(currentDir); //ustawia initial directory jako ten, w którym zapisany jest f
        }
    }

    private Image fitImageToTabPane(File file, Image img, int rotate) throws MalformedURLException {
        if (rotate == 0 || rotate == 180) {
            if (img.getHeight() > tabPane.getHeight() - 10 && img.getWidth() <= tabPane.getWidth() - 10)
                img = new Image(file.toURI().toURL().toString(), 0, tabPane.getHeight() - 10, true, false);
            else if (img.getWidth() > tabPane.getWidth() - 10 && img.getHeight() <= tabPane.getHeight() - 10)
                img = new Image(file.toURI().toURL().toString(), tabPane.getWidth() - 10, 0, true, false);
            else // (img.getWidth() > tabPane.getWidth() - 10 && img.getHeight() > tabPane.getHeight())
                img = new Image(file.toURI().toURL().toString(), tabPane.getWidth() - 10, tabPane.getHeight() - 10, true, false);
        } else { //rotate==90 || rotate==270 i wys. jest tam zamieniona z szerokością
            if (img.getHeight() > tabPane.getWidth() - 10 && img.getWidth() <= tabPane.getHeight() - 10) //gdy szer. za duża (tu: img height)
                img = new Image(file.toURI().toURL().toString(), tabPane.getHeight() - 10, 0, true, false);
            else if (img.getWidth() > tabPane.getHeight() - 10 && img.getHeight() <= tabPane.getWidth() - 10) //gdy wysokość za duża (tu: img width)
                img = new Image(file.toURI().toURL().toString(), 0, tabPane.getWidth() - 10, true, false);
            else if (img.getHeight() > tabPane.getWidth() - 10 && img.getWidth() > tabPane.getHeight())
                img = new Image(file.toURI().toURL().toString(), tabPane.getHeight() - 10, tabPane.getWidth() - 10, true, false);
        }
        return img;
    }

    private int checkOrientation(File file) throws IOException, ImageProcessingException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        //https://www.impulseadventure.com/photo/exif-orientation.html
        int orientation = 1;
        try {
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION); //dla plików, które nie mają tej informacji pojawia się null pointer exception
        } catch (MetadataException | NullPointerException e) {
            //logger.warn("Could not get orientation"); https://www.edureka.co/blog/logger-in-java
        }
        int rotate;
        switch (orientation) {
            case 3:
                rotate = 180;
                break;
            case 6:
                rotate = 90;
                break;
            case 8:
                rotate = 270;
                break;
            default:
                rotate = 0;
        }
        return rotate;
    }

    @FXML
    void duplicateFile(ActionEvent event) throws MalformedURLException, URISyntaxException {
        Image img = returnSelectedImage(); //to może być Image lub WritableImage
        /////////////////////////////////////////////
        PixelReader pixelReader = img.getPixelReader();
        WritableImage writableImage
                = new WritableImage((int) img.getWidth(), (int) img.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < (int) img.getHeight(); y++) {
            for (int x = 0; x < (int) img.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }

//        try {
//            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
//            ImageIO.write(bufferedImage, "png", file);
//        } catch (IOException ex) {
//            System.out.println(ex);
//        }
        //jak ustawić tu nowy obiekt Image i zapisać do niego writableImage??
        //jak zapisać duplikat (writableImage) w folderze źródłowym dodając "_copy(i)"
        ImageView nImgView = new ImageView(writableImage);
        Tab tab0 = new Tab(Functionality.getNameWithoutExt(img) + "_copy." + Functionality.getExtension2(img), nImgView); //zdjecie.jpg -> zdjecie_copy.jpg
        //File output =
        //ustawić do Tab Filepath!
        tabPane.getTabs().add(tab0); //wstawia nowe zdjęcie na końcu tabPane (na pocz.: add(0,tab0)
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1); //ustawia focus na nowo otwartym oknie
        //ImageInfo info = new ImageInfo(writableImage);
    }

    @FXML
    void exitProgram(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void saveFile(ActionEvent event) throws FileNotFoundException {
//        Image img = ((ImageView)tabPane.getSelectionModel().getSelectedItem().getContent()).getImage();
//        try {
//            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(img, null);
//            ImageIO.write(bufferedImage, "png", file);
//        } catch (IOException ex) {
//            System.out.println(ex);
//        }
    }

    static Image returnSelectedImage() {
        Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
        ImageView imgView = (ImageView) selectedTab.getContent(); //pobiera Image View
        return imgView.getImage(); //pobiera obraz (tworzy nową referencję do niego)
    }

    void saveFileAs2(ActionEvent event) throws IOException {
        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(((ImageView) tabPane.getSelectionModel().getSelectedItem().getContent()).getImage(), null); //pobiera writableImage
        File file = fileChooser.showSaveDialog(rootNode.getScene().getWindow());
        ImageIO.write(bufferedImage, "png", file);
    }

    @FXML
    void saveFileAs(ActionEvent event) throws URISyntaxException {

        Image img = returnSelectedImage(); //to może być WritableImage lub Image
        String ext2;

        try {
            File currentDir = Functionality.getParentFile(img); //dla WritableImage tu byłby błąd
            ext2 = Functionality.getExtension2(img); //np.jpg
            //currentDir.getPath(): C:\Users\Asia\Pictures\Saved Pictures (poprawnie - tzn. zamiast '%20' jest ' ')
            fileChooser.setInitialFileName(Functionality.getNameWithoutExt(img) + "(1)"); //ustawia nazwę dla pliku
            if ((currentDir != null) && (currentDir.exists())) {
                fileChooser.setInitialDirectory(currentDir); //ustawia initial directory jako ten, w którym zapisany jest f
            }
        } catch (MalformedURLException e) { //dla WritableImage
            configureFileChooser(fileChooser);
            //currentDir = fileChooser.getInitialDirectory();
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            String tabName = selectedTab.getText(); //np.zdjecie_copy.jpg
            ext2 = "png";
            //ext2 = Functionality.getExtension2(tabName); //np.jpg
            fileChooser.setInitialFileName(Functionality.getNameWithoutExt(tabName)); //ustawia nazwę dla pliku na podstawie tabName
        }

        fileChooser.setTitle("Save Image As...");
        File file = fileChooser.showSaveDialog(rootNode.getScene().getWindow());

        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(((ImageView) tabPane.getSelectionModel().getSelectedItem().getContent()).getImage(),
                        null), ext2, file); //domyślnie ustawia .jpg (dlaczego?)
            } catch (IOException ex) {
                Logger.getLogger(
                        FileChooser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    ////////////////////////////////////////////////
    @FXML
    void toGrayscale(ActionEvent event) {
        Image img = returnSelectedImage();
        Image dst = Functionality.rgbToGrayscale(img);
        ImageView nImgView = new ImageView(dst);
        Tab tab0;
        try {
        tab0 = new Tab(Functionality.getNameWithoutExt(img) + "_grayscale", nImgView);} //zdjecie.jpg -> zdjecie_stretched.jpg
            catch(NullPointerException e){
                Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
                tab0 = new Tab(selectedTab.getText()+"_grayscale",nImgView);
            }
        tabPane.getTabs().add(tab0);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    void showHistogramPanel(ActionEvent event) throws IOException { //liczy histogram dla obrazu i wyświetla histogramPanel
        Image img = returnSelectedImage();
        histo = new Histogram(img);

        FXMLLoader loader;
        if (histo.getFlag()) {
            loader = new FXMLLoader(getClass().getResource("histogramRGB.fxml"));
        }
        else {
            loader = new FXMLLoader(getClass().getResource("histogramMono.fxml"));
        }
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("histogram - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("histogram - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showHistogramStretchingPanel(ActionEvent actionEvent) throws IOException, URISyntaxException {
        Image img = returnSelectedImage();
        histo = new Histogram(img);
        //Image nImg = Functionality.createImageAfterLinearHistogramStretching(img);
        //stretchHisto = new Histogram(nImg);
        //ImageView nImgView = new ImageView(nImg);
        //Tab tab0 = new Tab(Functionality.getNameWithoutExt(img) + "_stretched." + Functionality.getExtension2(img), nImgView); //zdjecie.jpg -> zdjecie_stretched.jpg
        //tabPane.getTabs().add(tab0);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("histogramStretching.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Linear Histogram Stretching - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Linear Histogram Stretching - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showHistogramStretchingWithRangePanel(ActionEvent actionEvent) throws IOException, URISyntaxException {
        Image img = returnSelectedImage();
        histo = new Histogram(img);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("histogramStretchingWithRange.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Rozciąganie histogramu dla zadanego zakresu - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Rozciąganie histogramu dla zadanego zakresu - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showHistogramEqualizationPanel(ActionEvent actionEvent) throws IOException, URISyntaxException {
        Image img = returnSelectedImage();
        histo = new Histogram(img);
        //Image nImg = Functionality.createImageAfterHistogramEqualization(img,histo);
        Image nImg = Functionality.createImageAfterHistogramEqualizationAPO(img,histo);
        equalHisto = new Histogram(nImg);
        //ImageView nImgView = new ImageView(nImg);
        //Tab tab0 = new Tab (Functionality.getNameWithoutExt(img) + "_equalized." + Functionality.getExtension2(img), nImgView);
        //tabPane.getTabs().add(tab0);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("histogramEqualization.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Histogram equalization - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Histogram equalization - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    void showHistogramSelectedEqualizationPanel (ActionEvent actionEvent) throws IOException, URISyntaxException {
        Image img = returnSelectedImage();
        histo = new Histogram(img);
        Image nImg = Functionality.createImageAfterSelectedHistogramEqualization(img, histo);
        DISEqualHisto = new Histogram(nImg);
        //ImageView nImgView = new ImageView(nImg);
        //Tab tab0 = new Tab(Functionality.getNameWithoutExt(img) + "_equalized_DISHE." + Functionality.getExtension2(img), nImgView); //zdjecie.jpg -> zdjecie_copy.jpg
        //tabPane.getTabs().add(tab0);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("histogramEqualizationDISHE.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Histogram Equalization (met.DISHE)- " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Histogram Equalization (met.DISHE) - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showProgowaniePanel1(ActionEvent actionEvent) throws IOException {
        Image img = returnSelectedImage();
        histo = new Histogram(img);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("progowanieBinarne.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Progowanie - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Progowanie - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showProgowaniePanel2(ActionEvent actionEvent) throws IOException {
        Image img = returnSelectedImage();
        histo = new Histogram(img);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("progowanieZachowaniePoziomowSzarosci.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Progowanie - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Progowanie - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showRedukcjaPanel(ActionEvent actionEvent) throws IOException {
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader((getClass().getResource("redukcjaPoziomowSzarosci.fxml")));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Redukcja poziomów szarości - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Redukcja poziomów szarości - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void inverse(ActionEvent actionEvent) throws MalformedURLException, URISyntaxException {
        Image img = returnSelectedImage();
        Image nImg = Functionality.invert(img);
        ImageView nImgView = new ImageView(nImg);
        Tab tab0;
        try {
            tab0 = new Tab(Functionality.getNameWithoutExt(img) + "_invert." + Functionality.getExtension2(img), nImgView); //zdjecie.jpg -> zdjecie_copy.jpg
        } catch (NullPointerException e) {
            tab0 = new Tab("image_invert.png",nImgView);
        }
        tabPane.getTabs().add(tab0);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1); //ustawia focus na nowo otwartym oknie
    }
    public void showMedianPanel(ActionEvent actionEvent) throws IOException {
        Image img = returnSelectedImage();
//        Mat src = new Mat(), dst = new Mat();
//        try {
//            String filepath = img.getUrl();
//            src = Imgcodecs.imread(filepath, Imgcodecs.IMREAD_COLOR);
//        } catch () {
//
//        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Median.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Median - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Median - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    public void showSmoothingPanel(ActionEvent actionEvent) throws IOException{
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Smoothing.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Smoothing - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Smoothing - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    public void showSharpenPanel(ActionEvent actionEvent) throws IOException{ //LAPLASJAN
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Sharpening.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Sharpening - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Sharpening - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    public void showEdgeDetectionPanel1(ActionEvent actionEvent) throws IOException{
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Prewitt.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Edge Detection - Prewitt - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Edge Detection - Prewitt - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    public void showEdgeDetectionPanel2(ActionEvent actionEvent) throws IOException{
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Sobel_Canny.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Edge Detection - Sobel/Canny - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Edge Detection - Sobel/Canny - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    public void showLogicOpPanel(ActionEvent actionEvent) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LogicOp.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        nstage.setTitle("Logic operations");
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    public void showAdaptiveThresholdPanel(ActionEvent actionEvent) throws IOException{
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("adaptiveThreshold.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Adaptive Thresholding - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Adaptive Thresholding - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showOtsuThresholdPanel(ActionEvent actionEvent) throws IOException{
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("otsuThreshold.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Otsu Thresholding - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Otsu Thresholding - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    public void showTexturePanel(ActionEvent actionEvent) throws IOException{
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("segmTexture.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Segmentation - Texture - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Segmentation - Texture - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void showWatershedPanel(ActionEvent actionEvent) throws IOException{
        Image img = returnSelectedImage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("segmWatershed.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        try { //gdy mamy do czynienia z Image
            nstage.setTitle("Segmentation - Watershed - " + Functionality.getImageName(img));
        } catch (NullPointerException e) { //gdy mamy do czynienia z WritableImage
            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
            nstage.setTitle("Segmentation - Watershed - " + selectedTab.getText());
        }
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }
    @FXML
    public void initialize() {
        sTabPane = tabPane;

    }
    //////////////////////////////////////////
    public static Mat imageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];

        PixelReader reader = image.getPixelReader();
        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        mat.put(0, 0, buffer);
        return mat;
    }
}


///////////////////////////////////////////////
//File file = chooser.showSaveDialog(getScene().getWindow());
//    if (file != null) {
//            try {
//            ExportUtils.writeAsJPEG(this.canvas.getChart(), (int) getWidth(),
//            (int) getHeight(), file);
//            } catch (IOException ex) {
//            // FIXME: show a dialog with the error
//            throw new RuntimeException(ex);
//            }
//            }


