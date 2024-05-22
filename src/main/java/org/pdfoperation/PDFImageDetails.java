package org.pdfoperation;

/**
 * This class represents the details of an image in a PDF document.
 * It includes the position of the image (x and y coordinates) and the scaling factors for the image's width and height.
 * The position and scaling factors are in user space units as defined by the PDF specification.
 *
 * @author SUBRATAG
 */
public class PDFImageDetails {
    float xAxis;
    float yAxis;
    float scalingFactorX;
    float scalingFactorY;

    public PDFImageDetails(float xAxisUserSpaceUnit, float yAxisUserSpaceUnit, float imageWidthScaleX, float imageHeightScaleY) {
        this.xAxis = xAxisUserSpaceUnit;
        this.yAxis = yAxisUserSpaceUnit;
        this.scalingFactorX = imageWidthScaleX;
        this.scalingFactorY = imageHeightScaleY;
    }

    public float getXAxis() {
        return xAxis;
    }


    public float getYAxis() {
        return yAxis;
    }



    public float getScalingFactorX() {
        return scalingFactorX;
    }

    public float getScalingFactorY() {
        return scalingFactorY;
    }

}
