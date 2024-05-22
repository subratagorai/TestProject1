package org.pdfoperation;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddBarCodeMain {
    private static final String BASE_DIR = "/Users/subratag/Documents/git/github/pdfoperations/src/main/resources/";
    private static final String INPUT_DIR = BASE_DIR + "input";
    private static final String INPUT_FILE="Shipping.pdf";
    private static final String OUTPUT_DIR = BASE_DIR + "output";
    private static final String OUT_PUT_FILE = "AddedBarcodeAndBlankPage_"+INPUT_FILE;
    private static final Logger LOGGER = Logger.getLogger(PDFSplitterMain.class.getName());
    public static void main(String[] args) {
        try {

            PDDocument document = Loader.loadPDF(new File(INPUT_DIR+"/"+INPUT_FILE));
            int pageCount = document.getNumberOfPages();
            LOGGER.log(Level.INFO,"The number of pages in the PDF is: " + pageCount);
            PDPage blankPage = new PDPage();
            document.addPage(blankPage);
            pageCount = document.getNumberOfPages();
           // System.out.println("The number of pages in the PDF after adding blank page is: " + pageCount);
            //document.save(new File("/Users/subratag/Documents/git/be/Test2/src/main/resources/out/OLContent_blankPage.pdf"));
            // add barcode to the last page
            addBarCodeToPDF(document,pageCount);
            document.save(new File(OUTPUT_DIR + "/" + OUT_PUT_FILE));
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void addBarCodeToPDF(PDDocument document, int pageCount)
    {
        BufferedImage barcodeImage = createBarCodeImage();
        if(barcodeImage != null)
        {
            try {
                PDImageXObject barcodeXObject = LosslessFactory.createFromImage(document, barcodeImage);
                // For adding in empty page
                PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(pageCount-1),
                        PDPageContentStream.AppendMode.APPEND, true);
                contentStream.drawImage(barcodeXObject,100,100);
                contentStream.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static BufferedImage createBarCodeImage()
    {
        BufferedImage barcodeImage = null;
        String barcodeText = "Hello, World!";
        Code128Writer barcodeWriter = new Code128Writer();
        // height
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, 50, 50);
        barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        barcodeImage = rotate(barcodeImage,90);
        return  barcodeImage;
    }

    public static BufferedImage rotate(BufferedImage img, double angle) {
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.PI / 2, img.getWidth() / 2, img.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage rotatedImage = op.filter(img, null);
        return rotatedImage;
    }
}
