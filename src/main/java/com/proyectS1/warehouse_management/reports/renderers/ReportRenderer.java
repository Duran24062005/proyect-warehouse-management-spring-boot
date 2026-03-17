package com.proyectS1.warehouse_management.reports.renderers;

import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.reports.model.ReportDataset;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;

public interface ReportRenderer {

    ReportFormat getFormat();

    ReportFilePayload render(ReportDataset dataset);
}
