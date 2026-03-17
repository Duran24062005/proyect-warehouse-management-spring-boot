package com.proyectS1.warehouse_management.reports.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.proyectS1.warehouse_management.model.enums.ReportType;

public record ReportDataset(
    ReportType reportType,
    String title,
    String subtitle,
    String description,
    LocalDateTime generatedAt,
    String generatedBy,
    Map<String, String> filters,
    List<ReportSummaryItem> summary,
    List<ReportColumn> columns,
    List<Map<String, String>> rows
) {
}
