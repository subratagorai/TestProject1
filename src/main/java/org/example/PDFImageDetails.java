package org.example;

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

    public void setXAxis(float xAxis) {
        this.xAxis = xAxis;
    }

    public float getYAxis() {
        return yAxis;
    }

    public void setYAxis(float yAxis) {
        this.yAxis = yAxis;
    }

    public float getScalingFactorX() {
        return scalingFactorX;
    }

    public void setScalingFactorX(float scalingFactorX) {
        this.scalingFactorX = scalingFactorX;
    }

    public float getScalingFactorY() {
        return scalingFactorY;
    }

    public void setScalingFactorY(float scalingFactorY) {
        this.scalingFactorY = scalingFactorY;
    }
}
