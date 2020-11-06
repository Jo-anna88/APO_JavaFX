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
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class Controller2 {
    final FileChooser fileChooser = new FileChooser();
    private static MyImage nimg = null;
    ArrayList<ImageInfo> arraylist = new ArrayList<>();
    //private MyImage img = null;
    //private Tab selectedTab;

    @FXML
    private BorderPane rootNode;
    @FXML
    private TabPane tabPane;

    private static TabPane sTabPane;

    public Controller2() {
        setExtFilters(fileChooser);
        configureFileChooser(fileChooser);
    }

    @FXML
    void chooseFile(ActionEvent event) throws ImageProcessingException, IOException {
        File file = fileChooser.showOpenDialog(rootNode.getScene().getWindow()); //argument showOpenDialog to Stage (tu: primaryStage)
        if (file != null) {
            openFile(file);
        }
    }
    private void setExtFilters(FileChooser chooser){
        chooser.getExtensionFilters().addAll(
                // new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG","*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("GIF","*.gif"),
                new FileChooser.ExtensionFilter("BMP","*.bmp")
        );
    }
    private static void configureFileChooser(final FileChooser chooser) {
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }

    private void openFile(File file) throws IOException, ImageProcessingException {
        int rotate = checkOrientation(file); //metoda sprawdza orientację wybraną podczas akwizycji (czy zdjęcie wykonywane było pionowo/poziomo/...)
        MyImage img = null;
        try {
            img = new MyImage(file.toURI().toURL().toString(), 0,0,true,false);
            //jeśli orientacja obrazu podczas akwizycji jest niestandardowa a jego wymiary większe niż wys. i szer. tabPane, to:
            if (((img.getHeight() > tabPane.getHeight()-10 || img.getWidth() > tabPane.getWidth()-10) && (rotate==0||rotate==180)) || ((img.getHeight()>tabPane.getWidth()-10 || img.getWidth()>tabPane.getHeight()-10)&&(rotate==90||rotate==270))) {
                img = fitImageToTabPane(file,img,rotate);
            }
            //Image image = new Image(new FileInputStream("url for the image));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        ImageView imgView = new ImageView(img);
        imgView.setRotate(rotate);
        Tab tab0 = new Tab(img.getName(), imgView);

        tabPane.getTabs().add(tab0); //wstawia nowe zdjęcie na końcu tabPane (na pocz.: add(0,tab0)
        tabPane.getSelectionModel().select(tabPane.getTabs().size()-1); //ustawia focus na nowo otwartym oknie

        File currentDir = file.getParentFile(); //pobiera info o pliku-rodzicu (o folderze, w którym przechowywany jest obraz)
        if ((currentDir != null) && (currentDir.exists())) {
            fileChooser.setInitialDirectory(currentDir); //ustawia initial directory jako ten, w którym zapisany jest f
        }

    }

    private MyImage fitImageToTabPane(File file, MyImage img, int rotate) throws MalformedURLException {
        if (rotate==0 || rotate==180) {
            if (img.getHeight() > tabPane.getHeight() - 10 && img.getWidth() <= tabPane.getWidth() - 10)
                img = new MyImage(file.toURI().toURL().toString(), 0, tabPane.getHeight() - 10, true, false);
            else if (img.getWidth() > tabPane.getWidth() - 10 && img.getHeight() <= tabPane.getHeight() - 10)
                img = new MyImage(file.toURI().toURL().toString(), tabPane.getWidth() - 10, 0, true, false);
            else // (img.getWidth() > tabPane.getWidth() - 10 && img.getHeight() > tabPane.getHeight())
                img = new MyImage(file.toURI().toURL().toString(), tabPane.getWidth() - 10, tabPane.getHeight() - 10, true, false);
        }
        else { //rotate==90 || rotate==270 i wys. jest tam zamieniona z szerokością
            if (img.getHeight() > tabPane.getWidth() - 10 && img.getWidth() <= tabPane.getHeight() - 10) //gdy szer. za duża (tu: img height)
                img = new MyImage(file.toURI().toURL().toString(), tabPane.getHeight() - 10, 0, true, false);
            else if (img.getWidth() > tabPane.getHeight() - 10 && img.getHeight() <= tabPane.getWidth() - 10) //gdy wysokość za duża (tu: img width)
                img = new MyImage(file.toURI().toURL().toString(), 0, tabPane.getWidth() - 10, true, false);
            else if (img.getHeight() > tabPane.getWidth() - 10 && img.getWidth() > tabPane.getHeight())
                img = new MyImage(file.toURI().toURL().toString(), tabPane.getHeight() - 10, tabPane.getWidth() - 10, true, false);
        }
        return img;
    }

    private int checkOrientation (File file) throws IOException, ImageProcessingException{
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
    void duplicateFile(ActionEvent event) throws MalformedURLException {
        MyImage img = returnSelectedImage();
        /////////////////////////////////////////////
        PixelReader pixelReader = img.getPixelReader();
        WritableImage writableImage
                = new WritableImage((int)img.getWidth(), (int)img.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < (int)img.getHeight(); y++){
            for (int x = 0; x < (int)img.getWidth(); x++){
                Color color = pixelReader.getColor(x,y);
                pixelWriter.setColor(x, y, color);
            }
        }

//        try {
//            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
//            ImageIO.write(bufferedImage, "png", file);
//        } catch (IOException ex) {
//            System.out.println(ex);
//        }
        //jak ustawić tu nowy obiekt MyImage i zapisać do niego writableImage??
        //jak zapisać duplikat (writableImage) w folderze źródłowym dodając "_copy(i)"
        ImageView nImgView = new ImageView (writableImage);
        Tab tab0 = new Tab(img.getName()+"_copy", nImgView);
        tabPane.getTabs().add(tab0); //wstawia nowe zdjęcie na końcu tabPane (na pocz.: add(0,tab0)
        tabPane.getSelectionModel().select(tabPane.getTabs().size()-1); //ustawia focus na nowo otwartym oknie

    }

    @FXML
    void exitProgram(ActionEvent event) { Platform.exit();}

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

    static MyImage returnSelectedImage() {
        Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
        ImageView imgView = (ImageView) selectedTab.getContent(); //pobiera Image View
        MyImage img = (MyImage) imgView.getImage(); //pobiera obraz (tworzy nową referencję do niego)
        return img;
    }

    void saveFileAs2(ActionEvent event) throws IOException {
        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(((ImageView)tabPane.getSelectionModel().getSelectedItem().getContent()).getImage(),null); //pobiera writableImage
        File file = fileChooser.showSaveDialog(rootNode.getScene().getWindow());
        ImageIO.write(bufferedImage,tabPane.getSelectionModel().getSelectedItem().getText(),file);
    }

    @FXML
    void saveFileAs(ActionEvent event) throws URISyntaxException, MalformedURLException { //zapisuje obraz pod nową nazwą (nie potrafi zapisać WritableImage)
//        try {
        MyImage img = returnSelectedImage();
//        }
//        catch (ClassCastException e) {
//            Tab selectedTab = sTabPane.getSelectionModel().getSelectedItem(); //pobiera wybraną zakładkę (tab)
//            ImageView imgView = (ImageView) selectedTab.getContent(); //pobiera Image View
//            Image writableimage = imgView.getImage();
//        }

        URI uri = (new URL(img.getUrl())).toURI();
//        String urlString = img.getUrl();
//        URL url = new URL(urlString);
//        URI uri = url.toURI();
        //e.g. urlString: file:/C:/Users/Asia/Pictures/Saved%20Pictures/Zosia-duplicate.jpg

        File f = new File(uri); //tworzy referencję f odnoszącą się do pliku, w którym przechowywany jest obraz
        String ext2 = getExtension2(f); //pobiera info o rozszerzeniu pliku

        File currentDir = f.getParentFile(); //pobiera info o pliku-rodzicu (o folderze, w którym przechowywany jest obraz)
        //currentDir.getPath(): C:\Users\Asia\Pictures\Saved Pictures (poprawnie - tzn. zamiast '%20' jest ' ')
        if ((currentDir != null) && (currentDir.exists())) {
            fileChooser.setInitialDirectory(currentDir); //ustawia initial directory jako ten, w którym zapisany jest f
        }
        fileChooser.setTitle("Save Image As...");
        fileChooser.setInitialFileName(img.getNameWithoutExt()+"(1)"); //ustawia nazwę dla pliku
        File file = fileChooser.showSaveDialog(rootNode.getScene().getWindow());

        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage( ((ImageView) tabPane.getSelectionModel().getSelectedItem().getContent()).getImage(),
                        null), ext2, file);
            } catch (IOException ex) {
                Logger.getLogger(
                        FileChooser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    ///https://www.baeldung.com/java-file-extension///
//    public Optional<String> getExtension(File file) {
//        String filename = file.getName();
//        return Optional.ofNullable(filename)
//                .filter(f -> f.contains("."))
//                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
//    }
    public static String getExtension2(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    @FXML
    void showHistogramPanel(ActionEvent event) throws IOException { //liczy histogram dla obrazu i wyświetla histogramPanel
        MyImage img = returnSelectedImage();
        img.makeHistogram();
        FXMLLoader loader;
        if (img.getIsRGB()) {
            loader = new FXMLLoader(getClass().getResource("histogramRGB.fxml"));
        }
        else {
            loader = new FXMLLoader(getClass().getResource("histogramMono.fxml"));
        }
        Parent root = loader.load();
        Stage nstage = new Stage();
        nstage.setTitle("histogram - " + img.getName());
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    @FXML
    public void initialize() {
        sTabPane = tabPane;
    }

    public void showHistogramStretchingPanel(ActionEvent actionEvent) throws IOException {
        MyImage img = returnSelectedImage();
        img.makeHistogram();
//        this.nimg = (MyImage)img.createImageAfterLinearHistogramStretching();
//        nimg.makeHistogram();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("histogramStretching.fxml"));
        Parent root = loader.load();
        Stage nstage = new Stage();
        nstage.setTitle("Linear Histogram Stretching - " + img.getName());
        nstage.setScene(new Scene(root));
        nstage.initOwner(rootNode.getScene().getWindow());
        nstage.show();
    }

    public static MyImage getNimg() {
        return nimg;
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



