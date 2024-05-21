package org.example;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConvertToGrayScale {
    public static final String BASE_DIR = "/Users/subratag/Documents/git/github/TestProject1/src/main/resources/input/";
    private static final String INPUT_FILE="Shipping.pdf";
    private static final String OUT_PUT_FILE = "Updated_"+INPUT_FILE;

    public void convertPDFToGrayScaleUsingPDFBox() {
        // Load PDF from Base Directory using PDFBox
        try {
            File file = new File(BASE_DIR + "/" + INPUT_FILE);
            PDDocument document = Loader.loadPDF(file);
            System.out.println("PDF loaded");

            int pageCount = document.getPages().getCount();
            for (int i = 0; i < pageCount; i++) {
                List<ImageRef> imageRefs = new ArrayList<>();
                PDPage currentPage = document.getPage(i);
                PDFStreamParser parser = new PDFStreamParser(currentPage);
                // loop through all the tokens in the stream
                List<Object> pageTokens = parser.parse();
                System.out.println("pageTokens size: " + pageTokens.size());
                List<Object> editedPageTokens = new ArrayList<>();
                Set<String> distinctOperator = new HashSet<>();
                for (int counter = 0; counter < pageTokens.size(); counter++) {
                    Object token = pageTokens.get(counter);
                    if (token instanceof Operator) {
                        //TODO Need to check cs token to be removed or not
                        distinctOperator.add(((Operator) token).getName());
                        //System.out.println("Token value counter " + counter + " " + ((Operator) token).getName());
                         if (((Operator) token).getName().equals("k")) {
                            //System.out.println("Color Operator: " + ((Operator) token).getName() + " for token counter: " + counter);
                            convertCMYKToRGBToGray(pageTokens, editedPageTokens, counter);
                            editedPageTokens.add(Operator.getOperator("g"));
                        } else if (((Operator) token).getName().equals("K")) {
                           // System.out.println("Color Operator: " + ((Operator) token).getName() + " for token counter: " + counter);
                            convertCMYKToRGBToGray(pageTokens, editedPageTokens, counter);
                            editedPageTokens.add(Operator.getOperator("G"));
                        }
                        else if (((Operator) token).getName().equals("rg")) {
                            //System.out.println("Color Operator: " + ((Operator) token).getName() + " for token counter: " + counter);
                            convertGBToGray(pageTokens, editedPageTokens, counter);
                            editedPageTokens.add(Operator.getOperator("g"));
                        }
                        else if (((Operator) token).getName().equals("RG")) {
                            //System.out.println("Color Operator: " + ((Operator) token).getName() + " for token counter: " + counter);
                            convertGBToGray(pageTokens, editedPageTokens, counter);
                            editedPageTokens.add(Operator.getOperator("G"));
                        }
                        /*
                        Logic for Images
                         */
                        //Process tokens for images
                        else if (OperatorName.DRAW_OBJECT.equals(((Operator) token).getName()))
                        {
                            COSName cosName = null;
                            Object cosNameObj = pageTokens.get(counter - 1);
                            if (cosNameObj instanceof  COSName) {
                                cosName = (COSName) cosNameObj;
                            }
                            List<COSBase> operands=  new ArrayList<>();
                            operands.add(cosName);
                            PDXObject xObject = currentPage.getResources().getXObject((COSName) cosNameObj);
                            if(xObject instanceof PDImageXObject pdImageXObject)

                            {
                                GetImageDetailsService imageLocationService = new GetImageDetailsService(operands);
                                PDFImageDetails pdfImageDetails = imageLocationService.getImageDetailsFromPage(currentPage);

                                BufferedImage bufferedImage = pdImageXObject.getImage();
                                // get image's width and height
                                int width = bufferedImage.getWidth();
                                int height = bufferedImage.getHeight();
                                int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
                                // convert pixels to grayscale
                                for (int k = 0; k < pixels.length; k++) {

                                    // Here k denotes the index of array of pixels
                                    // for modifying the pixel value.
                                    int p = pixels[k];

                                    int a = (p >> 24) & 0xff;
                                    int r = (p >> 16) & 0xff;
                                    int g = (p >> 8) & 0xff;
                                    int b = p & 0xff;

                                    // calculate average
                                    int avg = (r + g + b) / 3;

                                    // replace RGB value with avg
                                    p = (a << 24) | (avg << 16) | (avg << 8) | avg;

                                    pixels[k] = p;
                                }
                                bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
                                // remove image token from the list
                                editedPageTokens.remove(editedPageTokens.size() -1);
                                // add list of images to be added in the page
                                imageRefs.add(new ImageRef(bufferedImage,cosName.getName(),pdfImageDetails.getXAxis(), pdfImageDetails.getYAxis(), pdfImageDetails.getScalingFactorX(), pdfImageDetails.getScalingFactorY()));

                                // write image
//                                try {
//                                    File f = new File( GrayScalePDFPrinter.BASE_DIR +"/"+ cosName+ Math.random() + ".png");
//
//                                    ImageIO.write(bufferedImage, "png", f);
//                                } catch (IOException e) {
//                                    System.out.println(e);
//                                }

                                System.out.println();

                            }
                            else {
                                editedPageTokens.add(token);
                            }
                        }
                        else if (OperatorName.FILL_NON_ZERO.equals(((Operator) token).getName())){
                            //System.out.println("Fill Non zero: " + ((Operator) token).getName());
                            editedPageTokens.add(token);
                        }
                        else {
                            editedPageTokens.add(token);
                        }

                    }
                    else {
                        editedPageTokens.add(token);
                    }

                }
                System.out.println("Distinct operators: " + distinctOperator.toString());
                PDStream updatedPageContents = new PDStream(document);

                OutputStream outputStream = updatedPageContents.createOutputStream(COSName.FLATE_DECODE);
                ContentStreamWriter contentWriter = new ContentStreamWriter(outputStream);
                contentWriter.writeTokens(editedPageTokens);
                currentPage.setContents(updatedPageContents);
                outputStream.close();

                for (ImageRef imageRef: imageRefs) {
                    PDImageXObject imageGrayScale =  LosslessFactory.createFromImage(document, imageRef.getBufferedImage());
                    PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true,true);
                    contentStream.drawImage(imageGrayScale, imageRef.getImageXAxis(), imageRef.getImageYAxis(), imageRef.getImageWidth(), imageRef.getImageHeight());
                    contentStream.close();
                }
            }
            document.save(BASE_DIR + OUT_PUT_FILE);
            // Remember to close the PDF document
            document.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void convertCMYKToRGBToGray(List<Object> pageTokens, List<Object> editedPageTokens, int counter) {
        Object o1 = pageTokens.get(counter - 4);
        Object o2 = pageTokens.get(counter - 3);
        Object o3 = pageTokens.get(counter - 2);
        Object o4 = pageTokens.get(counter - 1);

      //  System.out.println("CMYK values: " + o1 + " " + o2 + " " + o3 + " " + o4);

        float cyan;
        float magenta;
        float yellow;
        float black;

        if (o1 instanceof COSFloat) {
            cyan = ((COSFloat) o1).floatValue();
        } else {
            cyan = ((COSInteger) o1).floatValue();
        }

        if (o2 instanceof COSFloat) {
            magenta = ((COSFloat) o2).floatValue();
        } else {
            magenta = ((COSInteger) o2).floatValue();
        }

        if (o3 instanceof COSFloat) {
            yellow = ((COSFloat) o3).floatValue();
        } else {
            yellow = ((COSInteger) o3).floatValue();
        }

        if (o4 instanceof COSFloat) {
            black = ((COSFloat) o4).floatValue();
        } else {
            black = ((COSInteger) o4).floatValue();
        }

        int red = Math.round(255 * (1 - cyan) * (1 - black));
        int green = Math.round(255 * (1 - magenta) * (1 - black));
        int blue = Math.round(255 * (1 - yellow) * (1 - black));

        //System.out.println("RGB values: " + red + " " + green + " " + blue);

        // convert RGB to grayscale
        int gray = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);

        //System.out.println("Gray value: " + gray);

        // Add grayscale, gray
        float gray1 = (float) gray / 255;
        BigDecimal bd = new BigDecimal(Float.toString(gray1));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        gray1 = bd.floatValue();
        //System.out.println("Gray1 value: " + gray1);

        // remove CMYK
        editedPageTokens.remove(editedPageTokens.size() - 1);
        editedPageTokens.remove(editedPageTokens.size() - 1);
        editedPageTokens.remove(editedPageTokens.size() - 1);
        editedPageTokens.remove(editedPageTokens.size() - 1);

        // Add grayscale, gray
        editedPageTokens.add(new COSFloat(gray1));
    }


    private void convertGBToGray(List<Object> pageTokens, List<Object> editedPageTokens, int counter) {
        Object o1 = pageTokens.get(counter - 3);
        Object o2 = pageTokens.get(counter - 2);
        Object o3 = pageTokens.get(counter - 1);

        //  System.out.println("CMYK values: " + o1 + " " + o2 + " " + o3 + " " + o4);

        float red;
        float green;
        float blue;

        if (o1 instanceof COSFloat) {
            red = ((COSFloat) o1).floatValue();
        } else {
            red = ((COSInteger) o1).floatValue();
        }

        if (o2 instanceof COSFloat) {
            green = ((COSFloat) o2).floatValue();
        } else {
            green = ((COSInteger) o2).floatValue();
        }

        if (o3 instanceof COSFloat) {
            blue = ((COSFloat) o3).floatValue();
        } else {
            blue = ((COSInteger) o3).floatValue();
        }

        //System.out.println("RGB values: " + red + " " + green + " " + blue);

        // convert RGB to grayscale
        float gray1 = 0.299f * red + 0.587f * green + 0.114f * blue;
        BigDecimal bd = new BigDecimal(Float.toString(gray1));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        gray1 = bd.floatValue();
        //System.out.println("Gray1 value: " + gray1);

        // remove CMYK
        editedPageTokens.remove(editedPageTokens.size() - 1);
        editedPageTokens.remove(editedPageTokens.size() - 1);
        editedPageTokens.remove(editedPageTokens.size() - 1);

        // Add grayscale, gray
        editedPageTokens.add(new COSFloat(gray1));
    }
}