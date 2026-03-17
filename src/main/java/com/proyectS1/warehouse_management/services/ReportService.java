package com.proyectS1.warehouse_management.services;

import com.proyectS1.warehouse_management.dtos.response.ReportPreviewResponseDTO;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;
import com.proyectS1.warehouse_management.reports.model.ReportQuery;

public interface ReportService {

    ReportPreviewResponseDTO generatePreview(ReportQuery query);

    ReportFilePayload generateDownload(ReportQuery query);
}
