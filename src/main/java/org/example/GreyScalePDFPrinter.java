package org.example;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.io.File;
import java.io.IOException;

public class GreyScalePDFPrinter {

    public static final String BASE_DIR = "/Users/subratag/Documents/git/github/TestProject1/src/main/resources/input";

    public void printGreyScalePDF() throws IOException {
        File file = new File(BASE_DIR + "/BSS-232261.pdf");
        PDDocument document = Loader.loadPDF(file);
        int pageCount = document.getPages().getCount();
        for (int i = 0; i < pageCount; i++) {
            PDPage currentPage = document.getPage(i);
            currentPage.getResources();
            GrayscaleCreator pdfStreamEngine = new GrayscaleCreator();
            pdfStreamEngine.handlePage(document.getPage(i));
        }
        document.save(BASE_DIR + "/BSS-232261_GreyScale.pdf");
        document.close();
    }

}
