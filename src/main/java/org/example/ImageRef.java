package org.example;

import java.awt.image.BufferedImage;

public class ImageRef {

    private final BufferedImage bufferedImage;
    private final String imageCosName;
    private final float imageXAxis;
    private final float imageYAxis;
    private final float imageWidth;
    private final float imageHeight;

    public ImageRef(BufferedImage bufferedImage, String imageCosName, float imageXAxis, float imageYAxis, float imageWidth, float imageHeight) {
        this.bufferedImage = bufferedImage;
        this.imageCosName = imageCosName;
        this.imageXAxis = imageXAxis;
        this.imageYAxis = imageYAxis;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }


    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }


    public float getImageXAxis() {
        return imageXAxis;
    }

    public float getImageYAxis() {
        return imageYAxis;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public float getImageHeight() {
        return imageHeight;
    }
}
