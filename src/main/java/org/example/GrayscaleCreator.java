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
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GrayscaleCreator extends PDFStreamEngine {

    public GrayscaleCreator() throws IOException
    {
        addOperator(new Concatenate(this));
        addOperator(new DrawObject(this));
        addOperator(new SetGraphicsStateParameters(this));
        addOperator(new Save(this));
        addOperator(new Restore(this));
        addOperator(new SetMatrix(this));
    }

    public void handlePage(PDPage page) throws IOException {
        processPage(page);

    }


    @Override
    protected void processOperator( Operator operator, List<COSBase> operands) throws IOException{
        String operation = operator.getName();
        if (OperatorName.DRAW_OBJECT.equals(operation))
        {
            COSName objectName = (COSName) operands.get( 0 );
            PDXObject xobject = getResources().getXObject( objectName );
            if( xobject instanceof PDImageXObject)
            {
                PDImageXObject pdImageXObject = (PDImageXObject)xobject;
                int imageWidth = pdImageXObject.getWidth();
                int imageHeight = pdImageXObject.getHeight();
                System.out.println("*******************************************************************");
                System.out.println("Found image [" + objectName.getName() + "]");

                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                float imageXScale = ctmNew.getScalingFactorX();
                float imageYScale = ctmNew.getScalingFactorY();

                // position in user space units. 1 unit = 1/72 inch at 72 dpi
                float xAxisUserSpaceUnit = ctmNew.getTranslateX();
                float yAxisUserSpaceUnit = ctmNew.getTranslateY();
                System.out.println("position in PDF = " + xAxisUserSpaceUnit + ", " + yAxisUserSpaceUnit + " in user space units");
                // raw size in pixels
                System.out.println("raw image size  = " + imageWidth + ", " + imageHeight + " in pixels");
                // displayed size in user space units
                System.out.println("displayed size  = " + imageXScale + ", " + imageYScale + " in user space units");
                // displayed size in inches at 72 dpi rendering
                imageXScale /= 72;
                imageYScale /= 72;
                System.out.println("displayed size  = " + imageXScale + ", " + imageYScale + " in inches at 72 dpi rendering");
                // displayed size in millimeters at 72 dpi rendering
                imageXScale *= 25.4f;
                imageYScale *= 25.4f;
                System.out.println("displayed size  = " + imageXScale + ", " + imageYScale + " in millimeters at 72 dpi rendering");

                BufferedImage bufferedImage = pdImageXObject.getImage();
                // get image's width and height
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
                // convert to grayscale
                for (int i = 0; i < pixels.length; i++) {

                    // Here i denotes the index of array of pixels
                    // for modifying the pixel value.
                    int p = pixels[i];

                    int a = (p >> 24) & 0xff;
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;

                    // calculate average
                    int avg = (r + g + b) / 3;

                    // replace RGB value with avg
                    p = (a << 24) | (avg << 16) | (avg << 8) | avg;

                    pixels[i] = p;
                }
                bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
                // write image
                try {
                    File f = new File( GreyScalePDFPrinter.BASE_DIR +"/"+ objectName+ Math.random() + ".png");

                    ImageIO.write(bufferedImage, "png", f);
                } catch (IOException e) {
                    System.out.println(e);
                }

                System.out.println();

            }
            else if(xobject instanceof PDFormXObject)
            {
                PDFormXObject form = (PDFormXObject)xobject;
                showForm(form);
            }
        }
        else
        {
            super.processOperator( operator, operands);
        }
    }

}
