package com.proyectS1.warehouse_management.reports.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.reports.model.ReportColumn;
import com.proyectS1.warehouse_management.reports.model.ReportDataset;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;
import com.proyectS1.warehouse_management.reports.model.ReportSummaryItem;

@Component
public class ImageReportRenderer implements ReportRenderer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.IMG;
    }

    @Override
    public ReportFilePayload render(ReportDataset dataset) {
        int summaryCards = Math.max(1, dataset.summary().size());
        int summaryRows = (summaryCards + 2) / 3;
        int previewRows = Math.min(5, dataset.rows().size());
        int width = 1500;
        int height = 470 + summaryRows * 170 + previewRows * 58;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setPaint(new GradientPaint(0, 0, new Color(7, 16, 31), width, height, new Color(21, 54, 39)));
            graphics.fillRect(0, 0, width, height);

            graphics.setColor(new Color(255, 255, 255, 20));
            graphics.fillRoundRect(42, 42, width - 84, height - 84, 34, 34);

            drawHeader(graphics, dataset, width);
            drawSummary(graphics, dataset.summary());
            drawTablePreview(graphics, dataset);

            ImageIO.write(image, "png", output);
            return new ReportFilePayload(output.toByteArray(), MediaType.IMAGE_PNG, "png");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate image report", exception);
        } finally {
            graphics.dispose();
        }
    }

    private void drawHeader(Graphics2D graphics, ReportDataset dataset, int width) {
        graphics.setColor(new Color(226, 232, 240, 160));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 18));
        graphics.drawString("LogiTrack Reporting Suite", 86, 98);

        graphics.setColor(new Color(240, 253, 244));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 42));
        graphics.drawString(dataset.title(), 86, 146);

        graphics.setColor(new Color(187, 247, 208));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 25));
        graphics.drawString(dataset.subtitle(), 86, 182);

        graphics.setColor(new Color(203, 213, 225));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 20));
        drawWrappedLine(graphics, dataset.description(), 86, 224, width - 180, 28);

        graphics.setColor(new Color(255, 255, 255, 18));
        graphics.fillRoundRect(width - 430, 78, 300, 116, 24, 24);
        graphics.setColor(new Color(203, 213, 225));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 18));
        graphics.drawString("Generado por", width - 396, 116);
        graphics.setColor(new Color(248, 250, 252));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 22));
        graphics.drawString(dataset.generatedBy(), width - 396, 146);
        graphics.setColor(new Color(187, 247, 208));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 18));
        graphics.drawString(DATE_FORMATTER.format(dataset.generatedAt()), width - 396, 174);
    }

    private void drawSummary(Graphics2D graphics, List<ReportSummaryItem> summary) {
        int cardWidth = 410;
        int cardHeight = 124;
        int startX = 86;
        int startY = 286;
        int gap = 26;

        for (int index = 0; index < summary.size(); index++) {
            ReportSummaryItem item = summary.get(index);
            int row = index / 3;
            int col = index % 3;
            int x = startX + col * (cardWidth + gap);
            int y = startY + row * (cardHeight + gap);

            graphics.setColor(new Color(9, 18, 35, 208));
            graphics.fillRoundRect(x, y, cardWidth, cardHeight, 28, 28);
            graphics.setColor(new Color(74, 222, 128, 90));
            graphics.setStroke(new BasicStroke(1.5f));
            graphics.drawRoundRect(x, y, cardWidth, cardHeight, 28, 28);

            graphics.setFont(new Font("SansSerif", Font.PLAIN, 20));
            graphics.setColor(new Color(148, 163, 184));
            graphics.drawString(item.label(), x + 28, y + 40);

            graphics.setFont(new Font("SansSerif", Font.BOLD, 34));
            graphics.setColor(new Color(248, 250, 252));
            graphics.drawString(item.value(), x + 28, y + 86);
        }
    }

    private void drawTablePreview(Graphics2D graphics, ReportDataset dataset) {
        int previewStartY = 286 + ((Math.max(1, dataset.summary().size()) + 2) / 3) * 150 + 70;
        int x = 86;
        int width = 1328;

        graphics.setColor(new Color(255, 255, 255, 18));
        graphics.fillRoundRect(x, previewStartY, width, 74, 26, 26);
        graphics.setColor(new Color(226, 232, 240));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
        graphics.drawString("Vista tabular del reporte", x + 28, previewStartY + 32);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 18));
        graphics.setColor(new Color(148, 163, 184));
        graphics.drawString("Se muestran hasta 5 filas como referencia visual del archivo descargado.", x + 28, previewStartY + 58);

        int tableY = previewStartY + 96;
        int rowHeight = 54;
        int visibleColumns = Math.min(4, dataset.columns().size());
        int columnWidth = width / Math.max(1, visibleColumns);

        graphics.setColor(new Color(20, 30, 48, 225));
        graphics.fillRoundRect(x, tableY, width, rowHeight, 18, 18);
        graphics.setColor(new Color(187, 247, 208));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 18));
        for (int index = 0; index < visibleColumns; index++) {
            graphics.drawString(dataset.columns().get(index).label(), x + 20 + index * columnWidth, tableY + 34);
        }

        List<Map<String, String>> rows = dataset.rows().stream().limit(5).toList();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            int y = tableY + rowHeight + rowIndex * rowHeight;
            graphics.setColor(rowIndex % 2 == 0 ? new Color(255, 255, 255, 12) : new Color(255, 255, 255, 6));
            graphics.fillRoundRect(x, y, width, rowHeight - 6, 16, 16);
            graphics.setColor(new Color(241, 245, 249));
            graphics.setFont(new Font("SansSerif", Font.PLAIN, 16));

            for (int columnIndex = 0; columnIndex < visibleColumns; columnIndex++) {
                ReportColumn column = dataset.columns().get(columnIndex);
                String value = truncate(rows.get(rowIndex).getOrDefault(column.key(), "-"), 24);
                graphics.drawString(value, x + 20 + columnIndex * columnWidth, y + 30);
            }
        }
    }

    private void drawWrappedLine(Graphics2D graphics, String text, int x, int y, int maxWidth, int lineHeight) {
        StringBuilder line = new StringBuilder();
        int currentY = y;
        for (String word : text.split(" ")) {
            String testLine = line.isEmpty() ? word : line + " " + word;
            if (graphics.getFontMetrics().stringWidth(testLine) > maxWidth) {
                graphics.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (!line.isEmpty()) {
            graphics.drawString(line.toString(), x, currentY);
        }
    }

    private String truncate(String value, int limit) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, Math.max(1, limit - 1)) + "…";
    }
}
