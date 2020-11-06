package sample;

import javafx.scene.image.Image;

public class ImageInfo {
    private Image img;
    private String name;
    String url;

    public ImageInfo(Image i) {
        img = i;
        try {
            this.name = setName();
        } catch (NullPointerException e) {
            this.name="";
        }
    }
    ////////////////////////////////////////////////////////////////////
    private String setName() {
        String fname = img.getUrl();
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
}
