package org.example;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class MyAppMain {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");
       // call convertPDFToGreyScaleUsingPDFBox method
        ConvertToGreyScale convertToGreyScale = new ConvertToGreyScale();
        convertToGreyScale.convertPDFToGreyScaleUsingPDFBox();
//        GreyScalePDFPrinter greyScalePDFPrinter = new GreyScalePDFPrinter();
//        try {
//            greyScalePDFPrinter.printGreyScalePDF();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }
}