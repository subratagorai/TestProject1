package org.example;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.List;

public class GetImageDetailsService extends PDFStreamEngine {

    private final List<COSBase> currentOperands;
    private PDFImageDetails pdfImageDetails;


    public PDFImageDetails getPdfImageDetails() {
        return pdfImageDetails;
    }

    public void setPdfImageDetails(PDFImageDetails pdfImageDetails) {
        this.pdfImageDetails = pdfImageDetails;
    }

    public GetImageDetailsService(List<COSBase> currentOperands) throws IOException
    {
        this.currentOperands = currentOperands;
        addOperator(new Concatenate(this));
        addOperator(new DrawObject(this));
        addOperator(new SetGraphicsStateParameters(this));
        addOperator(new Save(this));
        addOperator(new Restore(this));
        addOperator(new SetMatrix(this));
    }

    public PDFImageDetails getImageDetailsFromPage(PDPage page) throws IOException {
       this.processPage(page);
       return this.getPdfImageDetails();
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        String operation = operator.getName();

        if (OperatorName.DRAW_OBJECT.equals(operation))
        {
            COSName objectName = (COSName) operands.get( 0 );
            COSName currentObjectName = (COSName) currentOperands.get( 0 );
            if(objectName.getName().equals(currentObjectName.getName())){
                System.out.println("*******************************************************************");
                System.out.println("Found image [" + objectName.getName() + "]");
                PDXObject xobject = getResources().getXObject( objectName );
                PDImageXObject pdImageXObject = (PDImageXObject)xobject;
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                float imageXScale = ctmNew.getScalingFactorX();
                float imageYScale = ctmNew.getScalingFactorY();
                int imageWidth = pdImageXObject.getWidth();
                int imageHeight = pdImageXObject.getHeight();
                double imageWidthMilliDouble = imageWidth * 25.4 / 72;
                double imageHeightMilliDouble = imageHeight * 25.4 / 72;
                int imageWidthMilli = (int) Math.round(imageWidthMilliDouble);
                int imageHeightMilli = (int) Math.round(imageHeightMilliDouble);
                // position in user space units. 1 unit = 1/72 inch at 72 dpi
                float xAxisUserSpaceUnit = ctmNew.getTranslateX();
                float yAxisUserSpaceUnit = ctmNew.getTranslateY();
                //position in Millis
                float xAxisUserSpaceUnitMilli = (float) Math.round(xAxisUserSpaceUnit * 25.4 /72);
                float yAxisUserSpaceUnitMilli = (float) Math.round(yAxisUserSpaceUnit * 25.4 /72);
                System.out.println("position in PDF = " + xAxisUserSpaceUnit + ", " + yAxisUserSpaceUnit + " in user space units");
                System.out.println("position in PDF = " + xAxisUserSpaceUnitMilli + ", " + yAxisUserSpaceUnitMilli + " in millis");
                // raw size in pixels
                System.out.println("raw image size  = " + imageWidth + ", " + imageHeight + " in pixels");
                System.out.println("raw image size  = " + imageWidthMilli + ", " + imageHeightMilli + " in millimeters");
                // displayed size in user space units
                System.out.println("displayed size  = " + imageXScale + ", " + imageYScale + " in user space units");
                this.setPdfImageDetails(new PDFImageDetails(xAxisUserSpaceUnit, yAxisUserSpaceUnit, imageXScale, imageYScale));
            }
        }
        else
        {
            super.processOperator( operator, operands);
        }

    }
}
