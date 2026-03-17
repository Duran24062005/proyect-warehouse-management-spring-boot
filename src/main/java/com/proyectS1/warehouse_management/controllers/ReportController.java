package com.proyectS1.warehouse_management.controllers;

import java.util.Locale;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.response.ReportPreviewResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementAnalyticsResponseDTO;
import com.proyectS1.warehouse_management.model.enums.MovementType;
import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.model.enums.ReportType;
import com.proyectS1.warehouse_management.reports.model.MovementAnalyticsQuery;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;
import com.proyectS1.warehouse_management.reports.model.ReportQuery;
import com.proyectS1.warehouse_management.services.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "Endpoints para generar previsualizaciones y descargas de reportes")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid report type, format or filters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Genera preview o descarga de reportes")
    public ResponseEntity<?> getReport(
        @RequestParam String type,
        @RequestParam(defaultValue = "json") String format,
        @RequestParam(required = false) Long productId,
        @RequestParam(required = false) Long warehouseId,
        @RequestParam(required = false) MovementType movementType,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Long managerUserId
    ) {
        ReportQuery query = new ReportQuery(
            parseType(type),
            parseFormat(format),
            productId,
            warehouseId,
            movementType,
            category,
            managerUserId
        );

        if (query.format() == ReportFormat.JSON) {
            ReportPreviewResponseDTO preview = reportService.generatePreview(query);
            return ResponseEntity.ok(preview);
        }

        ReportFilePayload payload = reportService.generateDownload(query);
        return ResponseEntity.ok()
            .contentType(payload.mediaType())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(payload.filename()).build().toString())
            .body(payload.content());
    }

    @GetMapping("/analytics/movements")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Movement analytics generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid analytics filters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Genera analiticas temporales para movimientos")
    public ResponseEntity<MovementAnalyticsResponseDTO> getMovementAnalytics(
        @RequestParam(defaultValue = "30d") String window,
        @RequestParam(required = false) Long productId,
        @RequestParam(required = false) Long warehouseId,
        @RequestParam(required = false) MovementType movementType
    ) {
        return ResponseEntity.ok(reportService.generateMovementAnalytics(
            new MovementAnalyticsQuery(window, productId, warehouseId, movementType)
        ));
    }

    private ReportType parseType(String value) {
        try {
            return ReportType.fromToken(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private ReportFormat parseFormat(String value) {
        try {
            return ReportFormat.fromToken(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
