package org.pdfoperation;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class is the main entry point for the PDF splitting application.
 * It uses the Apache PDFBox library to split a PDF document into individual pages.
 * The input PDF document is loaded from a specified directory, and the Splitter class is used to split the document.
 * Each page of the split document is then saved as a separate PDF file in the output directory.
 *
 * @author SUBRATAG
 */
public class PDFSplitterMain {
    public static final String BASE_DIR = "/Users/subratag/Documents/git/github/TestProject1/src/main/resources/input/";
    private static final String INPUT_FILE="Updated_ConsolidatedBill_AG_20240301_M01_1_1_4.pdf";
    private static final String OUT_PUT_FILE = "Updated_"+INPUT_FILE;

    public static void main(String[] args) throws IOException {

        File file = new File(BASE_DIR + "/" + INPUT_FILE);
        PDDocument document = Loader.loadPDF(file);
        System.out.println("PDF loaded");
        Splitter splitter = new Splitter();
        List<PDDocument> splitDocuments = splitter.split(document);

        // Save each individual page as a separate PDF
        for (int i = 0; i < 2; i++) {
            PDDocument page = splitDocuments.get(i);
            page.save(new File(BASE_DIR+"output_page_" + (i + 1) + ".pdf"));
            page.close();
        }

        document.close();
        System.out.println("Splitting PDF");
    }
}
