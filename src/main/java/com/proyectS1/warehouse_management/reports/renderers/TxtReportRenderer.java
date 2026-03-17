package com.proyectS1.warehouse_management.reports.renderers;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.reports.model.ReportColumn;
import com.proyectS1.warehouse_management.reports.model.ReportDataset;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;

@Component
public class TxtReportRenderer implements ReportRenderer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_COLUMN_WIDTH = 24;

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.TXT;
    }

    @Override
    public ReportFilePayload render(ReportDataset dataset) {
        StringBuilder builder = new StringBuilder();
        String banner = "=".repeat(108);

        builder.append(banner).append('\n');
        builder.append(center(dataset.title().toUpperCase(), 108)).append('\n');
        builder.append(center(dataset.subtitle(), 108)).append('\n');
        builder.append(banner).append("\n\n");

        builder.append(wrap(dataset.description(), 104)).append("\n\n");
        builder.append("Generado por: ").append(dataset.generatedBy()).append('\n');
        builder.append("Fecha de generacion: ").append(DATE_FORMATTER.format(dataset.generatedAt())).append("\n\n");

        builder.append("RESUMEN EJECUTIVO\n");
        builder.append("-".repeat(108)).append('\n');
        dataset.summary().forEach(item -> builder
            .append("• ")
            .append(item.label())
            .append(": ")
            .append(item.value())
            .append('\n'));

        builder.append("\nFILTROS APLICADOS\n");
        builder.append("-".repeat(108)).append('\n');
        if (dataset.filters().isEmpty()) {
            builder.append("• Sin filtros\n");
        } else {
            dataset.filters().forEach((key, value) -> builder.append("• ").append(key).append(": ").append(value).append('\n'));
        }

        builder.append("\nDETALLE TABULAR\n");
        builder.append("-".repeat(108)).append('\n');
        if (dataset.rows().isEmpty()) {
            builder.append("No hay registros disponibles para los filtros seleccionados.\n");
        } else {
            List<Integer> columnWidths = resolveColumnWidths(dataset);
            appendTableBorder(builder, columnWidths);
            appendTableRow(builder, dataset.columns().stream().map(ReportColumn::label).toList(), columnWidths);
            appendTableBorder(builder, columnWidths);
            for (Map<String, String> row : dataset.rows()) {
                appendTableRow(
                    builder,
                    dataset.columns().stream()
                        .map(column -> row.getOrDefault(column.key(), "-"))
                        .toList(),
                    columnWidths
                );
            }
            appendTableBorder(builder, columnWidths);
        }

        return new ReportFilePayload(
            builder.toString().getBytes(StandardCharsets.UTF_8),
            MediaType.TEXT_PLAIN,
            "txt"
        );
    }

    private List<Integer> resolveColumnWidths(ReportDataset dataset) {
        List<Integer> widths = new ArrayList<>();
        for (ReportColumn column : dataset.columns()) {
            int width = Math.min(MAX_COLUMN_WIDTH, Math.max(12, column.label().length()));
            for (Map<String, String> row : dataset.rows()) {
                width = Math.min(MAX_COLUMN_WIDTH, Math.max(width, row.getOrDefault(column.key(), "-").length()));
            }
            widths.add(width);
        }
        return widths;
    }

    private void appendTableBorder(StringBuilder builder, List<Integer> widths) {
        builder.append('+');
        widths.forEach(width -> builder.append("-".repeat(width + 2)).append('+'));
        builder.append('\n');
    }

    private void appendTableRow(StringBuilder builder, List<String> values, List<Integer> widths) {
        builder.append('|');
        for (int index = 0; index < values.size(); index++) {
            String value = truncate(values.get(index), widths.get(index));
            builder.append(' ')
                .append(padRight(value, widths.get(index)))
                .append(' ')
                .append('|');
        }
        builder.append('\n');
    }

    private String center(String value, int width) {
        String safeValue = value == null ? "" : value;
        if (safeValue.length() >= width) {
            return safeValue;
        }
        int leftPadding = (width - safeValue.length()) / 2;
        return " ".repeat(leftPadding) + safeValue;
    }

    private String wrap(String value, int width) {
        String safeValue = value == null ? "" : value;
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : safeValue.split(" ")) {
            if (current.isEmpty()) {
                current.append(word);
                continue;
            }

            if (current.length() + word.length() + 1 > width) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current.append(' ').append(word);
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        return String.join("\n", lines);
    }

    private String padRight(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }

    private String truncate(String value, int width) {
        String safeValue = value == null ? "-" : value.replace('\n', ' ');
        if (safeValue.length() <= width) {
            return safeValue;
        }
        return safeValue.substring(0, Math.max(1, width - 1)) + "…";
    }
}
