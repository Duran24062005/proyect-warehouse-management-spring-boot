package com.proyectS1.warehouse_management.reports.renderers;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.reports.model.ReportDataset;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;

@Component
public class CsvReportRenderer implements ReportRenderer {

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.CSV;
    }

    @Override
    public ReportFilePayload render(ReportDataset dataset) {
        StringBuilder builder = new StringBuilder();
        builder.append(dataset.columns().stream()
            .map(column -> escapeValue(column.label()))
            .collect(Collectors.joining(",")))
            .append('\n');

        dataset.rows().forEach(row -> {
            builder.append(dataset.columns().stream()
                .map(column -> escapeValue(row.getOrDefault(column.key(), "")))
                .collect(Collectors.joining(",")))
                .append('\n');
        });

        return new ReportFilePayload(
            builder.toString().getBytes(StandardCharsets.UTF_8),
            MediaType.parseMediaType("text/csv"),
            "csv"
        );
    }

    private String escapeValue(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
