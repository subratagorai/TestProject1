package org.pdfoperation;

/**
 * This is the main class for the grayscale converter application.
 * It creates an instance of the ConvertToGrayScale class and calls the convertPDFToGrayScaleUsingPDFBox method.
 * This method converts a PDF document to grayscale using PDFBox.
 *
 * @author SUBRATAG
 */
public class GrayScaleConverterMain {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");
       // call convertPDFToGreyScaleUsingPDFBox method
        ConvertToGrayScale convertToGreyScale = new ConvertToGrayScale();
        convertToGreyScale.convertPDFToGrayScaleUsingPDFBox();

    }
}