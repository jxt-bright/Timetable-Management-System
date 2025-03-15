package com.bright.timetable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class DrawTable {
    public static void drawTable(PDDocument document,
                                 PDPageContentStream contentStream,
                                 float yStart,
                                 float margin,
                                 float tableWidth,
                                 List<List<String>> data,
                                 PDFont headerFont,
                                 PDFont bodyFont,
                                 float fontSize) throws IOException {

        final int rows = data.size();
        final int cols = data.get(0).size();
        final float rowHeight = 20f;
        final float tableHeight = rowHeight * rows;
        final float colWidth = tableWidth / cols;
        final float cellMargin = 5f;

        // Draw header row
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.addRect(margin, yStart - tableHeight, tableWidth, rowHeight);
        contentStream.fill();

        // Draw table cells
        contentStream.setNonStrokingColor(Color.BLACK);
        float y = yStart;
        for (int i = 0; i < rows; i++) {
            List<String> row = data.get(i);
            y -= rowHeight;

            // Draw row background
            if (i % 2 == 0) {
                contentStream.setNonStrokingColor(240, 240, 240);
                contentStream.addRect(margin, y, tableWidth, rowHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(Color.BLACK);
            }

            // Draw cell text
            float x = margin;
            for (String text : row) {
                contentStream.beginText();
                contentStream.setFont(i == 0 ? headerFont : bodyFont, fontSize);
                contentStream.newLineAtOffset(x + cellMargin, y + cellMargin);
                contentStream.showText(text);
                contentStream.endText();
                x += colWidth;
            }

            // Draw grid lines
            contentStream.setStrokingColor(Color.DARK_GRAY);
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(margin, y);
            contentStream.lineTo(margin + tableWidth, y);
            contentStream.stroke();
        }
    }
}