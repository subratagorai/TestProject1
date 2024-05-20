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
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.List;

public class GetImageLocationService extends PDFStreamEngine {

    private List<COSBase> currentOperands;
    private float[] imageLocation;

    public float[] getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(float[] imageLocation) {
        this.imageLocation = imageLocation;
    }




    public GetImageLocationService(List<COSBase> currentOperands) throws IOException
    {
        this.currentOperands = currentOperands;
        addOperator(new Concatenate(this));
        addOperator(new DrawObject(this));
        addOperator(new SetGraphicsStateParameters(this));
        addOperator(new Save(this));
        addOperator(new Restore(this));
        addOperator(new SetMatrix(this));
    }

    public float[] getImageLocation(PDPage page) throws IOException {
       this.processPage(page);
       return  this.getImageLocation();
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        String operation = operator.getName();

        if (OperatorName.DRAW_OBJECT.equals(operation))
        {
            COSName objectName = (COSName) operands.get( 0 );
            Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
            COSName currentObjectName = (COSName) currentOperands.get( 0 );
            if(objectName.getName().equals(currentObjectName.getName())){
                System.out.println("*******************************************************************");
                System.out.println("Found image [" + objectName.getName() + "]");
                // position in user space units. 1 unit = 1/72 inch at 72 dpi
                float xAxisUserSpaceUnit = ctmNew.getTranslateX();
                float yAxisUserSpaceUnit = ctmNew.getTranslateY();
                System.out.println("position in PDF = " + xAxisUserSpaceUnit + ", " + yAxisUserSpaceUnit + " in user space units");

                float[] imageLocation = {xAxisUserSpaceUnit, yAxisUserSpaceUnit};
                this.setImageLocation(imageLocation);
            }
        }
        else
        {
            super.processOperator( operator, operands);
        }

    }
}
