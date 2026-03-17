package com.proyectS1.warehouse_management.reports.renderers;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.reports.model.ReportColumn;
import com.proyectS1.warehouse_management.reports.model.ReportDataset;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;
import com.proyectS1.warehouse_management.reports.model.ReportSummaryItem;

@Component
public class PdfReportRenderer implements ReportRenderer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_OBLIQUE = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.PDF;
    }

    @Override
    public ReportFilePayload render(ReportDataset dataset) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PageCursor cursor = newPage(document, null);
            cursor = drawHeader(cursor, dataset);
            cursor = drawSummary(cursor, dataset.summary());
            cursor = drawFilters(cursor, dataset.filters());
            cursor = drawTable(cursor, dataset);
            cursor.stream.close();
            document.save(output);
            return new ReportFilePayload(output.toByteArray(), MediaType.APPLICATION_PDF, "pdf");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate PDF report", exception);
        }
    }

    private PageCursor drawHeader(PageCursor cursor, ReportDataset dataset) throws IOException {
        setFillColor(cursor, 18, 34, 58);
        cursor.stream.addRect(42, cursor.page.getMediaBox().getHeight() - 170, cursor.contentWidth, 120);
        cursor.stream.fill();

        drawText(cursor, "LOGITRACK REPORTING SUITE", 58, 732, FONT_BOLD, 10, 196, 219, 246);
        drawText(cursor, dataset.title(), 58, 708, FONT_BOLD, 22, 255, 255, 255);
        drawText(cursor, dataset.subtitle(), 58, 688, FONT_OBLIQUE, 12, 187, 247, 208);

        float descriptionY = 668;
        for (String line : wrap(dataset.description(), 84)) {
            drawText(cursor, line, 58, descriptionY, FONT_REGULAR, 10, 226, 232, 240);
            descriptionY -= 13;
        }

        drawText(cursor, "Generado por: " + dataset.generatedBy(), 390, 710, FONT_BOLD, 10, 226, 232, 240);
        drawText(cursor, DATE_FORMATTER.format(dataset.generatedAt()), 390, 694, FONT_REGULAR, 10, 187, 247, 208);
        cursor.y = 610;
        return cursor;
    }

    private PageCursor drawSummary(PageCursor cursor, List<ReportSummaryItem> summary) throws IOException {
        drawSectionTitle(cursor, "Resumen ejecutivo");
        float cardWidth = (cursor.contentWidth - 16) / 2;
        float currentX = cursor.margin;
        float currentY = cursor.y;
        int columnIndex = 0;

        for (ReportSummaryItem item : summary) {
            if (currentY < 150) {
                cursor = newPage(cursor.document, cursor);
                drawSectionTitle(cursor, "Resumen ejecutivo");
                currentX = cursor.margin;
                currentY = cursor.y;
                columnIndex = 0;
            }

            setFillColor(cursor, 244, 247, 251);
            cursor.stream.addRect(currentX, currentY - 40, cardWidth, 34);
            cursor.stream.fill();
            drawText(cursor, item.label(), currentX + 10, currentY - 19, FONT_REGULAR, 10, 71, 85, 105);
            drawText(cursor, item.value(), currentX + 10, currentY - 34, FONT_BOLD, 14, 15, 23, 42);

            columnIndex++;
            if (columnIndex == 2) {
                columnIndex = 0;
                currentX = cursor.margin;
                currentY -= 52;
            } else {
                currentX += cardWidth + 16;
            }
        }

        cursor.y = currentY - 18;
        return cursor;
    }

    private PageCursor drawFilters(PageCursor cursor, Map<String, String> filters) throws IOException {
        if (cursor.y < 140) {
            cursor = newPage(cursor.document, cursor);
        }

        drawSectionTitle(cursor, "Filtros aplicados");
        if (filters.isEmpty()) {
            drawText(cursor, "Sin filtros", cursor.margin, cursor.y, FONT_REGULAR, 10, 71, 85, 105);
            cursor.y -= 18;
            return cursor;
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            drawText(cursor, entry.getKey() + ": " + entry.getValue(), cursor.margin, cursor.y, FONT_REGULAR, 10, 71, 85, 105);
            cursor.y -= 14;
        }

        cursor.y -= 12;
        return cursor;
    }

    private PageCursor drawTable(PageCursor cursor, ReportDataset dataset) throws IOException {
        if (cursor.y < 160) {
            cursor = newPage(cursor.document, cursor);
        }

        drawSectionTitle(cursor, "Detalle tabular");
        if (dataset.rows().isEmpty()) {
            drawText(cursor, "No hay registros disponibles para los filtros seleccionados.", cursor.margin, cursor.y, FONT_REGULAR, 10, 100, 116, 139);
            return cursor;
        }

        int visibleColumns = Math.min(4, dataset.columns().size());
        float tableWidth = cursor.contentWidth;
        float columnWidth = tableWidth / visibleColumns;
        float y = cursor.y;

        setFillColor(cursor, 15, 23, 42);
        cursor.stream.addRect(cursor.margin, y - 22, tableWidth, 20);
        cursor.stream.fill();

        for (int index = 0; index < visibleColumns; index++) {
            drawText(cursor, dataset.columns().get(index).label(), cursor.margin + 8 + (index * columnWidth), y - 15, FONT_BOLD, 9, 236, 253, 245);
        }

        y -= 30;
        List<Map<String, String>> rows = dataset.rows();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            if (y < 70) {
                cursor = newPage(cursor.document, cursor);
                y = cursor.y;
                setFillColor(cursor, 15, 23, 42);
                cursor.stream.addRect(cursor.margin, y - 22, tableWidth, 20);
                cursor.stream.fill();
                for (int index = 0; index < visibleColumns; index++) {
                    drawText(cursor, dataset.columns().get(index).label(), cursor.margin + 8 + (index * columnWidth), y - 15, FONT_BOLD, 9, 236, 253, 245);
                }
                y -= 30;
            }

            setFillColor(cursor, rowIndex % 2 == 0 ? 248 : 241, rowIndex % 2 == 0 ? 250 : 245, rowIndex % 2 == 0 ? 252 : 249);
            cursor.stream.addRect(cursor.margin, y - 20, tableWidth, 18);
            cursor.stream.fill();

            for (int columnIndex = 0; columnIndex < visibleColumns; columnIndex++) {
                ReportColumn column = dataset.columns().get(columnIndex);
                String value = truncate(rows.get(rowIndex).getOrDefault(column.key(), "-"), 24);
                drawText(cursor, value, cursor.margin + 8 + (columnIndex * columnWidth), y - 14, FONT_REGULAR, 8, 30, 41, 59);
            }
            y -= 22;
        }
        return cursor;
    }

    private void drawSectionTitle(PageCursor cursor, String title) throws IOException {
        drawText(cursor, title, cursor.margin, cursor.y, FONT_BOLD, 13, 15, 23, 42);
        cursor.y -= 18;
    }

    private void drawText(PageCursor cursor, String text, float x, float y, PDType1Font font, float fontSize, int red, int green, int blue)
        throws IOException {
        cursor.stream.beginText();
        cursor.stream.setFont(font, fontSize);
        cursor.stream.setNonStrokingColor(new Color(red, green, blue));
        cursor.stream.newLineAtOffset(x, y);
        cursor.stream.showText(sanitize(text));
        cursor.stream.endText();
    }

    private void setFillColor(PageCursor cursor, int red, int green, int blue) throws IOException {
        cursor.stream.setNonStrokingColor(new Color(red, green, blue));
    }

    private List<String> wrap(String text, int maxChars) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            if (current.isEmpty()) {
                current.append(word);
                continue;
            }

            if (current.length() + word.length() + 1 > maxChars) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current.append(' ').append(word);
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String truncate(String text, int limit) {
        String safeText = sanitize(text);
        if (safeText.length() <= limit) {
            return safeText;
        }
        return safeText.substring(0, Math.max(1, limit - 1)) + "...";
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\t', ' ');
    }

    private PageCursor newPage(PDDocument document, PageCursor previous) throws IOException {
        if (previous != null) {
            previous.stream.close();
        }
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        return new PageCursor(document, page, stream, 42, 42, page.getMediaBox().getWidth() - 84, 750);
    }

    private static final class PageCursor {
        private final PDDocument document;
        private final PDPage page;
        private final PDPageContentStream stream;
        private final float margin;
        private final float top;
        private final float contentWidth;
        private float y;

        private PageCursor(PDDocument document, PDPage page, PDPageContentStream stream, float margin, float top, float contentWidth, float y) {
            this.document = document;
            this.page = page;
            this.stream = stream;
            this.margin = margin;
            this.top = top;
            this.contentWidth = contentWidth;
            this.y = y;
        }
    }
}
