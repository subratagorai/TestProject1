package org.pdfoperation.main;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDFMerger {
    private static final String BASE_DIR = "/Users/subratag/Documents/git/github/pdfoperations/src/main/resources/";
    private static final String INPUT_DIR = BASE_DIR + "input";
    private static final String INPUT_FILE="Shipping.pdf";
    private static final String OUTPUT_DIR = BASE_DIR + "output";
    private static final String OUT_PUT_FILE = "Merged_"+INPUT_FILE;
    private static final  Logger LOGGER = Logger.getLogger(PDFSplitter.class.getName());
    public static void main(String[] args) throws IOException {

        LOGGER.log(Level.INFO, "Merging PDF");
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setAcroFormMergeMode(PDFMergerUtility.AcroFormMergeMode.JOIN_FORM_FIELDS_MODE);
        pdfMerger.setDocumentMergeMode(PDFMergerUtility.DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);
        PDDocument targetDocument = new PDDocument();

        File file = new File(INPUT_DIR + "/" + INPUT_FILE);
        for(int i=0; i<3 ; i++)
        {
            PDDocument document = Loader.loadPDF(file);
            pdfMerger.appendDocument(targetDocument, document);
            document.close();
        }

        targetDocument.save(OUTPUT_DIR + "/" + OUT_PUT_FILE);
        targetDocument.close();
    }
}
