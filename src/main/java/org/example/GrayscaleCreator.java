package org.example;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GrayscaleCreator extends PDFGraphicsStreamEngine {
    protected GrayscaleCreator(PDPage page) {
        super(page);
    }

    @Override
    public void appendRectangle(Point2D point2D, Point2D point2D1, Point2D point2D2, Point2D point2D3) throws IOException {
        System.out.printf("Append rectangle: %s %s %s %s%n", point2D, point2D1, point2D2, point2D3);
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        System.out.printf("Draw image: %s%n", pdImage);
    }

    @Override
    public void clip(int i) throws IOException {
        System.out.printf("Clip: %d%n", i);
    }

    @Override
    public void moveTo(float v, float v1) throws IOException {
        System.out.printf("Move to: %f %f%n", v, v1);
    }

    @Override
    public void lineTo(float v, float v1) throws IOException {
        System.out.printf("Line to: %f %f%n", v, v1);
    }

    @Override
    public void curveTo(float v, float v1, float v2, float v3, float v4, float v5) throws IOException {
        System.out.printf("Curve to: %f %f %f %f %f %f%n", v, v1, v2, v3, v4, v5);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return null;
    }

    @Override
    public void closePath() throws IOException {
        System.out.printf("End closePath%n");
    }

    @Override
    public void endPath() throws IOException {
        System.out.printf("End path%n");
    }

    @Override
    public void strokePath() throws IOException {
        System.out.printf("Stroke path%n");
    }

    @Override
    public void fillPath(int i) throws IOException {
        System.out.printf("Fill path: %d%n", i);
    }

    @Override
    public void fillAndStrokePath(int i) throws IOException {
        System.out.printf("Fill and stroke path: %d%n", i);
    }

    @Override
    public void shadingFill(COSName cosName) throws IOException {
        System.out.printf("Shading fill: %s%n", cosName);
    }

    public void handlePage(PDPage page) throws IOException {
        processPage(page);

    }

    @Override
    public void processPage(PDPage page) throws IOException
    {
        super.processPage(page);
        PDFStreamParser parser = new PDFStreamParser(page);
        List<Object> pageTokens = parser.parse();
        System.out.println("pageTokens size: " + pageTokens.size());
        List<Object> editedPageTokens = new ArrayList<>();
        List<COSBase> arguments = new ArrayList<>();
        for (int counter = 0; counter < pageTokens.size(); counter++) {
            Object token = pageTokens.get(counter);
            if (token instanceof Operator) {
                processOperator1((Operator) token,counter, arguments, pageTokens);
                arguments.clear();
            } else {
                arguments.add((COSBase) token);
            }
        }
        PDFStreamParser parser1 = new PDFStreamParser(getPage());
        List<Object> pageTokens1 = parser1.parse();
        System.out.println("pageTokens1 size: " + pageTokens1.size());
    }

    protected void processOperator1(Operator operator, int counter, List<COSBase> arguments, List<Object> pageTokens) throws IOException {
       // super.processOperator();
        if (operator.getName().equals("k")) {
            System.out.println("Color Operator: " + operator.getName() + " for token counter: " + counter);
            float grey = convertCMYKToRGBToGrey(pageTokens, counter);
            List<COSBase> operands = new ArrayList<>();
            operands.add(new COSFloat(grey));
            this.processOperator(Operator.getOperator("g"), operands);
           // setNonStrokingDeviceGrayColor.process(operator, operands);
        }
        if (operator.getName().equals("K")) {
            System.out.println("Color Operator: " + operator.getName() + " for token counter: " + counter);
            float grey = convertCMYKToRGBToGrey(pageTokens, counter);
            List<COSBase> operands = new ArrayList<>();
            operands.add(new COSFloat(grey));
            this.processOperator(Operator.getOperator("G"), operands);
        }
        //super.processOperator(operator, arguments);
    }

    private float convertCMYKToRGBToGrey(List<Object> pageTokens, int counter) {
        Object o1 = pageTokens.get(counter - 4);
        Object o2 = pageTokens.get(counter - 3);
        Object o3 = pageTokens.get(counter - 2);
        Object o4 = pageTokens.get(counter - 1);
        System.out.println("CMYK values: " + o1 + " " + o2 + " " + o3 + " " + o4);
        float cyan;
        float magenta;
        float yellow;
        float black;
        float grey1;

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
        System.out.println("RGB values: " + red + " " + green + " " + blue);

        // convert RGB to greyscale
        int grey = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
        System.out.println("Grey value: " + grey);
        // Add greyscale, grey
        grey1 = (float) grey / 255;
        BigDecimal bd = new BigDecimal(Float.toString(grey1));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        grey1 = bd.floatValue();
        System.out.println("Grey1 value: " + grey1);
        return grey1;
    }

}
