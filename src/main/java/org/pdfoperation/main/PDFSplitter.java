package org.pdfoperation.main;

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
public class PDFSplitter {
    private static final String BASE_DIR = "/Users/subratag/Documents/git/github/pdfoperations/src/main/resources/";
    private static final String INPUT_DIR = BASE_DIR + "input";
    private static final String INPUT_FILE="ConsolidatedBill_AG_20240301_M01_1_1_4.pdf";
    private static final String OUTPUT_DIR = BASE_DIR + "output";
    private static final String OUT_PUT_FILE = "ConsolidatedBill_Sample.pdf";

    public static void main(String[] args) throws IOException {

        File file = new File(INPUT_DIR + "/" + INPUT_FILE);
        PDDocument document = Loader.loadPDF(file);
        System.out.println("PDF loaded");
        Splitter splitter = new Splitter();
        splitter.setStartPage(1);
        splitter.setEndPage(50);
        splitter.setSplitAtPage(50 - 1 +1);
        List<PDDocument> splitDocuments = splitter.split(document);

        PDDocument page = splitDocuments.get(0);
        page.save(new File(OUTPUT_DIR+ "/" + OUT_PUT_FILE ));
        page.close();

        document.close();
        System.out.println("Extract  PDF");
    }
}
